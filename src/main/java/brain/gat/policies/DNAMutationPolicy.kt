package brain.gat.policies

import brain.gat.context.GATSettings
import brain.genes.ModelGenes
import brain.genes.WeightGenes
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object DNAMutationPolicy {

	private val nativeRandom = java.util.Random()
	private fun nextGaussRandom() = nativeRandom.nextGaussian().toFloat()

	fun mutate(genes: ModelGenes, settings: GATSettings, initial: Boolean): ModelGenes {
		val mutateRate = if (initial) settings.initialMutationRate else settings.mutationRate
		val mixedLayers = genes.layerByWeightMap.mapValues { (layerKey, layerA) ->
			val mixedWeights = layerA.mapValues w@{ weight ->
				val aGenes = weight.value

				if (!aGenes.trainable) {
					return@w aGenes.copy()
				}

				val mix = FloatArray(aGenes.size) { ord ->
					val current = aGenes.genes[ord]
					val final = if (nativeRandom.nextFloat() < mutateRate) {
						val new = nextGaussRandom() * settings.weightMod * sqrt(6f / aGenes.size)
						val newPreCalc = current + new
						val f = if (newPreCalc > settings.weightSoftCap || newPreCalc < -settings.weightSoftCap) {
							current + (new / 2)
						} else {
							newPreCalc
						}
						max(min(f, settings.weightHeavyCap), -settings.weightHeavyCap)
					} else {
						current // no modification
					}
					return@FloatArray max(min(final, settings.weightHeavyCap), -settings.weightHeavyCap)
				}
				return@w WeightGenes(
					aGenes.width,
					aGenes.height,
					mix,
					trainable = true,
				)
			}
			return@mapValues mixedWeights
		}
		return ModelGenes(mixedLayers)
	}

}