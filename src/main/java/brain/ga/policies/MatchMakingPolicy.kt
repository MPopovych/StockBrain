package brain.ga.policies

import brain.ga.GAScoreBoard
import brain.ga.GAScoreHolder
import brain.ga.GASettings
import kotlin.random.Random

interface MatchMakingPolicy {
	fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch>
}

sealed class FutureMatch {
	class Repeat(val source: GAScoreHolder) : FutureMatch()
	class CrossMatch(val parentA: GAScoreHolder, val parentB: GAScoreHolder, val mutate: Boolean) : FutureMatch()
	class MutateMatch(val source: GAScoreHolder) : FutureMatch()
}

class DefaultMatchMakingPolicy(private val repeatTop: Int) : MatchMakingPolicy {
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		val top = scoreBoard.getTop() ?: throw IllegalStateException()
		if (repeatTop > 0) {
			scoreBoard.getAscendingFitnessList().takeLast(repeatTop).forEach { best ->
				buffer.add(FutureMatch.Repeat(best))
			}
		}
		buffer.add(FutureMatch.MutateMatch(top))

		val holders = scoreBoard.getAscendingFitnessList().takeLast(settings.topParentCount)
		while (buffer.size < settings.totalPopulationCount - 1) {
			val a = holders.random()
			val b = holders.random()

			if (a == b) continue
			buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextBoolean()))
		}
		return buffer
	}
}

/**
 * According to some articles this approach provides better regularization
 * Allows the neural network to take a "step back" while training and trial a new path
 * While eliminating the aged out solution instead of over-fitting it
 */
class AgingMatchMakingPolicy(private val repeatTop: Int, private val lifespan: Int) : MatchMakingPolicy {

	init {
		require(lifespan > 0)
	}
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		val freshOnes = scoreBoard.getAscendingFitnessList().filter { it.bornOnEpoch >= generation - lifespan }
		if (freshOnes.isNotEmpty()) {
			val top = freshOnes.last()
			if (repeatTop > 0) {
				freshOnes.takeLast(repeatTop).forEach { best ->
					buffer.add(FutureMatch.Repeat(best))
					buffer.add(FutureMatch.MutateMatch(best))
				}
			}
			buffer.add(FutureMatch.MutateMatch(top))
		}
		val youngest = freshOnes.takeLast(settings.topParentCount)
			.ifEmpty { scoreBoard.getAscendingFitnessList() } // fallback if all are expired
		while (buffer.size < settings.totalPopulationCount) {
			val a = youngest.random()
			val b = youngest.random()

			if (a == b && youngest.size > 2) continue
			if (a == b) {
				buffer.add(FutureMatch.MutateMatch(a))
			} else {
				buffer.add(FutureMatch.CrossMatch(a, b, mutate = if (repeatTop > 0) false else Random.nextBoolean()))
			}
		}
		return buffer
	}
}