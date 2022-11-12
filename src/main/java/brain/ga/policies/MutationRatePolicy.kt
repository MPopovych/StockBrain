package brain.ga.policies

import brain.ga.GAScoreBoard
import kotlin.math.abs

interface MutationRatePolicy {

	fun calculateMutationRate(generation: Int, scoreBoard: GAScoreBoard): Double

}

class STDMutationRagePolicy(private val basisRate: Double = 0.01) : MutationRatePolicy {
	override fun calculateMutationRate(generation: Int, scoreBoard: GAScoreBoard): Double {
		val deviationFraction = scoreBoard.getStdAndPercent().second / 100.0
		return abs(basisRate - deviationFraction)
	}
}

class SinMutationRagePolicy(
	private val rate: Int = 10,
	private val amplitude: Double = 0.9,
	private val base: Double = 0.1
) : MutationRatePolicy {

	init {
		if (base - amplitude <= 0 || base + amplitude >= 1.0)
			throw IllegalStateException("Amplitude is out of range (0..1)")
	}

	override fun calculateMutationRate(generation: Int, scoreBoard: GAScoreBoard): Double {
//		val deviationFraction = scoreBoard.getStdAndPercent().second / 100.0
		return 0.0
	}
}
