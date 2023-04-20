package brain.ga

import brain.utils.printGreenBr
import brain.utils.roundUp
import kotlin.math.pow
import kotlin.math.sqrt

enum class GAScoreBoardOrder {
	Ascending,
	Descending
}

class GAScoreBoard(val order: Int, private val settings: GASettings) {

	private var scoreList: ArrayList<GAScoreHolder> = ArrayList()

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
			scoreList.clear()
		}
		val idSet = scoreList.filter { !it.isOutDated }.map { it.id }.toSet()

		batch.onEach {
				if (it.score.isNaN() || it.score.isInfinite()) {
					throw IllegalStateException("NaN or Infinite in score : $it")
				}
				if (it.genes.parentAId in idSet) {
					scoreList.removeIf { parent ->
						parent.id == it.genes.parentAId && parent.score <= it.score
					}
				}
				if (it.genes.parentBId in idSet) {
					scoreList.removeIf { parent ->
						parent.id == it.genes.parentBId && parent.score <= it.score
					}
				}
			}

		scoreList.addAll(0, batch.filter { it.id !in idSet  }) // add to 0 for distinct
		var sorted = when (settings.scoreBoardOrder) {
			GAScoreBoardOrder.Ascending -> scoreList
				.sortedBy { it.bornOnEpoch }
				.sortedBy { it.score } // ascending
			GAScoreBoardOrder.Descending -> scoreList
				.sortedBy { it.bornOnEpoch }
				.sortedByDescending { it.score } // descending
		}
		sorted = sorted.filter { !it.isOutDated }
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
		sb.append("Room: $order - ")
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()
		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append("score: ${t.score} \t" +
					"-- ${t.id.hashCode()} \t" +
					"-- g:${t.bornOnEpoch}" ).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}