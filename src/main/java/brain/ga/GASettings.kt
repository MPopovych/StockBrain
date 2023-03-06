package brain.ga

import brain.ga.policies.*

data class GASettings(
	val topParentCount: Int,
	val totalPopulationCount: Int,
	val scoreBoardOrder: GAScoreBoardOrder,
	val scoreBoardClearOnGeneration: Boolean,
	val mutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.02),
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0),
	val matchMakingPolicy: MatchMakingPolicy = DefaultMatchMakingPolicy(repeatTop = if (scoreBoardClearOnGeneration) 3 else 0),
	val crossOverPolicy: CrossOverPolicy = SinglePointCrossOver(),
)
