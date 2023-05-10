package brain.pso

import brain.activation.FastTanhFunction
import brain.ga.weights.ModelGenes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CustomVelocityPolicyV2 {

	private const val alpha = 0.8f
	private const val weightCap = 1.75f

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

		var wTillMRatio = 0.33f
		var mTillTRatio = 0.33f
		var tTillBRatio = 0.33f
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

					val chance1In = 300
					var wDirection = ((tValue - wValue) * (mTillTRatio) + (mValue - wValue) * wTillMRatio) * alpha
					if (Random.nextInt(chance1In) == 0) {
						wDirection = jRandom.nextGaussian().toFloat() * alpha
					}
					var mDirection = ((bValue - mValue) * tTillBRatio + (tValue - mValue) * mTillTRatio) * alpha
					if (Random.nextInt(chance1In) == 0) {
						mDirection = jRandom.nextGaussian().toFloat() * alpha
					}
					var tDirection = (bValue - tValue) * alpha
					if (Random.nextInt(chance1In) == 0) {
						tDirection = jRandom.nextGaussian().toFloat() * alpha
					}

					val wSigned = mValue + wDirection * randomWeightReductionK
					val mSigned = wValue + mDirection * randomWeightReductionK
					val tSigned = tValue + tDirection * randomWeightReductionK

					wWeight[i] = max(min(smoothMove(wValue, wSigned), weightCap), -weightCap)
					mWeight[i] = max(min(smoothMove(mValue, mSigned), weightCap), -weightCap)
					tWeight.genes[i] = max(min(smoothMove(tValue, tSigned), weightCap), -weightCap)
				}
			}
		}
		return Triple(tCopy, mCopy, wCopy)
	}

	private val fastTanh = FastTanhFunction()
	private fun smoothMove(current: Float, target: Float): Float {
		if (current == target) {
			return smoothMove(current, current + jRandom.nextGaussian().toFloat() / 100)
		}
		val t = if (current >= 0 && target <= 0) {
			// move from positive towards negative
			val dampen = fastTanh.apply(current / 3) * current
			if (dampen < 0.000001f) {
				abs(fastTanh.apply(target / 3)) * target
			}
			dampen
		} else if (current <= 0 && target >= 0) {
			// move from negative towards zero
			val dampen = fastTanh.apply(current / 3) * current
			if (dampen > -0.000001f) {
				abs(fastTanh.apply(target / 3)) * target
			}
			dampen
		} else {
			target
		}
//		val d = min(max(t - current, -0.3f), 0.3f)
		val d = t - current
		return current + d
	}
}