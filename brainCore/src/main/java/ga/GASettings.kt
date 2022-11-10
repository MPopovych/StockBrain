package ga

import ga.policies.*

data class GASettings(
	val topParentCount: Int,
	val totalPopulationCount: Int,
	val scoreBoardOrder: GAScoreBoardOrder,
	val mutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.02),
	val initialMutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.04),
	val matchMakingPolicy: MatchMakingPolicy = DefaultMatchMakingPolicy(),
	val crossOverPolicy: CrossOverPolicy = UniformCrossOver(),
)