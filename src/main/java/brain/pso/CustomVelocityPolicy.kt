package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CustomVelocityPolicy {

	private val jRandom = java.util.Random()

	fun move(
		best: PSOScore,
		top: PSOScore, middle: PSOScore, worst: PSOScore,
		psoContext: PolicyContext,
	): Triple<ModelGenes, ModelGenes, ModelGenes> {
		val alpha = psoContext.settings.alpha
		val rBetaBase = psoContext.settings.rBetaBase
		val rBetaRandom = psoContext.settings.rBetaRandom
		val weightCap = psoContext.settings.weightCap
		val weightHeavy = psoContext.settings.weightHeavy
		val weightMoveCap = weightCap

		val tCopy = top.genes.copy()
		val mCopy = middle.genes.copy()
		val wCopy = worst.genes.copy()

		val scoreRange: Double
		val wTillM: Double
		val tTillB: Double
		val mTillT: Double
		when (psoContext.settings.order) {
			PSOScoreBoardOrder.Ascending -> {
				scoreRange = best.score - worst.score
				wTillM = middle.score - worst.score
				mTillT = top.score - middle.score
				tTillB = best.score - top.score
			}

			PSOScoreBoardOrder.Descending -> {
				scoreRange = worst.score - best.score
				wTillM = worst.score - middle.score
				mTillT = middle.score - top.score
				tTillB = top.score - best.score
			}
		}

		var wTillMRatio = 0.0f
		var mTillTRatio = 0.0f
		var tTillBRatio = 0.0f
		if (scoreRange != 0.0) {
			tTillBRatio = min(tTillB / scoreRange, 1.0).toFloat()
			mTillTRatio = min(mTillT / scoreRange, 1.0).toFloat()
			wTillMRatio = min(wTillM / scoreRange, 1.0).toFloat()
		}

//		val randomDepth = tCopy.layers.values.filter { l -> l.map.values.sumOf { w -> w.size } > 0 }.random().depth
		tCopy.layers.map { tLayer ->
//			if (psoContext.generation % 2 == 0 && tLayer.value.depth != randomDepth) return@map

			val bLayer = best.genes.layers[tLayer.key] ?: throw IllegalStateException()
			val mLayer = mCopy.layers[tLayer.key] ?: throw IllegalStateException()
			val wLayer = wCopy.layers[tLayer.key] ?: throw IllegalStateException()
			tLayer.value.map.values.map inner@{ tWeight ->
				if (tWeight.size == 0) return@inner

				val bWeight = bLayer.map[tWeight.weightName]?.genes ?: throw IllegalStateException()
				val mWeight = mLayer.map[tWeight.weightName]?.genes ?: throw IllegalStateException()
				val wWeight = wLayer.map[tWeight.weightName]?.genes ?: throw IllegalStateException()

				for (i in tWeight.genes.indices) {
					val tValue = tWeight.genes[i]
					val bValue = bWeight[i]
					val mValue = mWeight[i]
					val wValue = wWeight[i]

					val wmDirection = max(min((mValue - wValue) * (wTillMRatio), weightMoveCap), -weightMoveCap)
					val mtDirection =
						max(min((tValue - mValue) * (wTillMRatio + mTillTRatio), weightMoveCap), -weightMoveCap)
					val tbDirection = max(min((bValue - tValue) * (1f - tTillBRatio), weightMoveCap), -weightMoveCap)

					val wSigned =
						wValue + (wmDirection + tbDirection + mtDirection) * alpha * (rBetaBase + rBetaRandom * jRandom.nextFloat())
					val mSigned =
						mValue + (mtDirection - wmDirection + tbDirection) * alpha * (rBetaBase + rBetaRandom * jRandom.nextFloat())
					val tSigned =
						tValue + (tbDirection - mtDirection - wmDirection) * alpha * (rBetaBase + rBetaRandom * jRandom.nextFloat())

					wWeight[i] = max(min(smoothMove(wValue, wSigned, weightHeavy), weightCap), -weightCap)
					mWeight[i] = max(min(smoothMove(mValue, mSigned, weightHeavy), weightCap), -weightCap)
					tWeight.genes[i] = max(min(smoothMove(tValue, tSigned, weightHeavy), weightCap), -weightCap)
				}
			}
		}
		return Triple(tCopy, mCopy, wCopy)
	}

	fun smoothMove(current: Float, target: Float, weightHeavy: Float): Float {
		val t = if (current >= 0 && target <= 0) {
			// move from positive towards negative
			(current + target) / 3
		} else if (current <= 0 && target >= 0) {
			// move from negative towards zero
			(current + target) / 3
		} else {
			if (abs(target) > weightHeavy) {
				(current + target) / 3
			} else {
				target
			}
		}
		return t
	}
}