package ga

import utils.printGreen
import java.util.TreeMap
import kotlin.math.max

class GAScoreBoard(private val topCount: Int) {

	private val hashSet = HashSet<String>()
	val scoreMap = TreeMap<Double, GAScoreHolder>()
	val size: Int
		get() = scoreMap.size

	fun getTop(): GAScoreHolder? {
		if (scoreMap.isEmpty()) return null
		return scoreMap.lastEntry().value
	}

	fun getBottomScore(): Double? {
		if (scoreMap.isEmpty()) return null
		return scoreMap.firstKey()
	}

	fun push(scoreHolder: GAScoreHolder) {
		if (!scoreMap.containsKey(scoreHolder.score) && !hashSet.contains(scoreHolder.chromosomeHash)) {
			scoreMap[scoreHolder.score] = scoreHolder
			hashSet.add(scoreHolder.chromosomeHash)
		} else if (!hashSet.contains(scoreHolder.chromosomeHash)) {

			val oldScore = scoreMap.remove(scoreHolder.score) ?: throw IllegalStateException("Should not happen")
			scoreMap[scoreHolder.score] = scoreHolder
			hashSet.add(scoreHolder.chromosomeHash)

			val lowerEntry = scoreMap.lowerEntry(oldScore.score)
			if (lowerEntry != null) {
				val below = oldScore.copy(score = (lowerEntry.key + scoreHolder.score) / 2.0)
				scoreMap[below.score] = below
			} else {
				hashSet.remove(oldScore.chromosomeHash)
			}
		}
	}

	fun trimBottom() {
		if (size == 0) throw IllegalStateException("Nothing to trim")

		while (scoreMap.size > topCount) {
			val lowest = scoreMap.firstEntry()
			scoreMap.remove(lowest.key)
			hashSet.remove(lowest.value.chromosomeHash)
		}
	}

	fun printScoreBoard() {
		val sb = StringBuilder()
		scoreMap.forEach { (t, u) ->
			sb.append("score: $t -- ${u.id}").appendLine()
		}
		printGreen(sb.toString().trimIndent())
	}

}