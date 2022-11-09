package layers

import matrix.Matrix
import utils.getShape
import utils.printGreen
import utils.printRed

class Concat(
	parentLayerBlock: (() -> List<LayerBuilder<*>>)
): LayerBuilder.MultiInput<ConcatImpl> {
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	override fun createFromList(previousShapes: List<LayerShape>): ConcatImpl {
		return ConcatImpl().also {
			it.create(LayerShape.None, createShape(previousShapes))
		}
	}

	fun createShape(previousShapes: List<LayerShape>): LayerShape {
		val width = previousShapes.sumOf { it.width }
		return LayerShape(width, 1)
	}

}

class ConcatImpl : Layer.MultiInputLayer() {

	override lateinit var outputBuffer: Matrix

	override fun create(previousShape: LayerShape, currentShape: LayerShape) {
		outputBuffer = Matrix(currentShape.width, currentShape.height)
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