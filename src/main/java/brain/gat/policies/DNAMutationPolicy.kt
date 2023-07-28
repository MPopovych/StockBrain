package brain.gat.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes
import brain.gat.context.GATSettings
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object DNAMutationPolicy {

	private val nativeRandom = java.util.Random()
	private fun nextGaussRandom() = nativeRandom.nextGaussian().toFloat()

	fun mutate(genes: ModelGenes, settings: GATSettings, initial: Boolean): ModelGenes {
		val mutateRate = if (initial) settings.initialMutationRate else settings.mutationRate
		val mixedLayers = genes.layers.mapValues { (layerKey, layerA) ->
			val mixedWeights = layerA.map.mapValues w@{ weight ->
				val aGenes = weight.value
				val mix = FloatArray(aGenes.size) { ord ->
					var current = aGenes.genes[ord]
					val final = if (nativeRandom.nextFloat() < mutateRate) {
						val new = nextGaussRandom() * settings.weightMod
						if (nativeRandom.nextBoolean()) {
							val newPreCalc = current + new
							if (newPreCalc > settings.weightSoftCap || newPreCalc < -settings.weightSoftCap) {
								current += (new / 2)
							} else {
								current = newPreCalc
							}
						} else {
							current = new
						}
						max(min(current, settings.weightHeavyCap), -settings.weightHeavyCap)
					} else {
						current // no modification
					}
					return@FloatArray final
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