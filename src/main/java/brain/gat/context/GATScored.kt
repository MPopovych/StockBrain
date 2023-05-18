package brain.gat.context

import brain.gat.encoding.GATModel

class GATScored(
	val id: String,
	val score: Double,
	val model: GATModel,
	val parentA: String,
	val parentB: String,
) {

}