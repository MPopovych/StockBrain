package brain.layers

import brain.matrix.Matrix

class RepeatSingle(
	private val times: Int,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<RepeatSingleLayerImpl> {
	companion object {
		const val defaultNameType = "RepeatSingle"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape().copy(height = times)

	init {
		if (parentLayer.getShape().height != 1) {
			throw IllegalStateException("Illegal height of parent: ${parentLayer.getShape().height}}")
		}
	}

	override fun create(): RepeatSingleLayerImpl {
		return RepeatSingleLayerImpl(
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

	override fun getSerializedBuilderData(): LayerMetaData.OnlyUnitsMeta {
		return LayerMetaData.OnlyUnitsMeta(units = times)
	}
}

class RepeatSingleLayerImpl(
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = RepeatSingle.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		for (y in outputBuffer.values.indices) {
			for (x in input.values.indices) {
				outputBuffer.values[y][x] = input.values[0][x]
			}
		}
		return outputBuffer
	}

}