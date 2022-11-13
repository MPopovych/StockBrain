package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath


class TimeMask(
	val fromStart: Int,
	val fromEnd: Int,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<TimeMaskImpl> {
	companion object {
		const val defaultNameType = "TimeMask"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = parentLayer.getShape().let { it.copy(height = it.height - fromStart - fromEnd) }

	init {
		if (shape.height < 1) throw IllegalStateException("Height cant be less than one")
	}

	override fun create(): TimeMaskImpl {
		return TimeMaskImpl(fromStart = fromStart, fromEnd = fromEnd, directShape = shape, name = name)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.TimeMaskMeta {
		return LayerMetaData.TimeMaskMeta(fromStart = fromStart, fromEnd = fromEnd)
	}
}

class TimeMaskImpl(
	private val fromStart: Int,
	private val fromEnd: Int,
	val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = TimeMask.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		MatrixMath.transferHeightRange(input, outputBuffer, fromStart, fromEnd)
		return outputBuffer
	}

}
