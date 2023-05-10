package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CustomVelocityPolicy {

	private const val alpha = 0.8f
	private const val weightCap = 1.0f

	private val jRandom = java.util.Random()

	fun move(
		best: PSOScore,
		top: PSOScore, middle: PSOScore, worst: PSOScore,
		psoContext: PolicyContext,
	): Triple<ModelGenes, ModelGenes, ModelGenes> {
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
				tTillB = best.score - top.score
				mTillT = top.score - middle.score
			}

			PSOScoreBoardOrder.Descending -> {
				scoreRange = worst.score - best.score
				wTillM = worst.score - middle.score
				tTillB = top.score - best.score
				mTillT = middle.score - top.score
			}
		}

		var wTillMRatio = 0.5f
		var mTillTRatio = 0.5f
		var tTillBRatio = 0.5f
		if (scoreRange != 0.0) {
			wTillMRatio = min(wTillM / scoreRange, 1.0).toFloat()
			mTillTRatio = min(mTillT / scoreRange, 1.0).toFloat()
			tTillBRatio = min(tTillB / scoreRange, 1.0).toFloat()
		}
		val randomWeightReductionK = if (jRandom.nextInt(10) == 0) 0.995f else 1f
		tCopy.layers.map { tLayer ->
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

					var wDirection = ((tValue - wValue) * (mTillTRatio) + (mValue - wValue) * wTillMRatio) * alpha
					if (abs(wDirection) < 0.00001f || scoreRange == 0.0) {
						wDirection = jRandom.nextGaussian().toFloat() * alpha
					}
					var mDirection = ((bValue - mValue) * tTillBRatio + (tValue - mValue) * mTillTRatio) * alpha
					if (abs(mDirection) < 0.00001f || scoreRange == 0.0) {
						mDirection = jRandom.nextGaussian().toFloat() * alpha
					}
					var tDirection = (bValue - tValue) * alpha
					if (abs(tDirection) < 0.00001f || scoreRange == 0.0) {
						tDirection = jRandom.nextGaussian().toFloat() * alpha
					}

					val wSigned = wDirection
					val mSigned = mDirection
					val tSigned = tDirection

					wWeight[i] = max(min(mValue + wSigned, weightCap), -weightCap) * randomWeightReductionK
					mWeight[i] = max(min(wValue + mSigned, weightCap), -weightCap) * randomWeightReductionK
					tWeight.genes[i] = max(min(tValue + tSigned, weightCap), -weightCap) * randomWeightReductionK
				}
			}
		}
		return Triple(tCopy, mCopy, wCopy)
	}
}