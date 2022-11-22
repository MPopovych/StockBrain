package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath

class FeatureMask(
	private val filterIndexes: Set<Int>,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FeatureMaskImpl> {
	companion object {
		const val defaultNameType = "FeatureMask"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape().copy(width = filterIndexes.size)

	override fun create(): FeatureMaskImpl {
		return FeatureMaskImpl(
			filterIndexes = filterIndexes,
			directShape = shape,
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.FeatureMaskMeta {
		return LayerMetaData.FeatureMaskMeta(filterIndexes)
	}
}

class FeatureMaskImpl(
	private val filterIndexes: Set<Int>,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FeatureMask.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		MatrixMath.transferWithFilter(input, outputBuffer, filterIndexes)
		return outputBuffer
	}

}