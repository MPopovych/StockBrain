package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.suppliers.Suppliers
import brain.utils.roundUpInt
import brain.utils.upscale
import kotlin.math.max
import kotlin.math.min

interface MutationPolicy {
	fun mutation(source: LayerGenes, destination: LayerGenes) {
		for (weight in source.map) {
			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
			mutateWeight(source = sourceW, destination = destinationW)
		}
	}

	fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	)
}

open class AdditiveMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomRangeNP
	private fun supplyNext() = randomRangeSupplier.supply(0, 0)

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val size = max(min((source.size * fraction).roundUpInt(), source.size), 1)
		for (i in 0 until size) {
			destination.genes[indices.random()] += supplyNext()
		}
	}
}

open class UpscaleMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val size = max(min((source.size * fraction).roundUpInt(), source.size), 1)
		for (i in 0 until size) {
			val randomIndex = indices.random()
			destination.genes[randomIndex] = destination.genes[randomIndex].upscale(4)
		}
	}
}


open class InversionMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val size = max(min((source.size * fraction).roundUpInt(), source.size), 1)
		for (i in 0 until size) {
			val randomIndex = indices.random()
			destination.genes[randomIndex] = -destination.genes[randomIndex]
		}
	}
}

class CyclicMutationPolicy(
	fraction: Double = 0.01,
	private val additiveRatio: Int = 8,
	private val upscaleRatio: Int = 1,
	inversionRatio: Int = 1,
) : MutationPolicy {

	private val sum = additiveRatio + upscaleRatio + inversionRatio
	private val additive = AdditiveMutationPolicy(fraction)
	private val upscale = UpscaleMutationPolicy(fraction)
	private val inversion = InversionMutationPolicy(fraction)

	init {
		if (sum <= 0) throw IllegalStateException("Sum of rations should bot be zero or less")
	}

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		val r = (0 until sum).random()
		if (r < additiveRatio) {
			additive.mutateWeight(source, destination)
		} else if (r < additiveRatio + upscaleRatio) {
			upscale.mutateWeight(source, destination)
		} else {
			inversion.mutateWeight(source, destination)
		}
	}
}