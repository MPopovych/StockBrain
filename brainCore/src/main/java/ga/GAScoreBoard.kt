package ga

import utils.printGreen
import java.util.TreeMap

class GAScoreBoard(private val topCount: Int) {

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
		scoreMap[scoreHolder.score] = scoreHolder
	}

	fun trimBottom() {
		val leftOver = scoreMap.size - topCount
		val bottomEntries = scoreMap.values.toList().subList(0, leftOver)
		bottomEntries.forEach {
			scoreMap.remove(it.score)
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