package brain.pso

import brain.ga.weights.ModelGenes
import brain.layers.WeightData
import brain.matrix.Matrix
import brain.models.Model
import brain.suppliers.Suppliers
import kotlin.random.Random

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
) {
	val velocity: PSOVelocity = PSOVelocity.randomOnModel(modelBuffer)
}

class PSOScore(
	val score: Double,
	val genes: ModelGenes
) {
	companion object {
		val NULL = PSOScore(0.0, ModelGenes(0, emptyMap(), "", ""))
	}
}

class PSOVelocity(
	val layerAndWeightMap: Map<String, Map<String, FloatArray>>
) {
	companion object {
		fun randomOnModel(model: Model): PSOVelocity {
			return PSOVelocity(
				model.graphMap.mapValues { gln ->
					gln.value.layer.weights.mapValues sub@{ w ->
						val size = w.value.matrix.width * w.value.matrix.height
						val r = FloatArray(size) {
							Random.nextFloat() / (size + 10) // const to lesser influence on narrow weights
						}
//						Suppliers.RandomHE.fill(r)
						return@sub r
					}
				}
			)
		}
	}
}