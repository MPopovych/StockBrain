package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object CustomVelocityPolicyResOld {

	private const val alpha = 0.6f
	private const val alphaRandom = 0.3f

	private const val weightCap = 1.3f
	private const val weightHeavy = 0.6f

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

		var wTillMRatio = 0.90f
		var mTillTRatio = 0.60f
		var tTillBRatio = 0.30f
		if (scoreRange != 0.0) {
			tTillBRatio = min(tTillB / scoreRange, 1.0).toFloat()
			mTillTRatio = min(mTillT / scoreRange, 1.0).toFloat()
			wTillMRatio = min(wTillM / scoreRange, 1.0).toFloat()
		}
//		val randomDepth = tCopy.layers.values.filter { l -> l.map.values.sumOf { w -> w.size } > 0 }.random().depth
//		val randomWeightReductionK = if (jRandom.nextInt(10) == 0) 0.995f else 1f
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

					val chance1In = 100
					var wmDirection = (mValue - wValue) * (alpha + alphaRandom * jRandom.nextFloat())
					if (Random.nextInt(chance1In) == 0 && wmDirection == 0.0f) {
						wmDirection = -wValue
					}
					var mtDirection = (tValue - mValue) * (alpha + alphaRandom * jRandom.nextFloat())
//					if (Random.nextInt(chance1In) == 0 && mtDirection == 0.0f) {
//						mtDirection = -mValue
//					}
					var tbDirection = (bValue - tValue) * (alpha + alphaRandom * jRandom.nextFloat())
					if (tbDirection == 0.0f) {
						tbDirection = (mtDirection + wmDirection) / 2 + (jRandom.nextGaussian().toFloat() / 30f)
					}
					if (mtDirection == 0.0f) {
						mtDirection = (tbDirection + wmDirection) / 2 + (jRandom.nextGaussian().toFloat() / 30f)
					}
					if (wmDirection == 0.0f) {
						wmDirection = (tbDirection + mtDirection) / 2 + (jRandom.nextGaussian().toFloat() / 30f)
					}

					val wSigned = wValue + wmDirection
					val mSigned = mValue + mtDirection
					val tSigned = tValue + tbDirection

					wWeight[i] = max(min(smoothMove(wValue, wSigned), weightCap), -weightCap)
					mWeight[i] = max(min(smoothMove(mValue, mSigned), weightCap), -weightCap)
					tWeight.genes[i] = max(min(smoothMove(tValue, tSigned), weightCap), -weightCap)
				}
			}
		}
		return Triple(tCopy, mCopy, wCopy)
	}

	fun smoothMove(current: Float, target: Float): Float {
		val t = if (current >= 0 && target <= 0) {
			// move from positive towards negative
			(current + target) / 3
		} else if (current <= 0 && target >= 0) {
			// move from negative towards zero
			(current + target) / 3
		} else {
			if (abs(target) > weightHeavy) {
				(current + target) / 3
			}
			target
		}
		return t
	}
}