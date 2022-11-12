package brain.ga.policies

import brain.ga.GAScoreBoard

interface MutationRatePolicy {

	fun calculateMutationRatio(scoreBoard: GAScoreBoard): Double

}

class STDMutationRagePolicy: MutationRatePolicy {
	override fun calculateMutationRatio(scoreBoard: GAScoreBoard): Double {
		val pair = scoreBoard.getStdAndPercent()
		return 0.0
	}
}
