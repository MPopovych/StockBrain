package layers

import activation.ActivationFunction
import activation.applyFromMatrixTo
import matrix.Matrix

class Activation(
	private val function: ActivationFunction,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<ActivationLayerImpl> {

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val activationShape = parentLayer.getShape()

	override fun create(): ActivationLayerImpl {
		return ActivationLayerImpl(function, activationShape, name).also {
			it.init()
		}
	}

	override fun getShape(): LayerShape {
		return activationShape
	}
}

class ActivationLayerImpl(
	private val function: ActivationFunction,
	private val activationShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {

	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(activationShape.width, activationShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		function.applyFromMatrixTo(input, outputBuffer)
		return outputBuffer
	}

}