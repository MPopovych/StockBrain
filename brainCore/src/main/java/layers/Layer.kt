package layers

import matrix.Matrix
import matrix.MatrixMath
import utils.getShape


sealed class Layer {

	var name: String = this.javaClass.simpleName.removeSuffix("Impl")

	abstract var outputBuffer: Matrix
	abstract fun create(previousShape: LayerShape, currentShape: LayerShape)
	open fun getShape(): LayerShape = outputBuffer.getShape()

	val weights = ArrayList<WeightData>()
	fun addWeights(name: String, w: Matrix, trainable: Boolean) {
		weights.add(WeightData(name, w, trainable))
	}

	open fun flushBuffer() {
		MatrixMath.flush(outputBuffer)
	}

	fun getTrainable() = weights.filter { it.trainable }

	abstract class SingleInputLayer : Layer() {
		abstract fun call(input: Matrix): Matrix
	}
	abstract class MultiInputLayer : Layer() {
		abstract fun call(inputs: List<Matrix>): Matrix
	}
}

class WeightData(
	val name: String,
	val w: Matrix,
	val trainable: Boolean,
) {
	fun describe(): String {
		return "${name}: ${w.getShape()}"
	}
}

sealed interface LayerBuilder<T : Layer> {
	val name: String
		get() = this.javaClass.simpleName

	interface DeadEnd<T : Layer.SingleInputLayer> : LayerBuilder<T> {
		fun createWith(shape: LayerShape): T
	}

	interface SingleInput<T : Layer.SingleInputLayer> : LayerBuilder<T> {
		val parentLayer: LayerBuilder<*>
		fun createFrom(previousShape: LayerShape): T
	}

	interface MultiInput<T : Layer.MultiInputLayer> : LayerBuilder<T> {
		val parentLayers: List<LayerBuilder<*>>
		fun createFromList(previousShapes: List<LayerShape>): T
	}
}

data class LayerShape(val width: Int, val height: Int) {
	companion object {
		val None = LayerShape(0, 0)
	}
}