package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class RNN(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<RNNImpl> {
	companion object {
		const val defaultNameType = "RNN"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): RNNImpl {
		return RNNImpl(
			activation = activation,
			units = units,
			reverse = reverse,
			useBias = useBias,
			parentShape = parentLayer.getShape(),
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.RNNMeta {
		return LayerMetaData.RNNMeta(useBias = useBias, reverse = reverse)
	}
}

class RNNImpl(
	override val activation: ActivationFunction?,
	val units: Int,
	val reverse: Boolean,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = RNN.defaultNameType

	private lateinit var iKernel: WeightData
	private lateinit var hKernel: WeightData
	private lateinit var bias: WeightData

	private val iBufferM1: Matrix = Matrix(units, 1)
	private val hBufferM1: Matrix = Matrix(units, 1)

	lateinit var cellStateBufferCurrent: Matrix
	lateinit var cellStateBufferPrev: Matrix
	override lateinit var outputBuffer: Matrix

	override fun init() {
		iKernel = WeightData("iKernel", Matrix(units, parentShape.width), trainable = true)
		addWeights(iKernel)
		hKernel = WeightData("hKernel", Matrix(units, units), trainable = true)
		addWeights(hKernel)

		for (w in weights.values) {
			Suppliers.fillFull(w.matrix, Suppliers.RandomHE)
		}

		bias = WeightData("zBias", Matrix(units, 1), trainable = useBias)
		addWeights(bias)

		cellStateBufferCurrent = Matrix(parentShape.width, 1)
		cellStateBufferPrev = Matrix(units, 1)
		outputBuffer = Matrix(units, 1)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(cellStateBufferCurrent) // x
		MatrixMath.flush(cellStateBufferPrev) // h_prev
		MatrixMath.flush(iBufferM1)
		MatrixMath.flush(hBufferM1)

		var rowIterator = (0 until input.height).toList()
		if (reverse) {
			rowIterator = rowIterator.asReversed()
		}
		for (t in rowIterator) {
			MatrixMath.transferSingleRow(input, cellStateBufferCurrent, t, 0)
			MatrixMath.multiply(cellStateBufferCurrent, iKernel.matrix, iBufferM1)
			MatrixMath.multiply(cellStateBufferPrev, hKernel.matrix, hBufferM1)
			MatrixMath.add(iBufferM1, hBufferM1, cellStateBufferPrev)
			activation?.also {
				Activations.activate(cellStateBufferPrev, cellStateBufferPrev, it)
			}
		}
		MatrixMath.transfer(cellStateBufferPrev, outputBuffer)
		return outputBuffer
	}

}
