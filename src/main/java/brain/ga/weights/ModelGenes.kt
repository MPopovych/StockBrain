package brain.ga.weights

import brain.ga.policies.CrossOverPolicy
import brain.ga.policies.MutationPolicy
import brain.models.Model
import brain.pso.ApproachPolicy
import brain.pso.PolicyContext
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
				.mapIndexed { callOrder, graphLayerNode ->
					val map = graphLayerNode.layer.weights.values
						.filter { w -> w.trainable }
						.map { w ->
							WeightGenes(
								w.name,
								w.matrix.readFloatData(),
								w.matrix.width,
								w.matrix.height,
								callOrder
							)
						}
						.associateBy { w -> w.weightName }
					return@mapIndexed LayerGenes(graphLayerNode.layer.name, map)
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
			layer.layer.weights.filter { it.value.trainable }.forEach { (key, weight) ->
				val weightGenes = layerGenes.map[key] ?: throw IllegalStateException("No layer $id in weights")
				weight.matrix.writeFloatData(weightGenes.genes)
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

	fun applyVelocityPolicy(velocityPolicy: VelocityPolicy, context: PolicyContext): ModelGenes {
		velocityPolicy.move(this, context)
//		layers.forEach { (s, layer) ->
//			velocityPolicy.move(mod = layer, totalGeneCount = geneCount)
//		}
		return this
	}

	fun applyApproachPolicy(
		approachPolicy: ApproachPolicy,
		destination: ModelGenes,
	): ModelGenes {
		approachPolicy.approach(this, destination)
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