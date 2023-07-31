package brain.gat.context

import brain.gat.GATScoreBoardOrder

class GATSettings(
	val population: Int,
	val topParentCount: Int,
	val mutationRate: Float = 0.03f,
	val initialMutationRate: Float = 0.50f,
	val weightMod: Float = 1.0f,
	val weightSoftCap: Float = 2.0f,
	val weightHeavyCap: Float = 15.0f,
	val clearEveryGeneration: Boolean,
	val order: GATScoreBoardOrder,
)
