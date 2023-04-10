package brain.ga

import brain.ga.weights.ModelGenes

data class GAScoreHolder(
	val id: String,
	val score: Double,
	val genes: ModelGenes,
) {

	val bornOnEpoch: Int
		get() = genes.bornOnEpoch

	var isOutDated = false
		private set

	fun copyGene() = genes.copy()
	fun copyWithParent(parentAId: String, parentBId: String) = genes.copyWithParents(parentAId, parentBId)

	fun markOutdated() {
		isOutDated = true
	}
}