package brain.pso

import brain.ga.policies.MutationPolicy
import brain.ga.policies.ReplaceMutationPolicy

data class PSOSettings(
	val swarms: Int = 1,
	val order: PSOScoreBoardOrder,
	val population: Int,
	val velocityPolicy: VelocityPolicy = VelocityPolicy.STD,
	val approachPersonalPolicy: ApproachPolicy = ApproachPolicy.OneFifth,
	val approachTopPolicy: ApproachPolicy = ApproachPolicy.OneTenth,
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0),
)