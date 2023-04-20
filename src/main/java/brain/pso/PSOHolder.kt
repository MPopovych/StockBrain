package brain.pso

import brain.ga.weights.ModelGenes
import brain.models.Model

class PSOModel(
	val ordinal: Int,
	var geneBuffer: ModelGenes,
	val modelBuffer: Model,
)

class PSOHolder(
	val ordinal: Int,
	var current: PSOScore,
	var best: PSOScore,
	val modelBuffer: Model,
)

class PSOScore(
	val score: Double,
	val genes: ModelGenes
) {
	companion object {
		val NULL = PSOScore(0.0, ModelGenes(0, emptyMap(), "", ""))
	}
}