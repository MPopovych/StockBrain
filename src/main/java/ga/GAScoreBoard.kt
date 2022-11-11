package ga

import utils.printGreen

enum class GAScoreBoardOrder {
	Ascending,
	Descending
}

class GAScoreBoard(private val topCount: Int, private val order: GAScoreBoardOrder) {

	private val hashSet = HashSet<String>()
	private val scoreList = ArrayList<GAScoreHolder>()

	val size: Int
		get() = scoreList.size

	fun getAscendingScoreList(): List<GAScoreHolder> = scoreList

	fun getTop(): GAScoreHolder? {
		return scoreList.lastOrNull()
	}

	fun getBottom(): GAScoreHolder? {
		return scoreList.firstOrNull()
	}

	fun pushBatch(batch: List<GAScoreHolder>) {
		scoreList.addAll(batch
			.filter {
				it.id !in hashSet
			}
		)
		when (order) {
			GAScoreBoardOrder.Ascending -> scoreList.sortBy { it.score } // ascending
			GAScoreBoardOrder.Descending -> scoreList.sortByDescending { it.score } // ascending
		}
		while (scoreList.size > topCount) {
			scoreList.removeFirst()
		}
		hashSet.clear()
		scoreList.forEach { hashSet.add(it.chromosomeHash) }
	}

	fun printScoreBoard() {
		val sb = StringBuilder()
		scoreList.forEach { t ->
			sb.append("score: ${t.score} -- ${t.id}").appendLine()
		}
		printGreen(sb.toString().trimIndent())
	}

}