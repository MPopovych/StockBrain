package brain.ga.policies

import brain.ga.GAScoreBoard
import brain.ga.GAScoreHolder
import brain.ga.GASettings
import kotlin.random.Random

interface MatchMakingPolicy {
	fun select(settings: GASettings, scoreBoard: GAScoreBoard): List<FutureMatch>
}

sealed class FutureMatch {
	class Repeat(val source: GAScoreHolder) : FutureMatch()
	class CrossMatch(val parentA: GAScoreHolder, val parentB: GAScoreHolder, val mutate: Boolean) : FutureMatch()
	class MutateMatch(val source: GAScoreHolder) : FutureMatch()
}

class DefaultMatchMakingPolicy(val repeatTop: Boolean) : MatchMakingPolicy {
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		val top = scoreBoard.getTop() ?: throw IllegalStateException()
		if (repeatTop) buffer.add(FutureMatch.Repeat(top))
		buffer.add(FutureMatch.MutateMatch(top))

		val holders = scoreBoard.getAscendingScoreList()
		while (buffer.size < settings.totalPopulationCount - 1) {
			val a = holders.random()
			val b = holders.random()

			if (a == b) continue
			buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextBoolean()))
		}
		return buffer
	}

}