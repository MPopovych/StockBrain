package brain.layers.weights

import brain.matrix.Matrix
import brain.serialization.MatrixSerialized
import brain.serialization.WeightSerialized

class WeightData(
	val name: String,
	val matrix: Matrix,
	val active: Boolean,
	val trainable: Boolean,
) {
	fun copy() = WeightData(name, matrix.copy(), active, trainable)
	fun serialize() = WeightSerialized(name, MatrixSerialized.fromMatrix(matrix), active, trainable)
}
