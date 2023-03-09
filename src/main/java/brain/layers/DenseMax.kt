package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller
import brain.suppliers.ValueSupplier

class DenseMax(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomBinZP,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DenseMaxLayerImpl> {
	companion object {
		const val defaultNameType = "DenseMax"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(units, parentLayer.getShape().height)

	override fun create(): DenseMaxLayerImpl {
		val weightShape = LayerShape(units, parentLayer.getShape().width)

		return DenseMaxLayerImpl(
			activation = activation,
			weightShape = weightShape,
			biasShape = shape,
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

class DenseMaxLayerImpl(
	override val activation: ActivationFunction? = null,
	private val weightShape: LayerShape,
	private val biasShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = DenseMax.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var bias: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(weightShape.width, weightShape.height), true)
		addWeights(kernel)
		bias = WeightData("bias", Matrix(biasShape.width, biasShape.height), trainable = useBias)
		addWeights(bias)
		outputBuffer = Matrix(biasShape.width, biasShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.multiplyMax(input, kernel.matrix, outputBuffer)
		if (useBias) MatrixMath.add(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
