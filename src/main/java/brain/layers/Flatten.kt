package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath

class Flatten(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FlattenImpl> {
	companion object {
		const val defaultNameType = "Flatten"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = parentLayer.getShape().let { s -> s.copy(width = s.width * s.height, height = 1) }

	init {
		if (shape.width < 2) throw IllegalStateException("width cant be one or less")
	}

	override fun create(): FlattenImpl {
		return FlattenImpl(directShape = shape, name = name)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}
}

class FlattenImpl(
	val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = Flatten.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		MatrixMath.convolutionFlatten(input, outputBuffer)
		return outputBuffer
	}

}
