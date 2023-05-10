package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

/**
 * Applies an element wise bias, multiplication and bias on each row, same matrices are used on every (t) row
 * output size is the same as input
 *
 * ((Input + B1) • W) + B2
 */
class PivotNorm(
	private val activation: ActivationFunction? = null,
	private val biasAInit: ValueFiller = Suppliers.Zero,
	private val kernelInit: ValueFiller = Suppliers.Ones,
	private val biasBInit: ValueFiller = Suppliers.Zero,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<PivotNormLayerImpl> {
	companion object {
		const val defaultNameType = "PivotNorm"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): PivotNormLayerImpl {
		return PivotNormLayerImpl(
			activation = activation,
			directShape = shape,
			name = name,
		)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel.matrix, kernelInit)
				Suppliers.fillFull(it.biasA.matrix, biasAInit)
				Suppliers.fillFull(it.biasB.matrix, biasBInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.OnlyBiasMeta {
		return LayerMetaData.OnlyBiasMeta(useBias = true)
	}
}

class PivotNormLayerImpl(
	override val activation: ActivationFunction? = null,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = PivotNorm.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var biasA: WeightData
	lateinit var biasB: WeightData

	override fun init() {
		kernel = WeightData("weight", Matrix(directShape.width, 1), true)
		registerWeight(kernel)
		biasA = WeightData("biasA", Matrix(directShape.width, 1), true)
		registerWeight(biasA)
		biasB = WeightData("biasB", Matrix(directShape.width, 1), true)
		registerWeight(biasB)
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.addSingleToEveryRow(input, biasA.matrix, outputBuffer)
		MatrixMath.hadamardSingleRow(outputBuffer, kernel.matrix, outputBuffer)
		MatrixMath.addSingleToEveryRow(outputBuffer, biasB.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}