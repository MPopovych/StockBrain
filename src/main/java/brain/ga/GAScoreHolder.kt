package brain.ga

import brain.ga.weights.ModelGenes

data class GAScoreHolder(
	val id: String,
	val score: Double,
	val genes: ModelGenes,
	val bornOnEpoch: Int,
	val parentA: String,
	val parentB: String,
) {

	var isOutDated = false
		private set

	fun copyGene() = genes.copy()

	fun markOutdated() {
		isOutDated = true
	}
}