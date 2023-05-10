package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

/**
 * Splits every feature in X units and applies bias only
 */
class Disperse(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val biasInit: ValueFiller = Suppliers.RandomHE,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DisperseLayerImpl> {
	companion object {
		const val defaultNameType = "Disperse"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(units * parentLayer.getShape().width, parentLayer.getShape().height)

	override fun create(): DisperseLayerImpl {
		return DisperseLayerImpl(
			units = units,
			activation = activation,
			parentShape = parentLayer.getShape(),
			name = name
		)
			.also {
				it.init()
				Suppliers.fillFull(it.bias.matrix, biasInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData {
		return LayerMetaData.OnlyUnitsMeta(units)
	}
}

class DisperseLayerImpl(
	val units: Int,
	override val activation: ActivationFunction? = null,
	private val parentShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Disperse.defaultNameType
	override lateinit var outputBuffer: Matrix

	lateinit var bias: WeightData

	override fun init() {
		bias = WeightData("bias", Matrix(units * parentShape.width, 1), trainable = useBias)
		registerWeight(bias)
		outputBuffer = Matrix(units * parentShape.width, parentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.disperseFeature(input, bias.matrix, outputBuffer, units)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
