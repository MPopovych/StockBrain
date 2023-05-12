package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes
import utils.ext.std
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface WeightOptPolicy {

	companion object {
		val NONE = NoneOptPolicy()
		val NOISE = NoiseOptPolicy(0.01f)
	}

	fun optimise(destination: ModelGenes) {
		for (layer in destination.layers.values) {
			optimise(layer)
		}
	}

	fun optimise(destination: LayerGenes) {
		for (weight in destination.map.values) {
			optimise(weight)
		}
	}

	fun optimise(
		destination: WeightGenes,
	)
}

class NoneOptPolicy: WeightOptPolicy {
	override fun optimise(destination: ModelGenes) {
	}
	override fun optimise(destination: LayerGenes) {
	}
	override fun optimise(destination: WeightGenes) {
	}
}

open class NoiseOptPolicy(private val fraction: Float = 0.01f) : WeightOptPolicy {
	override fun optimise(destination: WeightGenes) {
		val std = destination.genes.std()
		destination.genes.indices.forEach {
			destination.genes[it] += (Random.nextFloat() * 2 - 1f) * std * fraction
		}
	}
}

open class DiscreteOptPolicy(private val fraction: Float = 0.03f) : WeightOptPolicy {
	override fun optimise(destination: WeightGenes) {
		val chunkRanges = destination.genes.sorted().chunked(3).map {
			val f = it.first()
			val l = it.last()
			val m = (f + l) / 2
			Triple(f, l, m)
		}
		destination.genes.indices.forEach {
			val v = destination.genes[it]
			val chunk = chunkRanges.find { chunk -> v >= chunk.first && v <= chunk.second } ?: return@forEach
			destination.genes[it] = v * (1 - fraction) + chunk.third * fraction
		}
	}
}

open class OutlierOptPolicy(private val fraction: Float = 0.2f) : WeightOptPolicy {
	override fun optimise(destination: WeightGenes) {
		val std = destination.genes.map { abs(it) }.std()
		val avg = destination.genes.map { abs(it) }.average().toFloat()
		val capH = avg + std + (Random.nextFloat() * 2 - 1f) * (1 + fraction)
		val capL = avg - std + (Random.nextFloat() * 2 - 1f) * (1 + fraction)

		destination.genes.indices.forEach {
			val v = destination.genes[it]
			if (v == 0.0f) return@forEach
			val absV = abs(v)
			val sign = if (v >= 0) 1 else -1
			destination.genes[it] = (max(min(absV, capH), capL) * fraction + absV * (1 - fraction)) * sign
		}
	}
}