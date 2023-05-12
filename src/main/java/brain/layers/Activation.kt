package brain.layers

import brain.activation.ActivationFunction
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix

class Activation(
	private val activation: ActivationFunction,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<ActivationLayerImpl> {
	companion object {
		const val defaultNameType = "Activation"
	}

	override val nameType: String = defaultNameType

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val activationShape = parentLayer.getShape()

	override fun create(): ActivationLayerImpl {
		return ActivationLayerImpl(activation, activationShape, name).also {
			it.init()
		}
	}

	override fun getShape(): LayerShape {
		return activationShape
	}

}

class ActivationLayerImpl(
	override val activation: ActivationFunction,
	private val activationShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Activation.defaultNameType

	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(activationShape.width, activationShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		activation.applyFromMatrixTo(input, outputBuffer)
		return outputBuffer
	}

}
