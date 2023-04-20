package brain.pso

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.utils.printYellowBr
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface ApproachPolicy {

	companion object {
		val OneTenth = ConstApproachPolicy(0.1f)
		val OneFifth = ConstApproachPolicy(0.2f)
		val OneThird = ConstApproachPolicy(0.3f)
	}

	fun approach(fromMod: LayerGenes,
	             fromScore: Float,
	             toRef: LayerGenes,
	             toScore: Float,
	             globalDeviation: Float) {
		for (weight in fromMod.map) {
			val wFromMod = fromMod.map[weight.key] ?: throw IllegalStateException()
			val wToRef = toRef.map[weight.key] ?: throw IllegalStateException()
			approachWeight(fromMod = wFromMod, toRef = wToRef, globalDeviation)
		}
	}

	fun approachWeight(
		fromMod: WeightGenes,
		toRef: WeightGenes,
		progress: Float,
	)
}

class ConstApproachPolicy(val const: Float): ApproachPolicy {
	override fun approachWeight(fromMod: WeightGenes, toRef: WeightGenes, progress: Float) {
		fromMod.genes.indices.forEach {
			val newValue = fromMod.genes[it] * (1 - const) + toRef.genes[it] * const
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}
