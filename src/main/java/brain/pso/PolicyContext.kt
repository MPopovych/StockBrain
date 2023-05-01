package brain.pso

class PolicyContext(
	val generation: Int,
	val board: PSOScoreBoard,
	val settings: PSOSettings,
	val choreographyK: Float
) {
}