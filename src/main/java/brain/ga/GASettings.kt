package brain.ga

import brain.ga.policies.*

data class GASettings(
	val rooms: Int = 1,
	val leakRoomEvery: Int = Int.MAX_VALUE,
	val topParentCount: Int, // take X for crossover
	val totalPopulationCount: Int, // generate X samples from crossover
	val scoreBoardOrder: GAScoreBoardOrder, // ordering of result, ASC - higher = better, DSC - lower = better
	val scoreBoardClearOnGeneration: Boolean, // purge scoreboard on every iteration, if true - may need repeat
	val scoreBoardAllowSameResult: Boolean,
	val mutationPolicy: MutationPolicy = AdditiveMutationPolicy(0.02),
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0),
	// repeat top is valuable for semi-random instances, it is re-evaluated and given a new score
	val matchMakingPolicy: MatchMakingPolicy = DefaultMatchMakingPolicy(repeatTop = if (scoreBoardClearOnGeneration) 3 else 0),
	val crossOverPolicy: CrossOverPolicy = SinglePointCrossOver(),
	val scoringPolicy: ScoringPolicy = SteadyScoringPolicy(0.01)
)
