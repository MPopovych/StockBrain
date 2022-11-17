package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier

class Dense(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = Suppliers.RandomRangeNP,
	private val biasInit: ValueSupplier = Suppliers.Zero,
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
		val weightShape = LayerShape(units, parentLayer.getShape().width)

		return DenseLayerImpl(
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

	override fun getSerializedBuilderData(): LayerMetaData.DenseMeta {
		return LayerMetaData.DenseMeta(useBias = useBias)
	}
}

class DenseLayerImpl(
	override val activation: ActivationFunction? = null,
	private val weightShape: LayerShape,
	private val biasShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Dense.defaultNameType
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
		MatrixMath.multiply(input, kernel.matrix, outputBuffer)
		if (useBias) MatrixMath.add(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
