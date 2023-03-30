package brain.ga

import brain.utils.printGreenBr
import brain.utils.printRedBr
import brain.utils.roundUp
import java.util.TreeSet
import kotlin.math.pow
import kotlin.math.sqrt

enum class GAScoreBoardOrder {
	Ascending,
	Descending
}

class GAScoreBoard(private val settings: GASettings) {

	private var scoreList: ArrayList<GAScoreHolder> = ArrayList<GAScoreHolder>()

	val size: Int
		get() = scoreList.size

	fun getAscendingFitnessList(): List<GAScoreHolder> = scoreList

	fun getTop(): GAScoreHolder? {
		return scoreList.lastOrNull()
	}

	fun getStandardDeviation(): Double {
		return scoreList.takeLast(settings.topParentCount).map { it.score }.let {
			val mean = it.average()
			val stdSum = it.sumOf { score -> (score - mean).pow(2) }
			return@let sqrt(stdSum / size)
		}
	}

	fun getStdAndPercent(): Pair<Double, Double> {
		val best = scoreList.last().score
		val std = getStandardDeviation()
		val stdPercent = if (best == 0.0) 0.0 else std / best
		return Pair(std, stdPercent * 100)
	}

	fun pushBatch(batch: List<GAScoreHolder>) {
		if (settings.scoreBoardClearOnGeneration) {
			printRedBr("Clear all? ${scoreList.map { it.score }}")
			scoreList.clear()
		}
		batch.onEach {
				if (it.score.isNaN() || it.score.isInfinite()) {
					throw IllegalStateException("NaN or Infinite in score : $it")
				}
			}
		scoreList.addAll(0, batch) // add to 0 for distinct
		var sorted = when (settings.scoreBoardOrder) {
			GAScoreBoardOrder.Ascending -> scoreList.distinctBy { it.id }.sortedBy { it.score } // ascending
			GAScoreBoardOrder.Descending -> scoreList.distinctBy { it.id }.sortedByDescending { it.score } // descending
		}
		sorted = if (settings.scoreBoardAllowSameResult) sorted else sorted.distinctBy { it.score }

		scoreList.clear()
		scoreList.addAll(sorted)
		while (scoreList.size > settings.totalPopulationCount) {
			scoreList.removeFirst()
		}
	}

	fun printScoreBoard(limit: Int? = null) {
		val sb = StringBuilder()
		val stdAndPercent = getStdAndPercent()
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()

		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append("score: ${t.score} -- ${t.id.hashCode()} -- g:${t.bornOnEpoch}" ).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}