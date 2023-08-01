package brain.layers.abs

import brain.layers.weights.WeightData
import brain.matrix.Matrix

sealed interface LayerImpl {
	val id: String
	fun init() {}
	fun onWeightUpdated() {}
	fun weightData(): List<WeightData>
	val factory: LayerTypedFactory<LayerImpl, *>

	interface LayerSingleInput: LayerImpl {
		fun propagate(input: Matrix): Matrix
	}

	interface LayerMultiInput: LayerImpl {
		fun propagate(inputs: List<Matrix>): Matrix
	}
}