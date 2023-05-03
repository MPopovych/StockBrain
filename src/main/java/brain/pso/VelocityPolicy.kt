package brain.pso

import brain.ga.weights.ModelGenes
import brain.utils.roundUpInt
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

interface VelocityPolicy {
	companion object {
		val ConstNoise = ConstNoiseVelocityPolicy()
		val Distance = CappedDistanceVelocityPolicy()
		val Gaussian = GaussianVelocityPolicy()
	}

	fun move(mod: ModelGenes, context: PolicyContext)
}

class ConstNoiseVelocityPolicy : VelocityPolicy {
	override fun move(mod: ModelGenes, context: PolicyContext) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val moveVector = produceMoveVector(totalGeneCount)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] = (weightGenes.genes[it] + subVector[it])
				}
				processed += weightGenes.size

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}

	private val take = 50
	private fun produceMoveVector(totalGeneCount: Int): List<Float> {
		val randomPeaks = (0 until take).map {
			(Random.nextFloat() * 2 - 1f)
		}
		val randomFlats = (take until totalGeneCount).map {
			(Random.nextFloat() * 2 - 1f) * 0.1f / totalGeneCount
		}
		return (randomPeaks + randomFlats).shuffled()
	}
}


class CappedDistanceVelocityPolicy() : VelocityPolicy {
	private val fraction = 0.10

	override fun move(mod: ModelGenes, context: PolicyContext) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val moveVector = produceMoveVector(totalGeneCount, context)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] = (weightGenes.genes[it] + subVector[it])
				}
				processed += weightGenes.size

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}

	private fun produceMoveVector(totalGeneCount: Int, context: PolicyContext): List<Float> {
		val sqrtR = (totalGeneCount.toDouble() * fraction).roundUpInt()
		val sqrtDistance = sqrt(totalGeneCount.toFloat())

		val randomPeaks = (0 until sqrtR).map {
			(Random.nextFloat() * 2 - 1f) * context.choreographyK
		}
		val randomFlats = (sqrtR until totalGeneCount).map {
			(Random.nextFloat() * 2 - 1f) * context.choreographyK / totalGeneCount
		}
		val distributionArray = (randomPeaks + randomFlats).shuffled()
		val distSum = distributionArray.map { abs(it) }.sum()
		val positionDeltaArray = distributionArray.map {
			max(min(10 * (it / distSum), 1f), -1f)
		}
		return positionDeltaArray
	}
}

class GaussianVelocityPolicy(
	val multi: Float = 1f,
	private val clamp: Float? = 1.3f,
	private val normalise: Boolean = true
) : VelocityPolicy {

	private val jRandom: java.util.Random = java.util.Random()
	override fun move(mod: ModelGenes, context: PolicyContext) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val sqrtCount = sqrt(totalGeneCount.toFloat())

		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val avgCount = (weightGenes.size + sqrtCount)
				val avg = weightGenes.genes.average().toFloat()

				weightGenes.genes.indices.forEach {
					// move only half of weights, smooths out training
					if (jRandom.nextBoolean()) return@forEach

					// add normal noise
					val a = 3f * jRandom.nextGaussian().toFloat() * context.choreographyK / avgCount
					// move the average to negative at random
					val counter = if (normalise) {
						2f * jRandom.nextGaussian().toFloat() * context.choreographyK * -avg
					} else {
						0f
					}
					val v = weightGenes.genes[it] + (a + counter) * multi
					if (clamp != null) {
						weightGenes.genes[it] = max(min(clamp, v), -clamp)
					} else {
						weightGenes.genes[it] = v
					}
				}

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}
}