package brain.pso

import brain.utils.printGreenBr
import brain.utils.roundUp
import kotlin.math.pow
import kotlin.math.sqrt

enum class PSOScoreBoardOrder {
	Ascending,
	Descending
}

class PSOScoreBoard(val order: Int, private val settings: PSOSettings) {

	private var best: PSOHolder? = null
	private var scoreList: HashMap<Int, PSOHolder> = HashMap()

	val size: Int
		get() = scoreList.size

	fun getAscendingFitnessList(): List<PSOHolder> {
		return when (settings.order) {
			PSOScoreBoardOrder.Ascending -> scoreList.values
				.sortedBy { it.current.score } // ascending
			PSOScoreBoardOrder.Descending -> scoreList.values
				.sortedByDescending { it.current.score } // descending
		}
	}

	fun getTop(): PSOScore? {
		return best?.best
	}

	fun getStandardDeviation(): Double {
		return scoreList.values.map { it.current.score }.let {
			val mean = it.average()
			val stdSum = it.sumOf { score -> (score - mean).pow(2) }
			return@let sqrt(stdSum / size)
		}
	}

	fun push(eval: PSOHolder) {
		if (!eval.current.score.isFinite()) throw IllegalStateException("Not a finite number: ${eval.current.score}")

		val top = best
		if (top == null) {
			best = eval
		}
		if (top != null) {
			if (settings.order == PSOScoreBoardOrder.Ascending && eval.current.score >= top.best.score) {
				best = eval
			} else if (settings.order == PSOScoreBoardOrder.Descending && eval.current.score <= top.best.score) {
				best = eval
			}
		}
		if (settings.order == PSOScoreBoardOrder.Ascending && eval.current.score >= eval.best.score) {
			eval.best = eval.current
		} else if (settings.order == PSOScoreBoardOrder.Descending && eval.current.score <= eval.best.score) {
			eval.best = eval.current
		}
		scoreList[eval.ordinal] = eval
	}

	fun printScoreBoard(limit: Int? = null) {
		val sb = StringBuilder()
		val std = getStandardDeviation()
		sb.append("Room: $order - ")
		sb.append("Score deviation: ${std.roundUp(6)}").appendLine()
		getAscendingFitnessList().takeLast(limit ?: scoreList.size).forEach { t ->
			sb.append("score: ${t.current.score} -- ${t.current.genes.hashCode()} : ${t.ordinal}" ).appendLine()
		}
		printGreenBr(sb.toString().trimIndent())
	}

}