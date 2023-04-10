package brain.layers

import brain.matrix.Matrix

class Transpose(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<TransposeLayerImpl> {
	companion object {
		const val defaultNameType = "Transpose"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(parentLayer.getShape().height, parentLayer.getShape().width)

	override fun create(): TransposeLayerImpl {
		return TransposeLayerImpl(
			parentShape = parentLayer.getShape(),
			name = name
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}
}

class TransposeLayerImpl(
	private val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Transpose.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(parentShape.height, parentShape.width)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		val xInd = input.values[0].indices
		for (y in input.values.indices) {
			for (x in xInd) {
				outputBuffer.values[x][y] = input.values[y][x]
			}
		}
		return outputBuffer
	}

}
