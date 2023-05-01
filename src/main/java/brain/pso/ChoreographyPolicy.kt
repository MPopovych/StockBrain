package brain.pso

import kotlin.math.cos

interface ChoreographyPolicy {
	companion object {
		val Linear = LinearChoreographyPolicy
		val SinGen = SinGenChoreographyPolicy(15)
	}
	fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float
}

object LinearChoreographyPolicy : ChoreographyPolicy {
	override fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float {
		return 1f
	}
}

class SinGenChoreographyPolicy(private val cycleSize: Int = 15) : ChoreographyPolicy {
	override fun getKForContext(settings: PSOSettings, gen: Int, board: PSOScoreBoard): Float {
		val toCycle = (gen.toDouble() * 10) / (Math.PI * cycleSize)
		return (cos(toCycle) * 0.8 + 0.9).toFloat()
	}
}