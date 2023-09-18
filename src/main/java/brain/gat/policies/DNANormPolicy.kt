package brain.gat.policies

import brain.gat.context.GATSettings
import brain.genes.ModelGenes
import brain.genes.WeightGenes
import utils.ext.std
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object DNANormPolicy {

	private val nativeRandom = java.util.Random()
	private fun random() = nativeRandom.nextFloat() * 2f - 1f

	fun norm(genes: ModelGenes, settings: GATSettings, initial: Boolean): ModelGenes {
		val m = settings.normMomentum
		val mD = 1f - m
		val disabled = m == 0f || initial

		val mixedLayers = genes.layerByWeightMap.mapValues { (layerKey, layerA) ->
			val mixedWeights = layerA.mapValues w@{ weight ->
				val aGenes = weight.value

				if (!aGenes.trainable || disabled) {
					return@w aGenes.copy()
				}

				val avg = aGenes.genes.average().toFloat()
				val std = max(min(aGenes.genes.std(), 10f), 0.1f)
				val sqrtLen = sqrt(aGenes.size.toFloat())
				val mix = FloatArray(aGenes.size) { ord ->
					val current = aGenes.genes[ord]
					val final = (current - avg) / std
					val capped = max(min(final, settings.weightHeavyCap), -settings.weightHeavyCap) / sqrtLen
					return@FloatArray capped * m + current * mD
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