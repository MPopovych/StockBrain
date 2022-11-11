package brain.layers

import brain.activation.ActivationFunction
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.utils.getShape


sealed class Layer {

	companion object {
		const val DEFAULT_NAME = "layer"
	}

	abstract var name: String
	abstract val nameType: String

	abstract var outputBuffer: Matrix
	abstract fun init()
	open fun getShape(): LayerShape = outputBuffer.getShape()

	open val activation: ActivationFunction? = null
	val weights = LinkedHashMap<String, WeightData>()
	fun addWeights(weightData: WeightData) {
		weights[weightData.name] = weightData
	}

	open fun flushBuffer() {
		MatrixMath.flush(outputBuffer)
	}

	fun getTrainable() = weights.values.filter { it.trainable }
	fun getTrainableNumber() = getTrainable().sumOf { it.matrix.width * it.matrix.height }

	abstract class SingleInputLayer : Layer() {
		abstract fun call(input: Matrix): Matrix
	}

	abstract class MultiInputLayer : Layer() {
		abstract fun call(inputs: List<Matrix>): Matrix
	}
}

class WeightData(
	val name: String,
	val matrix: Matrix,
	var trainable: Boolean,
) {
	fun describe(): String {
		return "${name}: ${matrix.getShape()}"
	}
}

typealias LB = LayerBuilder<*>

sealed interface LayerBuilder<T : Layer> {
	var name: String
	val nameType: String
	fun getShape(): LayerShape
	fun create(): T

	interface DeadEnd<T : Layer.SingleInputLayer> : LayerBuilder<T>

	interface SingleInput<T : Layer.SingleInputLayer> : LayerBuilder<T> {
		val parentLayer: LayerBuilder<*>
	}

	interface MultiInput<T : Layer.MultiInputLayer> : LayerBuilder<T> {
		val parentLayers: List<LayerBuilder<*>>
	}

	fun getSerializedBuilderData(): LayerMetaData? {
		return null
	}
}

data class LayerShape(val width: Int, val height: Int)