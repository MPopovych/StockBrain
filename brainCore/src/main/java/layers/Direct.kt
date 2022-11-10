package layers

import activation.ActivationFunction
import activation.Activations
import matrix.Matrix
import matrix.MatrixMath
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import suppliers.ValueSupplier
import suppliers.ZeroSupplier

class Direct(
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = RandomRangeSupplier.INSTANCE,
	private val biasInit: ValueSupplier = ZeroSupplier.INSTANCE,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DirectLayerImpl> {
	companion object {
		const val defaultNameType = "Direct"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): DirectLayerImpl {
		return DirectLayerImpl(
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

	override fun getSerializedBuilderData(): LayerMetaData {
		return LayerMetaData.DirectMeta(useBias = useBias)
	}
}

class DirectLayerImpl(
	override val activation: ActivationFunction? = null,
	private val directShape: LayerShape,
	private val useBias: Boolean,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Direct.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var bias: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(directShape.width, directShape.height), true)
		addWeights(kernel)
		bias = if (useBias) {
			WeightData("bias", Matrix(directShape.width, directShape.height), true)
		} else {
			WeightData("bias", Matrix(directShape.width, directShape.height, Suppliers.Zero), false)
		}
		addWeights(bias)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.hadamard(input, kernel.matrix, outputBuffer)
		if (useBias) MatrixMath.add(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}