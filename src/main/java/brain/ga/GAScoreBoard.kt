package brain.ga

import brain.utils.printGreenBr
import brain.utils.roundUp
import java.util.TreeSet
import kotlin.math.pow
import kotlin.math.sqrt

enum class GAScoreBoardOrder {
	Ascending,
	Descending
}

class GAScoreBoard(private val settings: GASettings) {

	private val idSet = HashSet<String>()
	private val scoreList = ArrayList<GAScoreHolder>()

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

	fun getBottom(): GAScoreHolder? {
		return scoreList.firstOrNull()
	}

	fun pushBatch(batch: List<GAScoreHolder>) {
		if (settings.scoreBoardClearOnGeneration) {
			idSet.clear()
			scoreList.clear()
		}
		val list = if (settings.scoreBoardAllowSameResult) batch else batch.distinctBy { it.score }
		scoreList.addAll(list
			.onEach {
				if (it.score.isNaN() || it.score.isInfinite()) {
					throw IllegalStateException("NaN or Infinite in score : ${it}")
				}
			}
			.distinctBy { it.id }
			.filter {
				it.id !in idSet
			}
		)
		when (settings.scoreBoardOrder) {
			GAScoreBoardOrder.Ascending -> scoreList.sortBy { it.score } // ascending
			GAScoreBoardOrder.Descending -> scoreList.sortByDescending { it.score } // descending
		}
		while (scoreList.size > settings.totalPopulationCount) {
			scoreList.removeFirst()
		}
		idSet.clear()
		scoreList.forEach {
			idSet.add(it.chromosomeHash)
		}
	}

	fun printScoreBoard(limit: Int? = null) {
		val sb = StringBuilder()
		val stdAndPercent = getStdAndPercent()
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()

		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append("score: ${t.score} -- ${t.id.hashCode()} -- g-${t.bornOnEpoch}" ).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}