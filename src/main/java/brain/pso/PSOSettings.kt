package brain.pso

import brain.ga.policies.AdditiveMutationPolicy
import brain.ga.policies.MutationPolicy
import brain.ga.policies.ReplaceMutationPolicy

data class PSOSettings(
	val swarms: Int = 1,
	val keepBestForXGens: Int = 40,
	val order: PSOScoreBoardOrder,
	val population: Int,
	val initialMutationPolicy: MutationPolicy = AdditiveMutationPolicy(1.0, mod = 6f),
	val choreographyPolicy: ChoreographyPolicy = ChoreographyPolicy.SinGenNP,
	val alpha: Float = 0.9f, // 0.9f
	val rBetaBase: Float = 0.5f, // 0.5f
	val rBetaRandom: Float = 1.2f,// 1.1f, // 1.4f
	val weightCap: Float = 2f, // 2f
	val weightHeavy: Float = 0.6f, // 0.6f
	val useZeroOpt: Boolean = false,
	val followBest: Boolean = false,
) {
	companion object {
		val DEFAULT_BOT = PSOSettings(
			swarms = 1,
			population = 30,
			order = PSOScoreBoardOrder.Descending,
			initialMutationPolicy = AdditiveMutationPolicy(1.0, mod = 2f),
			choreographyPolicy = ChoreographyPolicy.SinGenNP,
			alpha = 0.9f,
			rBetaBase = 0.5f,
			rBetaRandom = 1.1f,
			weightCap = 2f,
			weightHeavy = 0.6f,
		)

		val DEFAULT_RISK = PSOSettings(
			swarms = 1,
			population = 30,
			order = PSOScoreBoardOrder.Descending,
			initialMutationPolicy = ReplaceMutationPolicy(1.0),
			choreographyPolicy = ChoreographyPolicy.SinGenNP,
			alpha = 0.9f,
			rBetaBase = 0.5f,
			rBetaRandom = 1.2f,
			weightCap = 2f,
			weightHeavy = 0.6f,
		)
	}
}
