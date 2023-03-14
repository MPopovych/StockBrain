package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class GRUIterative(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
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
		return LayerMetaData.GRUMeta(useBias = useBias, reverse = reverse)
	}
}

class GRUIterativeImpl(
	activation: ActivationFunction?,
	units: Int,
	reverse: Boolean,
	useBias: Boolean,
	parentShape: LayerShape,
	name: String,
) : GRUImpl(activation, units, reverse, useBias, parentShape, name) {
	override val nameType: String = GRUIterative.defaultNameType

	override fun init() {
		super.init()
		outputBuffer = Matrix(units, parentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(cellStateBufferPrev) // h_prev

		var rowIterator = (0 until input.height).toList()
		if (reverse) {
			rowIterator = rowIterator.asReversed()
		}
		for ((i, t) in rowIterator.withIndex()) {
			MatrixMath.transferSingleRow(input, cellStateBufferCurrent, t, 0)

			transferCurrentToOutput()

			MatrixMath.transferSingleRow(cellStateBufferPrev, outputBuffer, 0, i)
		}
		return outputBuffer
	}

}
