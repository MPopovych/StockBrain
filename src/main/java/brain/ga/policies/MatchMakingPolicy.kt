package brain.ga.policies

import brain.ga.GAScoreBoard
import brain.ga.GAScoreHolder
import brain.ga.GASettings
import brain.utils.printGreenBr
import java.lang.Integer.max
import kotlin.random.Random

interface MatchMakingPolicy {
	fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch>
}

sealed class FutureMatch {
	class Repeat(val source: GAScoreHolder) : FutureMatch()
	class CrossMatch(val parentA: GAScoreHolder, val parentB: GAScoreHolder, val mutate: Boolean) : FutureMatch()
	class MutateMatch(val source: GAScoreHolder) : FutureMatch()
	object New : FutureMatch()
}

class DefaultMatchMakingPolicy(private val repeatTop: Int, private val cataclysmEvery: Int? = null) : MatchMakingPolicy {
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		if (repeatTop > 0) {
			scoreBoard.getAscendingFitnessList().takeLast(repeatTop).forEach { best ->
				buffer.add(FutureMatch.Repeat(best))
			}
		}

		if (cataclysmEvery != null && (generation + 1) % cataclysmEvery == 0) {
			scoreBoard.getAscendingFitnessList().drop(settings.topParentCount).forEach {
				it.markOutdated()
			}
			printGreenBr("Executing cataclysm")
		}

		val holders = scoreBoard.getAscendingFitnessList().takeLast(settings.topParentCount)
		while (buffer.size < settings.totalPopulationCount) {
			val a = holders.random()
			val b = holders.random()

			if (a.id == b.id || a.score == b.score) {
				buffer.add(FutureMatch.MutateMatch(a))
			} else {
				buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextInt(8) == 0))
			}
		}
		return buffer
	}
}

/**
 * According to some articles this approach provides better regularization
 * Allows the neural network to take a "step back" while training and trial a new path
 * While eliminating the aged out solution instead of over-fitting it
 */
class AgingMatchMakingPolicy(private val lifespan: Int, private val cataclysmEvery: Int? = null) : MatchMakingPolicy {

	init {
		require(lifespan > 0)
		require(cataclysmEvery == null || cataclysmEvery > 0)
	}
	override fun select(settings: GASettings, scoreBoard: GAScoreBoard, generation: Int): List<FutureMatch> {
		val buffer = ArrayList<FutureMatch>()
		val freshAndOld = scoreBoard.getAscendingFitnessList().partition { it.bornOnEpoch >= generation - lifespan }
		val freshOnes = freshAndOld.first.distinctBy { it.score }.takeLast(settings.topParentCount)
		val oldOnes = freshAndOld.second

		if (freshOnes.isNotEmpty()) {
			oldOnes.forEach { it.markOutdated() }
		}

		if (cataclysmEvery != null && (generation + 1) % cataclysmEvery == 0) {
			if (freshOnes.isNotEmpty()) {
				scoreBoard.getAscendingFitnessList().drop(settings.topParentCount).forEach {
					it.markOutdated()
				}
				printGreenBr("Executing cataclysm")
			}
		}

		val youngest = freshOnes.ifEmpty { oldOnes }
		while (buffer.size < settings.totalPopulationCount) {
			val a = youngest.random()
			val b = youngest.random()

			if (a.score == b.score) {
				buffer.add(FutureMatch.MutateMatch(a))
			} else {
				// magic number
				buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextInt(5) == 0))
				buffer.add(FutureMatch.CrossMatch(a, b, mutate = Random.nextInt(5) == 0))
			}
		}
		return buffer
	}
}