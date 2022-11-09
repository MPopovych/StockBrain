package layers

import activation.ActivationFunction
import activation.applyFromMatrixTo
import matrix.Matrix

class Activation (
	private val function: ActivationFunction,
	parentLayerBlock: (() -> LayerBuilder<*>)
): LayerBuilder.SingleInput<ActivationLayerImpl> {

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	override fun createFrom(previousShape: LayerShape): ActivationLayerImpl {
		return ActivationLayerImpl(function).also {
			it.create(previousShape, previousShape)
		}
	}
}

class ActivationLayerImpl(private val function: ActivationFunction) : Layer.SingleInputLayer() {

	override lateinit var outputBuffer: Matrix

	override fun create(previousShape: LayerShape, currentShape: LayerShape) {
		outputBuffer = Matrix(currentShape.width, currentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		function.applyFromMatrixTo(input, outputBuffer)
		return outputBuffer
	}

}