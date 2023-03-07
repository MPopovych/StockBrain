package brain.ga

import brain.ga.policies.*

data class GASettings(
	val topParentCount: Int,
	val totalPopulationCount: Int,
	val scoreBoardOrder: GAScoreBoardOrder,
	val scoreBoardClearOnGeneration: Boolean,
	val mutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.02),
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0),
	// repeat top is valuable for semi-random instances, it is re-evaluated and given a new score
	val matchMakingPolicy: MatchMakingPolicy = DefaultMatchMakingPolicy(repeatTop = if (scoreBoardClearOnGeneration) 3 else 0),
	val crossOverPolicy: CrossOverPolicy = SinglePointCrossOver(),
)
