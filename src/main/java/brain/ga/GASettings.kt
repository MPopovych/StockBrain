package brain.ga

import brain.ga.policies.*

data class GASettings(
	val topParentCount: Int,
	val totalPopulationCount: Int,
	val scoreBoardOrder: GAScoreBoardOrder,
	val scoreBoardClearOnGeneration: Boolean,
	val mutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.02),
	val initialMutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.04),
	val matchMakingPolicy: MatchMakingPolicy = DefaultMatchMakingPolicy(repeatTop = scoreBoardClearOnGeneration),
	val crossOverPolicy: CrossOverPolicy = UniformCrossOver(),
)
