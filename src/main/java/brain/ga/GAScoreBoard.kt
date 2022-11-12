package brain.ga

import brain.utils.printGreenBr
import brain.utils.roundUp
import kotlin.math.pow
import kotlin.math.sqrt

enum class GAScoreBoardOrder {
	Ascending,
	Descending
}

class GAScoreBoard(private val topCount: Int, private val order: GAScoreBoardOrder) {

	private val idSet = HashSet<String>()
	private val scoreList = ArrayList<GAScoreHolder>()

	val size: Int
		get() = scoreList.size

	fun getAscendingScoreList(): List<GAScoreHolder> = scoreList

	fun getTop(): GAScoreHolder? {
		return scoreList.lastOrNull()
	}

	fun getStandardDeviation(): Double {
		return scoreList.map { it.score }.let {
			val mean = it.average()
			val stdSum = it.sumOf { score -> (score - mean).pow(2) }
			return@let sqrt(stdSum / size)
		}
	}

	fun getStdAndPercent(): Pair<Double, Double> {
		val best = scoreList.last().score
		val std = getStandardDeviation()
		val stdPercent = std / best
		return Pair(std, stdPercent * 100)
	}

	fun getBottom(): GAScoreHolder? {
		return scoreList.firstOrNull()
	}

	fun pushBatch(batch: List<GAScoreHolder>) {
		scoreList.addAll(batch
			.distinctBy { it.id }
			.filter {
				it.id !in idSet
			}
		)
		when (order) {
			GAScoreBoardOrder.Ascending -> scoreList.sortBy { it.score } // ascending
			GAScoreBoardOrder.Descending -> scoreList.sortByDescending { it.score } // ascending
		}
		while (scoreList.size > topCount) {
			scoreList.removeFirst()
		}
		idSet.clear()
		scoreList.forEach { idSet.add(it.chromosomeHash) }
	}

	fun printScoreBoard(limit: Int? = null) {
		val sb = StringBuilder()
		val stdAndPercent = getStdAndPercent()
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()

		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append("score: ${t.score} -- ${t.id.hashCode()}").appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}