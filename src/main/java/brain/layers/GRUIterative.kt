package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.nameType
import brain.matrix.Matrix
import brain.matrix.MatrixMath

class GRUIterative(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val updateActivation: ActivationFunction? = Activations.Sigmoid,
	val resetActivation: ActivationFunction? = Activations.Sigmoid,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<GRUIterativeImpl> {
	companion object {
		const val defaultNameType = "GRUIterative"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = parentLayer.getShape().height, width = units)

	override fun create(): GRUIterativeImpl {
		return GRUIterativeImpl(
			activation = activation,
			updateActivation = updateActivation,
			resetActivation = resetActivation,
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

	override fun getSerializedBuilderData(): LayerMetaData.GRUMeta {
		return LayerMetaData.GRUMeta(
			units = units,
			useBias = useBias,
			reverse = reverse,
			updateActivation = updateActivation?.nameType(),
			resetActivation = resetActivation?.nameType()
		)
	}
}

class GRUIterativeImpl(
	activation: ActivationFunction?,
	updateActivation: ActivationFunction?,
	resetActivation: ActivationFunction?,
	units: Int,
	reverse: Boolean,
	useBias: Boolean,
	parentShape: LayerShape,
	name: String,
) : GRUImpl(activation, updateActivation, resetActivation, units, reverse, useBias, parentShape, name) {
	override val nameType: String = GRUIterative.defaultNameType

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
