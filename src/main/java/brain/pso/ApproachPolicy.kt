package brain.pso

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

interface ApproachPolicy {

	companion object {
		val Classic = ClassicApproachPolicy(0.6f)
		val OneTenth = ConstApproachPolicy(0.1f)
		val OneFifth = ConstApproachPolicy(0.2f)
		val OneThird = ConstApproachPolicy(0.3f)
		val TwoThird = ConstApproachPolicy(0.6f)
		val KeepDistance = DistanceApproachPolicy()
		val FastApproach = ProximityApproachPolicy()
	}

	fun approach(
		fromMod: ModelGenes,
		toRef: ModelGenes,
	) {
		fromMod.layers.forEach { (s, layer) ->
			val destinationLayer = toRef.layers[s] ?: throw IllegalStateException("no layer at: $s")
			approach(
				fromMod = layer,
				toRef = destinationLayer,
			)
		}
	}

	fun approach(
		fromMod: LayerGenes,
		toRef: LayerGenes,
	) {
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

class ConstApproachPolicy(val const: Float) : ApproachPolicy {
	override fun approachWeight(fromMod: WeightGenes, toRef: WeightGenes, progress: Float) {
		fromMod.genes.indices.forEach {
			val newValue = fromMod.genes[it] * (1 - const) + toRef.genes[it] * const
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}


class DistanceApproachPolicy(private val maxDistance: Float = 10f) : ApproachPolicy {
	override fun approach(
		fromMod: LayerGenes,
		toRef: LayerGenes,
	) {
		if (fromMod.map.isEmpty()) return

		val sumD = fromMod.map.map {
			val toW = toRef.map[it.key] ?: throw IllegalStateException()
			getDistanceSquared(it.value, toW)
		}.sum()

		val progress = (maxDistance / (sqrt(sumD) + maxDistance)) // shift by 10% to overshoot and ensure movement

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
			val newValue = fromMod.genes[it] * (1 - progress) + toRef.genes[it] * (progress + Random.nextFloat() / 10)
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}


class ProximityApproachPolicy(private val const: Float = 3f, private val cap: Float = 10f) : ApproachPolicy {

	override fun approach(fromMod: ModelGenes, toRef: ModelGenes) {
		val distance = min(PSOUtils.modelDistance(fromMod, toRef), cap)
		val progress = (Random.nextFloat() / 2 + 0.25f) * (1 + distance) / (const + distance)
//		val progress = (1 + distance) / (const + distance)
		fromMod.layers.forEach { (s, layer) ->
			val destinationLayer = toRef.layers[s] ?: throw IllegalStateException("no layer at: $s")

			if (layer.map.isEmpty()) return@forEach

			for (weight in layer.map) {
				val wFromMod = layer.map[weight.key] ?: throw IllegalStateException()
				val wToRef = destinationLayer.map[weight.key] ?: throw IllegalStateException()
				approachWeight(fromMod = wFromMod, toRef = wToRef, progress)
			}
		}
	}

	override fun approach(
		fromMod: LayerGenes,
		toRef: LayerGenes,
	) {
		throw IllegalStateException()
	}

	override fun approachWeight(fromMod: WeightGenes, toRef: WeightGenes, progress: Float) {
		fromMod.genes.indices.forEach {
			val newValue = fromMod.genes[it] * (1 - progress) + toRef.genes[it] * (progress + Random.nextFloat() / 10)
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}

class ClassicApproachPolicy(private val const: Float = 0.6f) : ApproachPolicy {

	override fun approachWeight(fromMod: WeightGenes, toRef: WeightGenes, progress: Float) {
		fromMod.genes.indices.forEach {
			val progressR = Random.nextFloat() * const
			val newValue = fromMod.genes[it] * (1 - progressR) + toRef.genes[it] * progressR
			fromMod.genes[it] = newValue
			if (!newValue.isFinite()) throw IllegalStateException()
		}
	}
}
