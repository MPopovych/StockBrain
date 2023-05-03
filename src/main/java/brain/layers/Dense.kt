package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

class Dense(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomHE,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DenseLayerImpl> {
	companion object {
		const val defaultNameType = "Dense"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(units, parentLayer.getShape().height)

	override fun create(): DenseLayerImpl {
		return DenseLayerImpl(
			units = units,
			activation = activation,
			parentShape = parentLayer.getShape(),
			useBias = useBias,
			name = name
		)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel.matrix, kernelInit)
				Suppliers.fillFull(it.bias.matrix, biasInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.OnlyBiasMeta {
		return LayerMetaData.OnlyBiasMeta(useBias = useBias)
	}
}

class DenseLayerImpl(
	val units: Int,
	override val activation: ActivationFunction? = null,
	private val parentShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Dense.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var bias: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(units, parentShape.width), true)
		addWeights(kernel)
		bias = WeightData("bias", Matrix(units, 1), trainable = useBias)
		addWeights(bias)
		outputBuffer = Matrix(units, parentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.multiply(input, kernel.matrix, outputBuffer)
		if (useBias) MatrixMath.addSingleToEveryRow(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
