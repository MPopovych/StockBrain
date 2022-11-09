package layers

import matrix.Matrix
import matrix.MatrixMath
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import suppliers.ValueSupplier
import suppliers.ZeroSupplier

class Direct(
	val kernelInit: ValueSupplier = RandomRangeSupplier.INSTANCE,
	val biasInit: ValueSupplier = ZeroSupplier.INSTANCE,
	parentLayerBlock: (() -> LayerBuilder<*>)
): LayerBuilder.SingleInput<DirectLayerImpl> {

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	override fun createFrom(previousShape: LayerShape): DirectLayerImpl {
		return DirectLayerImpl().also {
			it.create(previousShape, previousShape)
			Suppliers.fillFull(it.kernel, kernelInit)
			Suppliers.fillFull(it.bias, biasInit)
		}
	}
}

class DirectLayerImpl : Layer.SingleInputLayer() {

	override lateinit var outputBuffer: Matrix
	lateinit var kernel: Matrix
	lateinit var bias: Matrix

	override fun create(previousShape: LayerShape, currentShape: LayerShape) {
		kernel = Matrix(currentShape.width, currentShape.height)
		addWeights("weight", kernel, true)
		bias = Matrix(currentShape.width, currentShape.height)
		addWeights("bias", bias, true)
		outputBuffer = Matrix(currentShape.width, currentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.hadamard(input, kernel, outputBuffer)
		MatrixMath.add(outputBuffer, bias, outputBuffer)
		return outputBuffer
	}

}