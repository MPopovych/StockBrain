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

	private val take = 5
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
			max(min(sqrtDistance * (it / distSum), 1f), -1f)
		}
		return positionDeltaArray
	}
}

class GaussianVelocityPolicy() : VelocityPolicy {

	private val jRandom: java.util.Random = java.util.Random()
	override fun move(mod: ModelGenes, context: PolicyContext) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val sqrtCount = sqrt(totalGeneCount.toFloat())

		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				weightGenes.genes.indices.forEach {
					var randomK = 1.0f
					if (Random.nextInt(totalGeneCount) == 0) {
						randomK = 10.0f
					}
					val avgCount = (weightGenes.size + sqrtCount) / 2
					val m = jRandom.nextGaussian(0.0, 0.02).toFloat()
					val a = jRandom.nextGaussian(0.0, 1.0).toFloat() * randomK * context.choreographyK / avgCount
					val v = weightGenes.genes[it] + weightGenes.genes[it] * m + a
					weightGenes.genes[it] = max(min(1.1f, v), -1.1f) * 0.995f
//					weightGenes.genes[it] = v
				}

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}
}