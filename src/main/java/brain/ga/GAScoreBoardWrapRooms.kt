package brain.ga

class GAScoreBoardWrapRooms(private val settings: GASettings) {

	val rooms: List<GAScoreBoard> = (0 until settings.rooms).map { GAScoreBoard(it, settings) }

	val size: Int
		get() = rooms.sumOf { it.size }

	fun getAscendingFitnessList(): List<GAScoreHolder> = rooms
		.map { it.getAscendingFitnessList() }
		.flatten()
		.let { s ->
			return@let when (settings.scoreBoardOrder) {
				GAScoreBoardOrder.Ascending -> s.sortedBy { it.score } // ascending
				GAScoreBoardOrder.Descending -> s.sortedByDescending { it.score } // descending
			}
		}

	fun getTop(): GAScoreHolder? {
		return when (settings.scoreBoardOrder) {
			GAScoreBoardOrder.Ascending -> rooms.mapNotNull { it.getTop() }.maxByOrNull { it.score }
			GAScoreBoardOrder.Descending -> rooms.mapNotNull { it.getTop() }.minByOrNull { it.score }
		}
	}


}