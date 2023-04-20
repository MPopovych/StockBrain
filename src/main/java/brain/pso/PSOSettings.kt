package brain.pso

import brain.ga.policies.MutationPolicy
import brain.ga.policies.ReplaceMutationPolicy

data class PSOSettings(
	val swarms: Int = 1,
	val order: PSOScoreBoardOrder,
	val population: Int,
	val velocityPolicy: VelocityPolicy = VelocityPolicy.STD,
	val approachPersonalPolicy: ApproachPolicy = ApproachPolicy.OneThird,
	val approachTopPolicy: ApproachPolicy = ApproachPolicy.OneThird,
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0),
)