package layers

import activation.ActivationFunction
import activation.Activations
import matrix.Matrix
import matrix.MatrixMath
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import suppliers.ValueSupplier
import suppliers.ZeroSupplier

class Dense(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = RandomRangeSupplier.INSTANCE,
	private val biasInit: ValueSupplier = ZeroSupplier.INSTANCE,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DenseLayerImpl> {
	companion object {
		const val defaultNameType = "Dense"
	}
	override val nameType: String = defaultNameType
	private val shape = LayerShape(units, 1)

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	override fun create(): DenseLayerImpl {
		val weightShape = LayerShape(units, parentLayer.getShape().width)

		return DenseLayerImpl(activation = activation, weightShape = weightShape, biasShape = shape, name = name)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel, kernelInit)
				Suppliers.fillFull(it.bias, biasInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData? {
		return activation?.let {
			LayerMetaData.DenseMeta(
				activation = Activations.serialize(it)
			)
		}
	}
}

class DenseLayerImpl(
	val activation: ActivationFunction? = null,
	val weightShape: LayerShape,
	val biasShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Dense.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: Matrix
	lateinit var bias: Matrix

	override fun init() {
		kernel = Matrix(weightShape.width, weightShape.height)
		addWeights("weight", kernel, true)
		bias = Matrix(biasShape.width, biasShape.height)
		addWeights("bias", bias, true)
		outputBuffer = Matrix(biasShape.width, biasShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.multiply(input, kernel, outputBuffer)
		MatrixMath.add(outputBuffer, bias, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer//.also { it.print() }
	}

}

data class DenseSerialized(
	val activation: String?,
)