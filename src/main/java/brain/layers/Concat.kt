package brain.layers

import brain.matrix.Matrix
import brain.utils.getShape


class Concat(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<ConcatImpl> {
	constructor(vararg layer: LayerBuilder<*>, name: String = Layer.DEFAULT_NAME) : this(name, { layer.toList() })

	companion object {
		const val defaultNameType = "Concat"
	}

	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	private val parentHeight = parentLayers.map { it.getShape().height }.distinct()

	init {
		if (parentHeight.size != 1) {
			throw IllegalStateException("Illegal heights of parents: ${parentHeight}}")
		}
	}

	private val parentHeights = parentHeight.first()
	private val concatShape = LayerShape(parentLayers.sumOf { it.getShape().width }, parentHeights)

	override fun create(): ConcatImpl {
		return ConcatImpl(concatShape, name).also {
			it.init()
		}
	}

	override fun getShape(): LayerShape {
		return concatShape
	}

}

class ConcatImpl(private val concatShape: LayerShape, override var name: String) : Layer.MultiInputLayer() {
	override val nameType: String = Concat.defaultNameType
	override lateinit var outputBuffer: Matrix

	override fun init() {
		outputBuffer = Matrix(concatShape.width, concatShape.height)
	}

	override fun call(inputs: List<Matrix>): Matrix {
		flushBuffer()

		var i = 0
		for (input in inputs) {
			val localWidth = input.getShape().width
			for (y in 0 until input.height) {
				input.values[y].copyInto(outputBuffer.values[y], destinationOffset = i)
			}
			i += localWidth
		}
		return outputBuffer
	}

}