package brain.pso

class PSOScoreBoardWrapRooms(private val settings: PSOSettings) {

	val rooms: List<PSOScoreBoard> = (0 until settings.swarms).map { PSOScoreBoard(it, settings) }

	val size: Int
		get() = rooms.sumOf { it.size }

	fun getAscendingFitnessList(): List<PSOHolder> = rooms
		.map { it.getAscendingFitnessList() }
		.flatten()
		.let { s ->
			return@let when (settings.order) {
				PSOScoreBoardOrder.Ascending -> s.sortedBy { it.current.score } // ascending
				PSOScoreBoardOrder.Descending -> s.sortedByDescending { it.current.score } // descending
			}
		}

	fun getTop(): PSOScore? {
		return when (settings.order) {
			PSOScoreBoardOrder.Ascending -> rooms.mapNotNull { it.getTop() }.maxByOrNull { it.score }
			PSOScoreBoardOrder.Descending -> rooms.mapNotNull { it.getTop() }.minByOrNull { it.score }
		}
	}



}