package brain.ga

import brain.gat.GATScoreBoardOrder
import brain.pso.PSOUtils
import brain.utils.printGreenBr
import brain.utils.roundUp
import kotlin.math.abs
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
		val worst = scoreList.first().score
		val std = getStandardDeviation()
		val range = abs(best - worst)
		val stdPercent = if (range == 0.0) 0.0 else std / range
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
				if (it.parentA in idSet) {
					scoreList.removeIf { parent ->
						val greater =
							if (settings.scoreBoardOrder == GAScoreBoardOrder.Ascending) {
								it.score >= parent.score
							} else {
								it.score <= parent.score
							}
						parent.id == it.parentA && greater
					}
				}
				if (it.parentB in idSet) {
					scoreList.removeIf { parent ->
						val greater =
							if (settings.scoreBoardOrder == GAScoreBoardOrder.Ascending) {
								it.score >= parent.score
							} else {
								it.score <= parent.score
							}
						parent.id == it.parentB && greater
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
		val top = getTop() ?: return

		val sb = StringBuilder()
		val stdAndPercent = getStdAndPercent()
		sb.append("Room: $order - ")
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()
		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			val distanceToTop = PSOUtils.modelDistance(t.genes, top.genes)
			sb.append("score: ${t.score.roundUp(6).toString().padEnd(8)} \t" +
					"- h: ${t.id.hashCode().toString().padEnd(12)} \t" +
					"- d: ${distanceToTop.roundUp(3).toString().padEnd(5)} \t" +
					"- g: ${t.bornOnEpoch}" ).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}