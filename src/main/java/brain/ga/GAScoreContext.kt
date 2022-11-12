package brain.ga

import brain.ga.weights.ModelGenes
import brain.models.Model
import java.util.concurrent.CopyOnWriteArrayList

class GAScoreContext(val generation: Int, val model: Model, val genes: ModelGenes) {
	internal val records: CopyOnWriteArrayList<Double> = CopyOnWriteArrayList()
	fun pushRecord(record: Double) {
		records.add(record)
	}
}