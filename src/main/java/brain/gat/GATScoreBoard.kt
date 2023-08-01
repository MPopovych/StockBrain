package brain.gat

import brain.gat.context.GATScored
import brain.gat.context.GATSettings
import brain.utils.printGreenBr
import brain.utils.roundUp
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

enum class GATScoreBoardOrder {
	Ascending,
	Descending
}

class GATScoreBoard(val order: Int, private val settings: GATSettings) {

	private var scoreList: ArrayList<GATScored> = ArrayList()

	val size: Int
		get() = scoreList.size

	// last is best
	fun getAscendingFitnessList(): List<GATScored> = scoreList

	fun getTop(): GATScored? {
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

	fun pushBatch(batch: List<GATScored>) {
		if (settings.clearEveryGeneration) {
			scoreList.clear()
		}
		val idSet = scoreList.map { it.id }.toSet()

		batch.onEach {
			if (it.score.isNaN() || it.score.isInfinite()) {
				throw IllegalStateException("NaN or Infinite in score : $it")
			}
			if (it.parentA in idSet) {
				scoreList.removeIf { parent ->
					val greater =
						if (settings.order == GATScoreBoardOrder.Ascending) {
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
						if (settings.order == GATScoreBoardOrder.Ascending) {
							it.score >= parent.score
						} else {
							it.score <= parent.score
						}
					parent.id == it.parentB && greater
				}
			}
		}

		scoreList.addAll(0, batch.filter { it.id !in idSet }) // add to 0 for distinct
		val sorted = when (settings.order) {
			GATScoreBoardOrder.Ascending -> scoreList
				.sortedBy { it.model.bornOnEpoch }
				.sortedBy { it.score } // ascending
			GATScoreBoardOrder.Descending -> scoreList
				.sortedBy { it.model.bornOnEpoch }
				.sortedByDescending { it.score } // descending
		}

		scoreList.clear()
		scoreList.addAll(sorted)
		while (scoreList.size > settings.population) {
			scoreList.removeFirst()
		}
	}

	fun printScoreBoard(limit: Int? = null) {
		val sb = StringBuilder()
		val stdAndPercent = getStdAndPercent()
		sb.append("Room: $order - ")
		sb.append("Score deviation: ${stdAndPercent.first} : ${stdAndPercent.second.roundUp(2)}%").appendLine()
		scoreList.takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append(
				"score: ${t.score.roundUp(6).toString().padEnd(8)} \t" +
						"- h: ${t.id.hashCode().toString().padEnd(12)} \t" +
						"- g: ${t.model.bornOnEpoch}"
			).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}