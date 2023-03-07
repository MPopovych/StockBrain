package brain.ga.weights

import brain.ga.policies.CrossOverPolicy
import brain.ga.policies.MutationPolicy
import brain.models.Model
import brain.utils.encodeGenes

// a hard copy of weights
class ModelGenes(var bornOnEpoch: Int, val layers: Map<String, LayerGenes>) {
	companion object {
		operator fun invoke(bornOnEpoch: Int, model: Model): ModelGenes {
			val weights: Map<String, LayerGenes> = model.layersMap.values
				.map {
					val map = it.weights.values
						.mapNotNull { w ->
							if (!w.trainable) return@mapNotNull null
							return@mapNotNull WeightGenes(w.name,
								w.matrix.readFloatData())
						}
						.associateBy { w -> w.weightName }
					return@map LayerGenes(it.name, map)
				}.associateBy { it.layerId }

			return ModelGenes(bornOnEpoch, weights)
		}
	}

	val chromosome: String
		get() = layers.values.joinToString(" ") { it.chromosome }

	// used for cleared up destinations
	fun applyToModel(model: Model) {
		model.layersMap.forEach { (id, layer) ->
			val layerGenes = layers[id] ?: throw IllegalStateException("No layer $id in genes")
			layerGenes.map.forEach { (wId, wGene) ->
				val weight = layer.weights[wId] ?: throw IllegalStateException("No layer $id in weights")
				weight.matrix.writeFloatData(wGene.genes)
			}
		}
	}

	fun copy(): ModelGenes {
		val wCopy = layers.mapValues { it.value.copy() }
		return ModelGenes(bornOnEpoch, wCopy)
	}

	fun applyCrossOverPolicy(crossOverPolicy: CrossOverPolicy, a: ModelGenes, b: ModelGenes): ModelGenes {
		layers.forEach { (i, layer) ->
			val aL = a.layers[i] ?: throw IllegalStateException("no layer at A: $i")
			val aB = b.layers[i] ?: throw IllegalStateException("no layer at B: $i")
			crossOverPolicy.cross(aL, aB, destination = layer)
		}
		return this
	}

	fun applyMutationPolicy(mutationPolicy: MutationPolicy, source: ModelGenes): ModelGenes {
		layers.forEach { (i, layer) ->
			val sourceLayer = source.layers[i] ?: throw IllegalStateException("no layer at: $i")
			mutationPolicy.mutation(sourceLayer, destination = layer)
		}
		return this
	}

}

class LayerGenes(val layerId: String, val map: Map<String, WeightGenes>) {
	val chromosome
		get() = map.values.joinToString(" ") { weightGenes -> weightGenes.genes.encodeGenes() }

	fun copy(): LayerGenes {
		return LayerGenes(layerId, map.mapValues { it.value.copy() })
	}
}