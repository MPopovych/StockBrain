package layers

import matrix.Matrix
import utils.getShape



class Concat(
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<ConcatImpl> {
	companion object {
		const val defaultNameType = "Concat"
	}
	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	val concatShape = LayerShape(parentLayers.sumOf { it.getShape().width }, 1)

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
			for (x in 0 until localWidth) {
				outputBuffer.values[i + x][0] = input.values[x][0]
			}
			i += localWidth
		}
		return outputBuffer
	}

}