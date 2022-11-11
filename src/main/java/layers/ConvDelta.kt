package layers

import activation.ActivationFunction
import activation.Activations
import matrix.Matrix
import matrix.MatrixMath

class ConvDelta(
	private val activation: ActivationFunction? = null,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<ConvDeltaImpl> {
	companion object {
		const val defaultNameType = "ConvDelta"
	}
	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = parentLayer.getShape().let { s -> s.copy(height = s.height - 1) }
	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): ConvDeltaImpl {
		return ConvDeltaImpl(activation = activation,
			directShape = shape,
			name = name)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}
}

class ConvDeltaImpl(
	override val activation: ActivationFunction? = null,
	val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = ConvDelta.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		MatrixMath.convolutionSubtract(input, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
