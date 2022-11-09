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
		return DirectLayerImpl(activation = activation,
			directShape = shape,
			name = name)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel, kernelInit)
				Suppliers.fillFull(it.bias, biasInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): Any? {
		return activation?.let {
			DirectSerialized(
				activation = Activations.serialize(it)
			)
		}
	}
}

class DirectLayerImpl(
	val activation: ActivationFunction? = null,
	val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Direct.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: Matrix
	lateinit var bias: Matrix

	override fun init() {
		kernel = Matrix(directShape.width, directShape.height)
		addWeights("weight", kernel, true)
		bias = Matrix(directShape.width, directShape.height)
		addWeights("bias", bias, true)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.hadamard(input, kernel, outputBuffer)
		MatrixMath.add(outputBuffer, bias, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}

data class DirectSerialized(
	val activation: String?,
)