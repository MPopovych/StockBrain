package brain.pso

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.utils.printCyanBr
import brain.utils.printYellowBr
import brain.utils.roundToDec
import kotlin.math.*
import kotlin.random.Random

interface ApproachPolicy {

	companion object {
		val OneTenth = ConstApproachPolicy(0.1f)
		val OneFifth = ConstApproachPolicy(0.2f)
		val OneThird = ConstApproachPolicy(0.3f)
		val TwoThird = ConstApproachPolicy(0.6f)
		val Distance = DistanceApproachPolicy()
	}

	fun approach(fromMod: LayerGenes,
	             toRef: LayerGenes,) {
		if (fromMod.map.isEmpty()) return

		for (weight in fromMod.map) {
			val wFromMod = fromMod.map[weight.key] ?: throw IllegalStateException()
			val wToRef = toRef.map[weight.key] ?: throw IllegalStateException()
			approachWeight(fromMod = wFromMod, toRef = wToRef, 0f)
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


class DistanceApproachPolicy(private val maxDistance: Float = 10f): ApproachPolicy {
	override fun approach(fromMod: LayerGenes,
	                      toRef: LayerGenes,) {
		if (fromMod.map.isEmpty()) return

		val sumD = fromMod.map.map {
			val toW = toRef.map[it.key] ?: throw IllegalStateException()
			getDistanceSquared(it.value, toW)
		}.sum()

		val progress = (maxDistance / (sqrt(sumD) + maxDistance)) + 0.1f // shift by 10% to overshoot and ensure movement

//		printCyanBr("total distance is: ${sqrt(sumD)}, progress: $progress")

		for (weight in fromMod.map) {
			val wFromMod = fromMod.map[weight.key] ?: throw IllegalStateException()
			val wToRef = toRef.map[weight.key] ?: throw IllegalStateException()
			approachWeight(fromMod = wFromMod, toRef = wToRef, progress)
		}
	}

	private fun getDistanceSquared(fromMod: WeightGenes, toRef: WeightGenes): Float {
		return fromMod.genes.mapIndexed { index, fl ->
			(toRef.genes[index] - fl).pow(2)
		}.sum()
	}

	override fun approachWeight(fromMod: WeightGenes, toRef: WeightGenes, progress: Float) {
		fromMod.genes.indices.forEach {
			val newValue = fromMod.genes[it] * (1 - progress) + toRef.genes[it] * progress
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}
