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
	fun mutation(source: LayerGenes, destination: LayerGenes, totalGeneCount: Int) {
		for (weight in source.map) {
			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
			sourceW.copyTo(destinationW)
			mutateWeight(source = sourceW, destination = destinationW, totalGeneCount = totalGeneCount)
		}
	}

	fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	)
}

open class AdditiveMutationPolicy(private val fraction: Double = 0.01, private val mod: Float = 1.0f) : MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomHE
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0) * mod

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			if (Random.nextFloat() <= fraction) {
				val rIndex = indices.random()
				destination.genes[rIndex] = source.genes[rIndex] + supplyNext(destination.size)
			}
		}
	}
}

open class ZeroAllMutationPolicy(private val fraction: Float = 0.01f) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			destination.genes[i] = source.genes[i] * fraction
		}
	}
}

open class ReplaceMutationPolicy(private val fraction: Double = 0.01, private val mod: Float = 1f) : MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomRangeNP
	private fun supplyNext(count: Int) = randomRangeSupplier.supply(count, 0, 0) * mod

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			if (Random.nextFloat() <= fraction) {
				destination.genes[indices.random()] = supplyNext(destination.size)
			}
		}
	}
}

open class CopyMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			if (Random.nextFloat() <= fraction) {
				destination.genes[indices.random()] = source.genes[indices.random()]
			}
		}
	}
}

open class UpscaleMutationPolicy(private val fraction: Double = 0.01) : MutationPolicy {
	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			if (Random.nextFloat() <= fraction) {
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
		totalGeneCount: Int,
	) {
		val indices = source.genes.indices
		for (i in indices) {
			val randomIndex = indices.random()
			if (Random.nextFloat() <= fraction) {
				destination.genes[randomIndex] = -source.genes[randomIndex]
			}
		}
	}
}

class CyclicMutationPolicy(
	private val fraction: Double = 0.01, // from 1.0 to 0.0
	private val additiveRatio: Int = 1,
	private val upscaleRatio: Int = 0,
	private val inversionRatio: Int = 0,
	private val copyRatio: Int = 0,
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

	override fun mutation(source: LayerGenes, destination: LayerGenes, totalGeneCount: Int) {
		for (weight in source.map) {
			val sourceW = source.map[weight.key] ?: throw IllegalStateException()
			val destinationW = destination.map[weight.key] ?: throw IllegalStateException()
			sourceW.copyTo(destinationW)
			mutateWeight(sourceW, destinationW, totalGeneCount)
		}
	}

	override fun mutateWeight(
		source: WeightGenes,
		destination: WeightGenes,
		totalGeneCount: Int,
	) {
		val r = (0 until sum).random()
		if (r < additiveRatio) {
			additive.mutateWeight(source, destination, totalGeneCount)
		} else if (r < additiveRatio + upscaleRatio) {
			upscale.mutateWeight(source, destination, totalGeneCount)
		} else if (r < additiveRatio + upscaleRatio + inversionRatio){
			inversion.mutateWeight(source, destination, totalGeneCount)
		} else if (r < additiveRatio + upscaleRatio + inversionRatio + copyRatio) {
			copy.mutateWeight(source, destination, totalGeneCount)
		} else if (r < sum) {
			replace.mutateWeight(source, destination, totalGeneCount)
		} else {
			throw IllegalStateException()
		}
	}
}