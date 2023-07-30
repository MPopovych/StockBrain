package brain.gat.context

import brain.gat.GATScoreBoardOrder

class GATSettings(
	val population: Int,
	val topParentCount: Int,
	val mutationRate: Float = 0.02f,
	val initialMutationRate: Float = 0.50f,
	val weightMod: Float = 0.1f,
	val weightSoftCap: Float = 1.5f,
	val weightHeavyCap: Float = 5.0f,
	val clearEveryGeneration: Boolean,
	val order: GATScoreBoardOrder,
) {

}