package layers

import matrix.Matrix
import matrix.MatrixMath
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import suppliers.ValueSupplier
import suppliers.ZeroSupplier

class Dense(
	val units: Int,
	val kernelInit: ValueSupplier = RandomRangeSupplier.INSTANCE,
	val biasInit: ValueSupplier = ZeroSupplier.INSTANCE,
	parentLayerBlock: (() -> LayerBuilder<*>)
): LayerBuilder.SingleInput<DenseLayerImpl> {

	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	override fun createFrom(previousShape: LayerShape): DenseLayerImpl {
		return DenseLayerImpl().also {
			it.create(previousShape, LayerShape(units, 1))
			Suppliers.fillFull(it.kernel, kernelInit)
			Suppliers.fillFull(it.bias, biasInit)
		}
	}
}

class DenseLayerImpl : Layer.SingleInputLayer() {

	override lateinit var outputBuffer: Matrix
	lateinit var kernel: Matrix
	lateinit var bias: Matrix

	override fun create(previousShape: LayerShape, currentShape: LayerShape) {
		val weightShape = LayerShape(currentShape.width, previousShape.width)

		kernel = Matrix(weightShape.width, weightShape.height)
		addWeights("weight", kernel, true)
		bias = Matrix(currentShape.width, currentShape.height)
		addWeights("bias", bias, true)
		outputBuffer = Matrix(currentShape.width, currentShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.multiply(input, kernel, outputBuffer)
		MatrixMath.add(outputBuffer, bias, outputBuffer)
		return outputBuffer
	}

}