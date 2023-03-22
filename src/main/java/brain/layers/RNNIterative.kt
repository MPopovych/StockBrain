package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath

class RNNIterative(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<RNNIterativeImpl> {
	companion object {
		const val defaultNameType = "RNNIterative"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = parentLayer.getShape().height, width = units)

	override fun create(): RNNIterativeImpl {
		return RNNIterativeImpl(
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

class RNNIterativeImpl(
	activation: ActivationFunction?,
	units: Int,
	reverse: Boolean,
	useBias: Boolean,
	parentShape: LayerShape,
	name: String,
) : RNNImpl(activation, units, reverse, useBias, parentShape, name) {
	override val nameType: String = RNNIterative.defaultNameType

	override fun init() {
		super.init()

		outputBuffer = Matrix(units, parentShape.height)
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
		for ((i, t) in rowIterator.withIndex()) {
			MatrixMath.transferSingleRow(input, cellStateBufferCurrent, t, 0)
			transferCurrentToOutput()

			MatrixMath.transferSingleRow(cellStateBufferPrev, outputBuffer, 0, i)
		}
		return outputBuffer
	}

}
