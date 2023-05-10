package brain.pso

import brain.ga.policies.MutationPolicy
import brain.ga.policies.ReplaceMutationPolicy

data class PSOSettings(
	val swarms: Int = 1,
	val keepBestForXGens: Int = 40,
	val order: PSOScoreBoardOrder,
	val population: Int,
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0, mod = 0.1f),
	val choreographyPolicy: ChoreographyPolicy = ChoreographyPolicy.SinGenNP
)
