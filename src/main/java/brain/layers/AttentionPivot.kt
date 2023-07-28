package brain.layers

import brain.matrix.Matrix
import kotlin.math.max
import kotlin.math.min


class AttentionPivot(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<AttentionPivotImpl> {
	constructor(vararg layer: LayerBuilder<*>, name: String = Layer.DEFAULT_NAME) : this(name, { layer.toList() })

	companion object {
		const val defaultNameType = "AttentionPivot"
	}

	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	init {
		if (parentLayers.size != 2) {
			throw IllegalStateException("Illegal count of parents: ${parentLayers.size}}")
		}
		if (parentLayers[0].getShape().width != parentLayers[1].getShape().width) {
			throw IllegalStateException("Illegal widths of parents: ${parentLayers[0].getShape()} ${parentLayers[1].getShape()}}")
		}
		if (parentLayers[0].getShape().height != parentLayers[1].getShape().height) {
			throw IllegalStateException("Illegal heights of parents: ${parentLayers[0].getShape()} ${parentLayers[1].getShape()}}")
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
				val pivot = inputs[1].values[y][x]
				outputBuffer.values[y][x] = inputs[0].values[y][x] * (1f + max(-0.1f, min(0.1f, pivot)))
			}
		}
		return outputBuffer
	}

}