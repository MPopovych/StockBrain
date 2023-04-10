package brain.layers

import brain.matrix.Matrix

class Pivot(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<PivotImpl> {
	companion object {
		const val defaultNameType = "Pivot"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(parentLayer.getShape().width, parentLayer.getShape().height)

	override fun create(): PivotImpl {
		return PivotImpl(
			parentShape = parentLayer.getShape(),
			name = name
		)
	}

	override fun getShape(): LayerShape {
		return shape
	}
}

class PivotImpl(
	private val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Pivot.defaultNameType
	override var outputBuffer: Matrix = Matrix(parentShape.width, parentShape.height)

	override fun init() {
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		for (x in input.values[0].indices) {
			val starting = input.values[0][x]
			for (y in input.values.indices) {
				outputBuffer.values[y][x] = input.values[y][x] - starting
			}
		}

		return outputBuffer
	}

}
