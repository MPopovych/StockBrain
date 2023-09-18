package brain.gat.policies

import brain.gat.context.GATSettings
import brain.genes.ModelGenes
import brain.genes.WeightGenes
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object DNAMutationPolicy {

	private val nativeRandom = java.util.Random()
	private fun random() = nativeRandom.nextFloat() * 2f - 1f

	fun mutate(genes: ModelGenes, settings: GATSettings, initial: Boolean): ModelGenes {
		val mutateRate = if (initial) settings.initialMutationRate else settings.mutationRate
		val mixedLayers = genes.layerByWeightMap.mapValues { (layerKey, layerA) ->
			val mixedWeights = layerA.mapValues w@{ weight ->
				val aGenes = weight.value

				if (!aGenes.trainable) {
					return@w aGenes.copy()
				}

//				val g = 1f / (aGenes.width + (if (aGenes.height == 1) aGenes.width else aGenes.height))
				val g = sqrt(6f / (aGenes.width + (if (aGenes.height == 1) aGenes.width else aGenes.height)))
				val mix = FloatArray(aGenes.size) { ord ->
					val current = aGenes.genes[ord]
					val final = if (nativeRandom.nextFloat() < mutateRate) {
						val new = random() * g * settings.weightMod
						current + new
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

		val mutated = ModelGenes(mixedLayers)
		return DNANormPolicy.norm(mutated, settings, initial)
	}

}