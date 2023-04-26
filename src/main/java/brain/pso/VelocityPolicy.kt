package brain.pso

import brain.ga.weights.ModelGenes
import brain.utils.roundUpInt
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface VelocityPolicy {
	companion object {
		val ConstNoise = ConstNoiseVelocityPolicy()
		val Distance = CappedDistanceVelocityPolicy()
	}

	fun move(mod: ModelGenes)
}

class ConstNoiseVelocityPolicy : VelocityPolicy {
	override fun move(mod: ModelGenes) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val moveVector = produceMoveVector(totalGeneCount)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] = (weightGenes.genes[it] + subVector[it]) * 0.995f
				}
				processed += weightGenes.size

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}

	private fun produceMoveVector(totalGeneCount: Int): List<Float> {
		val randomPeaks = (0 until 10).map {
			(Random.nextFloat() * 2 - 1f)
		}
		val randomFlats = (10 until totalGeneCount).map {
			(Random.nextFloat() * 2 - 1f) / totalGeneCount
		}
		return (randomPeaks + randomFlats).shuffled()
	}
}


class CappedDistanceVelocityPolicy(private val distance: Float = 10f) : VelocityPolicy {
	private val fraction = 0.20

	override fun move(mod: ModelGenes) {
		val totalGeneCount = PSOUtils.countModelGenes(mod)
		val moveVector = produceMoveVector(totalGeneCount)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] = (weightGenes.genes[it] + subVector[it]) * 0.99f
				}
				processed += weightGenes.size

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}

	private fun produceMoveVector(totalGeneCount: Int): List<Float> {
		val sqrtR = (totalGeneCount.toDouble() * fraction).roundUpInt()

		val randomPeaks = (0 until sqrtR).map {
			(Random.nextFloat() * 2 - 1f)
		}
		val randomFlats = (sqrtR until totalGeneCount).map {
			(Random.nextFloat() * 2 - 1f) / totalGeneCount
		}
		val distributionArray = (randomPeaks + randomFlats).shuffled()
		val distSum = distributionArray.sum()
		val positionDeltaArray = distributionArray.map {
			max(min(distance * (it / distSum), 1f), -1f)
		}
		return positionDeltaArray
	}
}