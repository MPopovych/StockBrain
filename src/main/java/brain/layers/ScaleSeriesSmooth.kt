package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

/**
 * Applies an element wise multiplication and bias on each row, same matrices are used on every (t) row
 * output size is the same as input
 */
class ScaleSeriesSmooth(
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.Ones,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<ScaleSeriesSmoothLayerImpl> {
	companion object {
		const val defaultNameType = "ScaleSeriesSmooth"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): ScaleSeriesSmoothLayerImpl {
		return ScaleSeriesSmoothLayerImpl(
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

	override fun getSerializedBuilderData(): LayerMetaData.OnlyBiasMeta {
		return LayerMetaData.OnlyBiasMeta(useBias = useBias)
	}
}

class ScaleSeriesSmoothLayerImpl(
	override val activation: ActivationFunction? = null,
	private val directShape: LayerShape,
	private val useBias: Boolean,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = ScaleSeriesSmooth.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var bias: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(directShape.width, 2), true)
		addWeights(kernel)
		bias = WeightData("bias", Matrix(directShape.width, 2), trainable = useBias)
		addWeights(bias)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		for (x in 0 until input.width) {
			val weightXTop = kernel.matrix.values[0][x]
			val weightXBot = kernel.matrix.values[1][x]
			for (y in 0 until input.height) {
				val weightXPoint = x.toFloat() / (input.width - 1)
				outputBuffer.values[y][x] =
					input.values[y][x] * (weightXTop * (1 - weightXPoint) + weightXBot * weightXPoint)
			}
		}
		if (useBias) {
			for (x in 0 until input.width) {
				val weightXTop = bias.matrix.values[0][x]
				val weightXBot = bias.matrix.values[1][x]
				for (y in 0 until input.height) {
					val weightXPoint = x.toFloat() / (input.width - 1)
					outputBuffer.values[y][x] =
						outputBuffer.values[y][x] + (weightXTop * (1 - weightXPoint) + weightXBot * weightXPoint)
				}
			}
		}
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}