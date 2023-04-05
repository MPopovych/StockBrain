package brain.ga.policies

import brain.ga.GAScoreBoard
import brain.ga.GAScoreBoardOrder
import brain.ga.GASettings
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

interface ScoringPolicy {
	fun applyScore(settings: GASettings, board: GAScoreBoard, score: Double, gen: Int): Double
}

class PlainScoringPolicy: ScoringPolicy {
	override fun applyScore(settings: GASettings, board: GAScoreBoard, score: Double, gen: Int): Double {
		return score
	}
}

class SteadyScoringPolicy(pace: Double = 0.01, private val eps: Double = 0.002) : ScoringPolicy {
	private val capKF = 1.0 + pace
	override fun applyScore(settings: GASettings, board: GAScoreBoard, score: Double, gen: Int): Double {
		val top = board.getTop()?.score ?: return score
		if (top == 0.0) return score
		return when (settings.scoreBoardOrder) {
			GAScoreBoardOrder.Ascending -> {
				min((max(top, 0.0) + eps) * capKF - (Random.nextFloat() / 10000), score)
			}
			GAScoreBoardOrder.Descending -> {
				max(top / capKF + Random.nextFloat() / 10000, score)
			}
		}
	}

}