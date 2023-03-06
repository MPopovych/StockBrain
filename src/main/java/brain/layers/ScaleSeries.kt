package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier

class ScaleSeries(
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = Suppliers.Ones,
	private val biasInit: ValueSupplier = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<ScaleSeriesLayerImpl> {
	companion object {
		const val defaultNameType = "ScaleSeries"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): ScaleSeriesLayerImpl {
		return ScaleSeriesLayerImpl(
			activation = activation,
			directShape = shape,
			name = name,
			useBias = useBias
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

	override fun getSerializedBuilderData(): LayerMetaData.DirectMeta {
		return LayerMetaData.DirectMeta(useBias = useBias)
	}
}

class ScaleSeriesLayerImpl(
	override val activation: ActivationFunction? = null,
	private val directShape: LayerShape,
	private val useBias: Boolean,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = ScaleSeries.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var bias: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(directShape.width, 1), true)
		addWeights(kernel)
		bias = WeightData("bias", Matrix(directShape.width, 1), trainable = useBias)
		addWeights(bias)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.hadamardSingleRow(input, kernel.matrix, outputBuffer)
		if (useBias) MatrixMath.addSingleRow(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}