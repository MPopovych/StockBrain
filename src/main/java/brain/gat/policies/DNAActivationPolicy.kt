package brain.gat.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes

object DNAActivationPolicy {

	fun activation(parentAGenes: ModelGenes, parentBGenes: ModelGenes): ModelGenes {
		val mixedLayers = parentAGenes.layers.mapValues { (layerKey, layerA) ->
			val layerB = parentBGenes.layers[layerKey] ?: throw IllegalStateException()

			val mixedWeights = layerA.map.mapValues w@{ weight ->
				val aGenes = weight.value
				val bGenes = layerB.map[weight.key] ?: throw IllegalStateException()

				val randomPoint = (0 until aGenes.size).random()
				val mix = FloatArray(aGenes.size) { ord ->
					if (ord < randomPoint) {
						aGenes.genes[ord]
					} else {
						bGenes.genes[ord]
					}
				}

				return@w WeightGenes(
					aGenes.weightName,
					mix,
					aGenes.width,
					aGenes.height,
					aGenes.callOrder
				)
			}
			return@mapValues LayerGenes(layerKey, mixedWeights, layerA.depth)
		}
		return ModelGenes(mixedLayers)
	}

}