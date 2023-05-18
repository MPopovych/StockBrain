package brain.gat.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes
import brain.gat.context.GATSettings
import kotlin.random.Random

object DNAMutationPolicy {

	fun mutate(genes: ModelGenes, settings: GATSettings, initial: Boolean): ModelGenes {
		val mutateRate = if (initial) settings.initialMutationRate else settings.mutationRate
		val mixedLayers = genes.layers.mapValues { (layerKey, layerA) ->
			val mixedWeights = layerA.map.mapValues w@{ weight ->
				val aGenes = weight.value
				val mix = FloatArray(aGenes.size) { ord ->
					if (Random.nextFloat() < mutateRate) {
						val new = (Random.nextFloat() * 2f - 1f) * settings.weightCap
						if (new > settings.weightHeavy || new < -settings.weightHeavy) {
							(new + settings.weightHeavy) / 2
						} else {
							new
						}
					} else {
						aGenes.genes[ord]
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