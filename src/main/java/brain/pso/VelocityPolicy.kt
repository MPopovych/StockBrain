package brain.pso

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

interface VelocityPolicy {

	companion object {
		val STD = DeviationVelocityPolicy()
	}

	fun move(mod: LayerGenes) {
		val totalGeneCount = mod.map.values.sumOf { it.size }
		for (weight in mod.map) {
			val w = mod.map[weight.key] ?: throw IllegalStateException()
			moveWeight(mod = w, totalGeneCount = totalGeneCount)
		}
	}

	fun moveWeight(
		mod: WeightGenes,
		totalGeneCount: Int,
	)
}

class DeviationVelocityPolicy : VelocityPolicy {
	override fun moveWeight(mod: WeightGenes, totalGeneCount: Int) {
//		val sqrt = sqrt(mod.size.toFloat())
//		val velArray = FloatArray(mod.size) { (Random.nextFloat() * 2 - 1f) * sqrt / (Random.nextInt(mod.size) + 1) }

		val sqrtR = sqrt(totalGeneCount.toFloat()).roundToInt()
		val velArray = FloatArray(mod.size) {
			if (Random.nextInt(sqrtR + 2) == 0) {
				Random.nextFloat() * 2 - 1f
			} else {
				(Random.nextFloat() * 2 - 1f) / totalGeneCount
			}
		}

		if (velArray.any { !it.isFinite() }) throw IllegalStateException()
		mod.genes.indices.forEach {
			mod.genes[it] += velArray[it]
		}
	}
}