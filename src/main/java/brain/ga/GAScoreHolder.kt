package brain.ga

import brain.ga.weights.ModelGenes

data class GAScoreHolder(
	val id: String,
	val score: Double,
	val genes: ModelGenes,
) {
	val idHashcode = id.hashCode()

	val chromosomeHash: String
		get() = genes.chromosome

	fun copyGene() = genes.copy()
}