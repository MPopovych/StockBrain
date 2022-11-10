package ga.policies

import ga.weights.LayerGenes
import ga.weights.WeightGenes
import suppliers.Suppliers
import utils.roundUpInt
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

	fun mutateWeight(source: WeightGenes, destination: WeightGenes)
}

class AdditiveMutationPolicy(private val fraction: Double = 0.01): MutationPolicy {
	private val randomRangeSupplier = Suppliers.RandomRangeNP
	private fun supplyNext() = randomRangeSupplier.supply(0, 0)

	override fun mutateWeight(source: WeightGenes, destination: WeightGenes) {
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