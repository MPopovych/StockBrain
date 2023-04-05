package brain.layers

import brain.matrix.Matrix


class AttentionPivot(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<AttentionPivotImpl> {
	constructor(vararg layer: LayerBuilder<*>, name: String = Layer.DEFAULT_NAME): this(name, { layer.toList() })

	companion object {
		const val defaultNameType = "AttentionPivot"
	}

	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	init {
		if (parentLayers.size > 2) {
			throw IllegalStateException("Illegal count of parents: ${parentLayers.size}}")
		}
		if (parentLayers[0].getShape().width != parentLayers[1].getShape().width ) {
			throw IllegalStateException("Illegal widths of parents: ${parentLayers[0].getShape()} ${parentLayers[1].getShape()}}")
		}
		if (parentLayers[1].getShape().height != 1) {
			throw IllegalStateException("Illegal heights of parent 2: ${parentLayers[1].getShape()}}")
		}
	}

	private val concatShape = parentLayers.first().getShape()

	override fun create(): AttentionPivotImpl {
		return AttentionPivotImpl(concatShape, name).also {
			it.init()
		}
	}

	override fun getShape(): LayerShape {
		return concatShape
	}

}

class AttentionPivotImpl(private val concatShape: LayerShape, override var name: String) : Layer.MultiInputLayer() {
	override val nameType: String = AttentionPivot.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(concatShape.width, concatShape.height)
	}

	override fun call(inputs: List<Matrix>): Matrix {
		flushBuffer()

		for (y in 0 until concatShape.height) {
			for (x in 0 until concatShape.width) {
				var m = 0f
				m += inputs[0].values[y][x]
				m += inputs[1].values[0][x]
				outputBuffer.values[y][x] = m
			}
		}
		return outputBuffer
	}

}