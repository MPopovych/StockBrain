package brain.ga.policies

import brain.ga.GAScoreBoard
import brain.ga.GAScoreHolder
import brain.ga.GASettings
import kotlin.random.Random

interface MatchMakingPolicy {
	fun select(settings: GASettings, scoreBoard: GAScoreBoard): List<FutureMatch>
}

sealed class FutureMatch {
	class CrossMatch(val parentA: GAScoreHolder, val parentB: GAScoreHolder, val mutate: Boolean) : FutureMatch()
	class MutateMatch(val source: GAScoreHolder) : FutureMatch()
}

class DefaultMatchMakingPolicy : MatchMakingPolicy {
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		val top = scoreBoard.getTop() ?: throw IllegalStateException()
		buffer.add(FutureMatch.MutateMatch(top))

		val holders = scoreBoard.getAscendingScoreList()
		while (buffer.size < settings.totalPopulationCount - 1) {
			val a = holders.random()
			val b = holders.random()

			if (a == b) continue
			// TODO implement and filter relatives
			buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextBoolean()))
		}
		return buffer
	}

}