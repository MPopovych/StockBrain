package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath

class InputLayer(
	val features: Int,
	val steps: Int = 1,
	override var name: String = Layer.DEFAULT_NAME,
) : LayerBuilder.DeadEnd<InputLayerImpl> {
	companion object {
		const val defaultNameType = "Input"
	}

	override val nameType: String = defaultNameType
	private val shape = LayerShape(features, steps)

	override fun getShape(): LayerShape {
		return shape
	}

	override fun create(): InputLayerImpl {
		return InputLayerImpl(shape, name).also { it.init() }
	}
}

class InputLayerImpl(
	private val inputShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = InputLayer.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(inputShape.width, inputShape.height)
	}

	override fun call(input: Matrix): Matrix {
		MatrixMath.transfer(input, outputBuffer)
		return outputBuffer
	}

}