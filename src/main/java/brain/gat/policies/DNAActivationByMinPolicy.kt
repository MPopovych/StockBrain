package brain.gat.policies

import brain.genes.ModelGenes
import brain.genes.WeightGenes
import kotlin.math.abs

object DNAActivationByMinPolicy {

	fun activation(parentAGenes: ModelGenes, parentBGenes: ModelGenes): ModelGenes {
		val mixedLayers = parentAGenes.layerByWeightMap.mapValues { (layerKey, layerA) ->
			val layerB = parentBGenes.layerByWeightMap[layerKey] ?: throw IllegalStateException()

			val mixedWeights = layerA.mapValues w@{ weight ->
				val aGenes = weight.value

				if (!aGenes.trainable) {
					return@w aGenes.copy()
				}

				val bGenes = layerB[weight.key] ?: throw IllegalStateException()

				val mix = FloatArray(aGenes.size) { ord ->
					if (abs(aGenes.genes[ord]) < abs(bGenes.genes[ord])) {
						aGenes.genes[ord]
					} else {
						bGenes.genes[ord]
					}
				}

				return@w WeightGenes(
					aGenes.width,
					aGenes.height,
					mix,
					trainable = true
				)
			}
			return@mapValues mixedWeights
		}
		return ModelGenes(mixedLayers)
	}

}