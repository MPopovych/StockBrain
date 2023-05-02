package brain.pso

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

interface ChoreographyPolicy {
	companion object {
		val Linear = LinearChoreographyPolicy
		val SinGen = SinGenChoreographyPolicy(15)
		val SinPeakGen = SinPeakGenChoreographyPolicy(15)
	}
	fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float
}

object LinearChoreographyPolicy : ChoreographyPolicy {
	override fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float {
		return 1f
	}
}

class SinPeakGenChoreographyPolicy(private val cycleSize: Int = 15) : ChoreographyPolicy {
	override fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float {
		val toCycle = max(sin(((gen.toDouble() + cycleSize.toDouble() / Math.PI) * Math.PI) / cycleSize), 0.0)
		return toCycle.pow(20.0).toFloat() * 3 + 0.5f
	}
}

class SinGenChoreographyPolicy(private val cycleSize: Int = 15) : ChoreographyPolicy {
	override fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float {
		val toCycle = (gen.toDouble() * 10) / (Math.PI * cycleSize)
		return (cos(toCycle) * 0.8 + 0.9).toFloat()
	}
}