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
) {
	val velocity: PSOVelocity = PSOVelocity.randomOnModel(modelBuffer)
}

class PSOScore(
	val score: Double,
	val genes: ModelGenes
) {
	companion object {
		val NULL = PSOScore(0.0, ModelGenes(emptyMap()))
	}
}

class PSOVelocity(
	val layerAndWeightMap: Map<String, Map<String, FloatArray>>
) {
	companion object {
		fun getRandomVelocity(size: Int): Float {
			return jRandom.nextFloat() * 2 - 1f
		}

		private val jRandom = java.util.Random()
		fun randomOnModel(model: Model): PSOVelocity {
			return PSOVelocity(
				model.graphMap.mapValues { gln ->
					gln.value.layer.weights.mapValues sub@{ w ->
						val size = w.value.matrix.width * w.value.matrix.height
						val r = FloatArray(size) {
							getRandomVelocity(size)
						}
						return@sub r
					}
				}
			)
		}
	}
}