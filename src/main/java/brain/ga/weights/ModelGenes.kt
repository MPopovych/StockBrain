package brain.ga.weights

import brain.ga.policies.CrossOverPolicy
import brain.ga.policies.MutationPolicy
import brain.models.Model
import brain.pso.ApproachPolicy
import brain.pso.VelocityPolicy
import brain.utils.encodeGenes

// a hard copy of weights
class ModelGenes(
	var bornOnEpoch: Int,
	val layers: Map<String, LayerGenes>,
	val parentAId: String,
	val parentBId: String,
) {
	companion object {
		operator fun invoke(bornOnEpoch: Int, model: Model, parentAId: String, parentBId: String): ModelGenes {
			val weights: Map<String, LayerGenes> = model.graphMap.values
				.map {
					val map = it.layer.weights.values
						.map { w ->
							WeightGenes(
								w.name,
								w.matrix.readFloatData()
							)
						}
						.associateBy { w -> w.weightName }
					return@map LayerGenes(it.layer.name, map)
				}.associateBy { it.layerId }

			return ModelGenes(bornOnEpoch, weights, parentAId, parentBId)
		}
	}

	val chromosome: String
		get() = layers.values.joinToString(" ") { it.chromosome }

	val geneCount = layers.values.sumOf { l -> l.map.values.sumOf { w -> w.size } }

	// used for cleared up destinations
	fun applyToModel(model: Model) {
		model.graphMap.forEach { (id, layer) ->
			val layerGenes = layers[id] ?: throw IllegalStateException("No layer $id in genes")
			layerGenes.map.forEach { (wId, wGene) ->
				val weight = layer.layer.weights[wId] ?: throw IllegalStateException("No layer $id in weights")
				weight.matrix.writeFloatData(wGene.genes)
			}
		}
	}

	fun copy(): ModelGenes {
		val wCopy = layers.mapValues { it.value.copy() }
		return ModelGenes(-1, wCopy, parentAId, parentBId)
	}

	fun copyWithParents(parentAId: String, parentBId: String): ModelGenes {
		val wCopy = layers.mapValues { it.value.copy() }
		return ModelGenes(-1, wCopy, parentAId, parentBId)
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
			mutationPolicy.mutation(source = sourceLayer, destination = layer, totalGeneCount = geneCount)
		}
		return this
	}

	fun applyVelocityPolicy(velocityPolicy: VelocityPolicy): ModelGenes {
		layers.forEach { (s, layer) ->
			velocityPolicy.move(mod = layer, totalGeneCount = geneCount)
		}
		return this
	}

	fun applyApproachPolicy(
		approachPolicy: ApproachPolicy,
		ownScore: Float,
		destination: ModelGenes,
		destinationScore: Float,
		globalDeviation: Float,
	): ModelGenes {
		layers.forEach { (s, layer) ->
			val destinationLayer = destination.layers[s] ?: throw IllegalStateException("no layer at: $s")
			approachPolicy.approach(
				fromMod = layer,
				fromScore = ownScore,
				toRef = destinationLayer,
				toScore = destinationScore,
				globalDeviation = globalDeviation
			)
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