package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier

/**
 * Applies an element wise multiplication on activated weight
 * output size is the same as input
 *
 * Input • Act(W)
 */
class FeatureFilter(
	private val weightActivation: ActivationFunction = Activations.Binary,
	private val kernelInit: ValueSupplier = Suppliers.Ones,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FeatureFilterLayerImpl> {
	companion object {
		const val defaultNameType = "FeatureFilter"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): FeatureFilterLayerImpl {
		return FeatureFilterLayerImpl(
			activation = weightActivation,
			directShape = shape,
			name = name,
		)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel.matrix, kernelInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}
}

class FeatureFilterLayerImpl(
	override val activation: ActivationFunction,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FeatureFilter.defaultNameType
	lateinit var kernel: WeightData
	lateinit var weightActivatedBuffer: Matrix
	override lateinit var outputBuffer: Matrix

	override fun init() {
		kernel = WeightData("weight", Matrix(directShape.width, 1), true)
		addWeights(kernel)
		weightActivatedBuffer = Matrix(directShape.width, 1)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		Activations.activate(kernel.matrix, weightActivatedBuffer, activation)
		MatrixMath.hadamardSingleRow(input, weightActivatedBuffer, outputBuffer)
		return outputBuffer
	}

}