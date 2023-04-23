package brain.pso

import brain.ga.policies.MutationPolicy
import brain.ga.policies.ReplaceMutationPolicy

data class PSOSettings(
	val swarms: Int = 1,
	val order: PSOScoreBoardOrder,
	val population: Int,
	val velocityPolicy: VelocityPolicy = VelocityPolicy.Distance,
	val approachPersonalPolicy: ApproachPolicy = ApproachPolicy.KeepDistance, // explore
	val approachTopPolicy: ApproachPolicy = ApproachPolicy.Classic,
	val initialMutationPolicy: MutationPolicy = ReplaceMutationPolicy(1.0, mod = 0.1f),
)

/**
 *  Has resistance to overfitting (Maybe due to distributed param growth) for MNIST
 *  Good on stock rl
 *
 *  val velocityPolicy: VelocityPolicy = VelocityPolicy.Distance,
 * 	val approachPersonalPolicy: ApproachPolicy = ApproachPolicy.KeepDistance,
 * 	val approachTopPolicy: ApproachPolicy = ApproachPolicy.OneThird,
 */

/** Produced good results in a reasonable time for MNIST
 *  val velocityPolicy: VelocityPolicy = VelocityPolicy.Distance,
 * 	val approachPersonalPolicy: ApproachPolicy = ApproachPolicy.FastApproach, // explore
 * 	val approachTopPolicy: ApproachPolicy = ApproachPolicy.OneTenth,
 */