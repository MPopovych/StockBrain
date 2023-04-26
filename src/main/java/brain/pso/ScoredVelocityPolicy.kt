package brain.pso

import brain.ga.weights.ModelGenes
import brain.utils.roundUpInt
import utils.ext.average
import utils.ext.dev
import utils.ext.std
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

object ScoredVelocityPolicy {
//	fun move(scoreBoardOrder: PSOScoreBoard, mod: ModelGenes): ModelGenes {
//		val totalGeneCount = PSOUtils.countModelGenes(mod)
//		val take = (scoreBoardOrder.size.toDouble() * 0.1).roundUpInt()
//		val allGenes = scoreBoardOrder.getAscendingFitnessList().map { it.best.genes }.takeLast(take)
//
////		var counter = 0
//		mod.layers.map { l ->
//			val allLayers = allGenes.map { it.layers[l.key] ?: throw IllegalStateException() }
//			l.value.map.values.map { w ->
//				val allWeights = allLayers.map { it.map[w.weightName] ?: throw IllegalStateException() }
//				val deviationWeights = devBuffer(allWeights.map { it.genes })
//				val deviationWeightsSorted = deviationWeights.sorted()
//				val topQuartileValue = deviationWeightsSorted[deviationWeights.size / 4]
//				val halfQuartileValue = deviationWeightsSorted[deviationWeights.size / 2]
//
//				for (i in w.genes.indices) {
//					// apply to the ones with most deviation
//					// apply weight decay
//					if (deviationWeights[i] > topQuartileValue) {
//						w.genes[i] = (w.genes[i] + (Random.nextFloat() * 2 - 1f) / 50f) * 0.995f
//					} else if (deviationWeights[i] > halfQuartileValue) {
//						w.genes[i] = w.genes[i] * 0.8f + allWeights.random().genes[i] * 0.2f
//					} else {
//						// do not mod
//					}
//				}
//			}
//		}
//		return mod
//	}

	fun move(scoreBoardOrder: PSOScoreBoard, mod: ModelGenes): ModelGenes {
		val take = (scoreBoardOrder.size.toDouble() * 0.1).roundUpInt()
		val allGenes = scoreBoardOrder.getAscendingFitnessList().map { it.best.genes }.takeLast(take)

		mod.layers.map { l ->
			val allLayers = allGenes.map { it.layers[l.key] ?: throw IllegalStateException() }
			l.value.map.values.map { w ->
				val allWeights = allLayers.map { it.map[w.weightName] ?: throw IllegalStateException() }
				val deviationWeights = devBuffer(allWeights.map { it.genes })
				val deviationWeightsSorted = deviationWeights.sorted()
				val halfQuartileValue = deviationWeightsSorted[deviationWeights.size / 2]

				val reshuffleBuffer = FloatArray(w.size)

				for (i in w.genes.indices) {
					// apply to the ones with most deviation
					// apply weight decay
					if (deviationWeights[i] > halfQuartileValue) {
						reshuffleBuffer[i] = (0.75f + Random.nextFloat() / 2) * 0.992f
					} else {
						// do not mod
						reshuffleBuffer[i] = 0.992f
					}
				}
				for (i in w.genes.indices) {
					w.genes[i] = w.genes[i] * reshuffleBuffer[i] + (Random.nextFloat() * 2 - 1f) / 40
				}
			}
		}
		return mod
	}

	private fun averageBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.average()
		}
		return buf
	}

	private fun stdBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.std()
		}
		return buf
	}

	private fun devBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.dev()
		}
		return buf
	}
}