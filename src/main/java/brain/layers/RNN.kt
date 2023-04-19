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

open class RNNImpl(
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
	private lateinit var hBias: WeightData
	private lateinit var oBias: WeightData

	private val iBufferM1: Matrix = Matrix(units, 1)
	private val hBufferM1: Matrix = Matrix(units, 1)

	protected lateinit var cellStateBufferCurrent: Matrix
	protected lateinit var cellStateBufferPrev: Matrix
	override lateinit var outputBuffer: Matrix

	override fun init() {
		iKernel = WeightData("iKernel", Matrix(units, parentShape.width, Suppliers.RandomHE), trainable = true)
		addWeights(iKernel)
		hKernel = WeightData("hKernel", Matrix(units, units, Suppliers.RandomHE), trainable = true)
		addWeights(hKernel)

		for (w in weights.values) {
			Suppliers.fillFull(w.matrix, Suppliers.RandomHE)
		}

		hBias = WeightData("hBias", Matrix(units, 1), trainable = useBias)
		addWeights(hBias)
		oBias = WeightData("oBias", Matrix(units, 1), trainable = useBias)
		addWeights(oBias)

		cellStateBufferCurrent = Matrix(parentShape.width, 1)
		cellStateBufferPrev = Matrix(units, 1)
		outputBuffer = Matrix(units, 1)
	}

	protected fun transferCurrentToOutput() {
		MatrixMath.multiply(cellStateBufferCurrent, iKernel.matrix, iBufferM1)

		MatrixMath.multiply(cellStateBufferPrev, hKernel.matrix, hBufferM1)
		if (useBias) MatrixMath.add(hBufferM1, hBias.matrix, hBufferM1)
		// save to previous, make sure its flushed
		MatrixMath.add(iBufferM1, hBufferM1, cellStateBufferPrev)
		if (useBias) MatrixMath.add(cellStateBufferPrev, oBias.matrix, cellStateBufferPrev)
		activation?.also {
			Activations.activate(cellStateBufferPrev, cellStateBufferPrev, it)
		}
	}

	private var cachedIterator: IntArray? = null // micro-optimisation for iterative operations
	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(cellStateBufferPrev) // h_prev


		val rowIterator = cachedIterator ?: (0 until input.height).let {
			var list = (0 until input.height).toList()
			if (reverse) {
				list = list.asReversed()
			}
			val array = list.toIntArray()
			cachedIterator = array
			return@let array
		}
		for (t in rowIterator) {
			MatrixMath.transferSingleRow(input, cellStateBufferCurrent, t, 0)
			transferCurrentToOutput()
		}
		MatrixMath.transfer(cellStateBufferPrev, outputBuffer)
		return outputBuffer
	}

}
