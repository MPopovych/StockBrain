package ga

import ga.weights.ModelGenes

data class GAScoreHolder(
	val id: String,
	val score: Double,
	val genes: ModelGenes
) {

	val chromosomeHash: String
		get() = genes.chromosome

	fun copyGene() = genes.copy()
}