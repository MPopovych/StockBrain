package brain.gat.context

import brain.gat.GATScoreBoardOrder

class GATSettings(
	val population: Int,
	val topParentCount: Int,
	val mutationRate: Float = 0.02f,
	val initialMutationRate: Float = 0.50f,
	val weightCap: Float = 1.5f,
	val additive: Boolean = true,
	val weightHeavy: Float = 1.0f,
	val clearEveryGeneration: Boolean,
	val order: GATScoreBoardOrder,
) {

}