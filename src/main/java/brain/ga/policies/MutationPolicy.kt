package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.suppliers.Suppliers
import brain.utils.roundUpInt
import brain.utils.upscale
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

interface MutationPolicy {
	fun mutation(source: LayerGenes, destination: LayerGenes) {
		for (weight in source.map) {
			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
			sourceW.copyTo(destinationW)
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
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0)

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundToInt()
		if (countToMutateDouble >= 1) {
			for (i in 0 until countToMutate) {
				destination.genes[indices.random()] += supplyNext(destination.size)
			}
		} else {
			if (Random.nextFloat() < countToMutateDouble) {
				destination.genes[indices.random()] += supplyNext(destination.size)
			}
		}
	}
}

open class ReplaceMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomRangeNP
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0)

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundToInt()
		if (countToMutateDouble >= 1) {
			for (i in 0 until countToMutate) {
				destination.genes[indices.random()] = supplyNext(destination.size)
			}
		} else {
			if (Random.nextFloat() < countToMutateDouble) {
				destination.genes[indices.random()] = supplyNext(destination.size)
			}
		}
	}
}

open class CopyMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundToInt()
		if (countToMutateDouble >= 1) {
			for (i in 0 until countToMutate) {
				val randomPosA = indices.random()
				val randomPosB = indices.random()
				destination.genes[randomPosA] = source.genes[randomPosB]
			}
		} else {
			if (Random.nextFloat() < countToMutateDouble) {
				val randomPosA = indices.random()
				val randomPosB = indices.random()
				destination.genes[randomPosA] = source.genes[randomPosB]
			}
		}
	}
}

open class UpscaleMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
	) {
		val indices = source.genes.indices
		val countToMutateDouble = min((source.size.toDouble() * fraction), source.size.toDouble())
		val countToMutate = countToMutateDouble.roundToInt()
		if (countToMutateDouble >= 1) {
			for (i in 0 until countToMutate) {
				val randomIndex = indices.random()
				if (Random.nextBoolean()) {
					destination.genes[randomIndex] = source.genes[randomIndex] * 1.1f
				} else {
					destination.genes[randomIndex] = source.genes[randomIndex] / 1.1f
				}
			}
		} else {
			if (Random.nextFloat() < countToMutateDouble) {
				val randomIndex = indices.random()
				if (Random.nextBoolean()) {
					destination.genes[randomIndex] = source.genes[randomIndex] * 1.1f
				} else {
					destination.genes[randomIndex] = source.genes[randomIndex] / 1.1f
				}
			}
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
		val countToMutate = countToMutateDouble.roundToInt()
		if (countToMutateDouble >= 1) {
			for (i in 0 until countToMutate) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = -source.genes[randomIndex]
			}
		} else {
			if (Random.nextFloat() < countToMutateDouble) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = -source.genes[randomIndex]
			}
		}
	}
}

class CyclicMutationPolicy(
	private val fraction: Double = 0.01, // from 1.0 to 0.0
	private val additiveRatio: Int = 4,
	private val upscaleRatio: Int = 1,
	private val inversionRatio: Int = 1,
	private val copyRatio: Int = 2,
	private val replaceRatio: Int = 4,
) : MutationPolicy {

	private val sum = additiveRatio + upscaleRatio + inversionRatio + copyRatio + replaceRatio
	private val additive = AdditiveMutationPolicy(fraction)
	private val upscale = UpscaleMutationPolicy(fraction)
	private val inversion = InversionMutationPolicy(fraction)
	private val copy = CopyMutationPolicy(fraction)
	private val replace = ReplaceMutationPolicy(fraction)

	init {
		if (sum <= 0) throw IllegalStateException("Sum of rations should bot be zero or less")
	}

	override fun mutation(source: LayerGenes, destination: LayerGenes) {
		for (weight in source.map) {
			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
			sourceW.copyTo(destinationW)
			mutateWeight(sourceW, destinationW)
		}
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
		} else if (r < additiveRatio + upscaleRatio + inversionRatio + copyRatio) {
			copy.mutateWeight(source, destination)
		} else if (r < sum) {
			replace.mutateWeight(source, destination)
		} else {
			throw IllegalStateException()
		}
	}
}