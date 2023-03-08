package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.suppliers.Suppliers
import brain.utils.roundUpInt
import brain.utils.upscale
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
	private val randomRangeSupplier = Suppliers.RandomHE
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0)

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundUpInt()
		for (i in 0 until countToMutate) {
			destination.genes[indices.random()] += supplyNext(destination.size)
		}
	}
}

open class ReplaceMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomRangeNP
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0) * 2

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val rounded = countToMutateDouble.roundUpInt()
		for (i in 0 until rounded) {
			destination.genes[indices.random()] = supplyNext(rounded)
		}
	}
}

open class SwapMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		if (source != destination) {
			source.copyTo(destination)
		}
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val rounded = countToMutateDouble.roundUpInt()
		for (i in 0 until rounded) {
			val randomPosA = indices.random()
			val randomPosB = indices.random()
			destination.genes[randomPosA] = source.genes[randomPosB]
			destination.genes[randomPosB] = source.genes[randomPosA]
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
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundUpInt()
		for (i in 0 until countToMutate) {
			val randomIndex = indices.random()
			destination.genes[randomIndex] = destination.genes[randomIndex].upscale(2)
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
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundUpInt()
		for (i in 0 until countToMutate) {
			val randomIndex = indices.random()
			destination.genes[randomIndex] = -destination.genes[randomIndex]
		}
	}
}

class CyclicMutationPolicy(
	private val fraction: Double = 0.01, // from 1.0 to 0.0
	private val additiveRatio: Int = 4,
	private val upscaleRatio: Int = 1,
	private val inversionRatio: Int = 1,
	private val swapRatio: Int = 2,
	private val replaceRatio: Int = 2,
) : MutationPolicy {

	private val sum = additiveRatio + upscaleRatio + inversionRatio + swapRatio + replaceRatio
	private val additive = AdditiveMutationPolicy(fraction)
	private val upscale = UpscaleMutationPolicy(fraction)
	private val inversion = InversionMutationPolicy(fraction)
	private val swap = SwapMutationPolicy(fraction)
	private val replace = ReplaceMutationPolicy(fraction)

	init {
		if (sum <= 0) throw IllegalStateException("Sum of rations should bot be zero or less")
	}

	override fun mutation(source: LayerGenes, destination: LayerGenes) {
		val takeCount = (source.map.size * fraction).roundUpInt()
		val random = source.map.values.shuffled()

		random.take(takeCount).forEach {
			val sourceW = source.map[it.weightName] ?: throw IllegalStateException()
			val destinationW = destination.map[it.weightName] ?: throw IllegalStateException()
			mutateWeight(sourceW, destinationW)
		}
		random.drop(takeCount).forEach {
			val sourceW = source.map[it.weightName] ?: throw IllegalStateException()
			val destinationW = destination.map[it.weightName] ?: throw IllegalStateException()
			sourceW.copyTo(destinationW)
		}

//		for (weight in source.map) {
//			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
//			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
//
//			if (random == sourceW) {
//				mutateWeight(sourceW, destinationW)
//			} else {
//				sourceW.copyTo(destinationW)
//			}
//		}
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
		} else if (r < additiveRatio + upscaleRatio + inversionRatio){
			inversion.mutateWeight(source, destination)
		} else if (r < additiveRatio + upscaleRatio + inversionRatio + swapRatio) {
			swap.mutateWeight(source, destination)
		} else {
			replace.mutateWeight(source, destination)
		}
	}
}