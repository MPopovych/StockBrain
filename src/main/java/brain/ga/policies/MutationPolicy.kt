package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import brain.suppliers.Suppliers
import brain.utils.roundUpInt
import brain.utils.upscale
import kotlin.math.min
import kotlin.random.Random

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
		if (countToMutateDouble >= 1.0) {
			for (i in 0 until countToMutate) {
				destination.genes[indices.random()] += supplyNext(countToMutate)
			}
		} else {
			if (Random.nextDouble(0.0, 1.0) <= countToMutateDouble) {
				destination.genes[indices.random()] += supplyNext(countToMutate)
			}
		}
	}
}

open class ReplaceMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
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
		val rounded = countToMutateDouble.roundUpInt()
		if (countToMutateDouble >= 1.0) {
			for (i in 0 until rounded) {
				destination.genes[indices.random()] = supplyNext(rounded)
			}
		} else {
			if (Random.nextDouble(0.0, 1.0) <= countToMutateDouble) {
				destination.genes[indices.random()] = supplyNext(rounded)
			}
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
		if (countToMutateDouble >= 1.0) {
			val countToMutate = countToMutateDouble.roundUpInt()
			for (i in 0 until countToMutate) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = destination.genes[randomIndex].upscale(2)
			}
		} else {
			if (Random.nextDouble(0.0, 1.0) <= countToMutateDouble) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = destination.genes[randomIndex].upscale(2)
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
		if (countToMutateDouble >= 1.0) {
			val countToMutate = countToMutateDouble.roundUpInt()
			for (i in 0 until countToMutate) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = -destination.genes[randomIndex]
			}
		} else {
			if (Random.nextDouble(0.0, 1.0) <= countToMutateDouble) {
				val randomIndex = indices.random()
				destination.genes[randomIndex] = -destination.genes[randomIndex]
			}
		}
	}
}

class CyclicMutationPolicy(
	fraction: Double = 0.01, // from 1.0 to 0.0
	private val additiveRatio: Int = 6,
	private val upscaleRatio: Int = 1,
	private val inversionRatio: Int = 1,
	private val replaceRatio: Int = 3,
) : MutationPolicy {

	private val sum = additiveRatio + upscaleRatio + inversionRatio + replaceRatio
	private val additive = AdditiveMutationPolicy(fraction * (additiveRatio.toDouble() / sum))
	private val upscale = UpscaleMutationPolicy(fraction * (upscaleRatio.toDouble() / sum))
	private val inversion = InversionMutationPolicy(fraction * (inversionRatio.toDouble() / sum))
	private val replace = ReplaceMutationPolicy(fraction *  (replaceRatio.toDouble() / sum))

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
		} else if (r < additiveRatio + upscaleRatio + inversionRatio){
			inversion.mutateWeight(source, destination)
		} else {
			replace.mutateWeight(source, destination)
		}
	}
}