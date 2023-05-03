package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

class Sparse(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomHE,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<SparseLayerImpl> {
	companion object {
		const val defaultNameType = "Sparse"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(units, parentLayer.getShape().height)

	override fun create(): SparseLayerImpl {
		return SparseLayerImpl(
			units = units,
			activation = activation,
			parentShape = parentLayer.getShape(),
			useBias = useBias,
			name = name
		)
			.also {
				it.init()
				Suppliers.fillFull(it.kernel.matrix, kernelInit)
				Suppliers.fillFull(it.gate.matrix, kernelInit)
				Suppliers.fillFull(it.bias.matrix, biasInit)
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.OnlyBiasMeta {
		return LayerMetaData.OnlyBiasMeta(useBias = useBias)
	}
}

class SparseLayerImpl(
	val units: Int,
	override val activation: ActivationFunction? = null,
	private val parentShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer(), LayerWarmupMode {
	override val nameType: String = Sparse.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var gateBuffer: Matrix
	lateinit var gate: WeightData
	lateinit var bias: WeightData

	private var warm = false

	override fun init() {
		kernel = WeightData("weight", Matrix(units, parentShape.width), true)
		addWeights(kernel)
		gate = WeightData("gate", Matrix(units, parentShape.width), true)
		addWeights(gate)
		bias = WeightData("bias", Matrix(units, 1), trainable = useBias)
		addWeights(bias)
		gateBuffer = Matrix(units, parentShape.width)
		outputBuffer = Matrix(units, parentShape.height)
	}

	override fun warmup() {
		Activations.BinaryNegPos.applyFromMatrixTo(gate.matrix, gateBuffer)
		MatrixMath.hadamard(kernel.matrix, gateBuffer, gateBuffer)
		warm = true
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		if (!warm) {
			throw IllegalStateException("Warmup not called")
		}
		MatrixMath.multiply(input, gateBuffer, outputBuffer)
		if (useBias) MatrixMath.addSingleToEveryRow(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
