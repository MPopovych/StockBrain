package brain.ga

import brain.ga.weights.ModelGenes

data class GAScoreHolder(
	val id: String,
	val score: Double,
	val genes: ModelGenes,
) {
	val idHashcode = id.hashCode()

	val bornOnEpoch: Int
		get() = genes.bornOnEpoch

	val chromosomeHash: String
		get() = genes.chromosome

	var isOutDated = false
		private set

	fun copyGene() = genes.copy()

	fun markOutdated() {
		isOutDated = true
	}
}