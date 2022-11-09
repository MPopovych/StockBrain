package layers

import matrix.Matrix
import matrix.MatrixMath

class InputLayer(
	val features: Int,
	val steps: Int = 1,
): LayerBuilder.DeadEnd<InputLayerImpl>{

	override fun createWith(shape: LayerShape): InputLayerImpl {
		return InputLayerImpl().also { it.create(LayerShape.None, LayerShape(features, steps)) }
	}

	fun create(): InputLayerImpl {
		return createWith(LayerShape.None)
	}
}

class InputLayerImpl : Layer.SingleInputLayer() {

	override lateinit var outputBuffer: Matrix

	override fun create(previousShape: LayerShape, currentShape: LayerShape) {
		outputBuffer = Matrix(currentShape.width, currentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		MatrixMath.transfer(input, outputBuffer)
		return outputBuffer
	}

}