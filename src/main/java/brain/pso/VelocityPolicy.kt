package brain.pso

import brain.ga.weights.ModelGenes
import brain.utils.roundUpInt
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

interface VelocityPolicy {

	companion object {
		val SQRT_NOISE = SqrtNoiseVelocityPolicy()
		val Distance = NoiseVelocityPolicy()
	}

	fun move(mod: ModelGenes)

}

class SqrtNoiseVelocityPolicy : VelocityPolicy {

	override fun move(mod: ModelGenes) {
		val totalGeneCount = mod.layers.values.sumOf { it.map.values.sumOf { it.size } }
		val moveVector = produceMoveVector(totalGeneCount)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] += subVector[it]
				}
				processed += weightGenes.size

				if (weightGenes.genes.any { !it.isFinite() }) throw IllegalStateException()
			}
		}
	}

	private fun produceMoveVector(totalGeneCount: Int): List<Float> {
		val sqrtR = sqrt(totalGeneCount.toFloat()).roundToInt()
		return (0 until totalGeneCount).map {
			if (Random.nextInt(sqrtR + 2) == 0) {
				(Random.nextFloat() * 2 - 1f) * (sqrtR / totalGeneCount)
			} else {
				(Random.nextFloat() * 2 - 1f) / totalGeneCount
			}
		}
	}
}


class NoiseVelocityPolicy(private val distance: Float = 10f) : VelocityPolicy {
	private val fraction = 0.20

	override fun move(mod: ModelGenes) {
		val totalGeneCount = mod.layers.values.sumOf { it.map.values.sumOf { it.size } }
		val moveVector = produceMoveVector(totalGeneCount)

		var processed = 0
		for (layerGenes in mod.layers.values) {
			for (weightGenes in layerGenes.map.values) {
				val subVector = moveVector.subList(processed, processed + weightGenes.size)
				weightGenes.genes.indices.forEach {
					weightGenes.genes[it] += subVector[it]
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
		val randomFlats = (sqrtR until  totalGeneCount).map {
			(Random.nextFloat() * 2 - 1f) / totalGeneCount
		}
		val distributionArray = (randomPeaks + randomFlats).shuffled()
		val distSum = distributionArray.sum()
		val positionDeltaArray = distributionArray.map {
			distance * (it / distSum)
		}
		return positionDeltaArray
	}
}