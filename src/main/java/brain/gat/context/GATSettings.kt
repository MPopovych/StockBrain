package brain.gat.context

import brain.gat.GATScoreBoardOrder

class GATSettings(
	val population: Int,
	val topParentCount: Int,
	val mutationRate: Float,
	val initialMutationRate: Float = 0.50f,
	val weightMod: Float,
	val weightHeavyCap: Float = 6.0f,
	val normMomentum: Float = 0.00001f,
	val removeAncestors: Boolean = false,
	val useTwoSetOfGenes: Boolean = false,
	val clearEveryGeneration: Boolean,
	val order: GATScoreBoardOrder,
)
