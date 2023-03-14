package brain.layers

import brain.matrix.Matrix


class AttentionMultiply(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<AttentionMultiplyImpl> {
	constructor(vararg layer: LayerBuilder<*>, name: String = Layer.DEFAULT_NAME): this(name, { layer.toList() })

	companion object {
		const val defaultNameType = "ConcatMultiply"
	}

	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	private val parentHeight = parentLayers.map { it.getShape().height }.distinct()
	private val parentWidth = parentLayers.map { it.getShape().width }.distinct()

	init {
		if (parentWidth.size != 1) {
			throw IllegalStateException("Illegal widths of parents: ${parentWidth}}")
		}
		if (parentHeight.size != 1) {
			throw IllegalStateException("Illegal heights of parents: ${parentHeight}}")
		}
	}

	private val concatShape = parentLayers.first().getShape()

	override fun create(): AttentionMultiplyImpl {
		return AttentionMultiplyImpl(concatShape, name).also {
			it.init()
		}
	}

	override fun getShape(): LayerShape {
		return concatShape
	}

}

class AttentionMultiplyImpl(private val concatShape: LayerShape, override var name: String) : Layer.MultiInputLayer() {
	override val nameType: String = AttentionMultiply.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(concatShape.width, concatShape.height)
	}

	override fun call(inputs: List<Matrix>): Matrix {
		flushBuffer()

		for (y in 0 until concatShape.height) {
			for (x in 0 until concatShape.width) {
				var m = 1f
				for (input in inputs) {
					m *= input.values[y][x]
				}
				outputBuffer.values[y][x] = m
			}
		}
		return outputBuffer
	}

}