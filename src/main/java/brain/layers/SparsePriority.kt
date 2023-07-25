package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller

class SparsePriority(
	private val units: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomHE,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<SparsePriorityLayerImpl> {
	companion object {
		const val defaultNameType = "SparsePriority"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = LayerShape(units, parentLayer.getShape().height)

	override fun create(): SparsePriorityLayerImpl {
		return SparsePriorityLayerImpl(
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

class SparsePriorityLayerImpl(
	val units: Int,
	override val activation: ActivationFunction? = null,
	private val parentShape: LayerShape,
	private val useBias: Boolean = true,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = SparsePriority.defaultNameType
	override lateinit var outputBuffer: Matrix
	lateinit var kernel: WeightData
	lateinit var gateBuffer: Matrix
	lateinit var actBuffer: Matrix
	lateinit var gate: WeightData
	lateinit var bias: WeightData
	private var warm = false

	override fun init() {
		kernel = WeightData("weight", Matrix(units, parentShape.width), true)
		registerWeight(kernel)
		gate = WeightData("gate", Matrix(units, parentShape.width), true)
		registerWeight(gate)
		bias = WeightData("bias", Matrix(units, 1), trainable = useBias)
		registerWeight(bias)
		gateBuffer = Matrix(units, parentShape.width)
		actBuffer = Matrix(units, parentShape.width)
		outputBuffer = Matrix(units, parentShape.height)
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

	override fun onWeightUpdate() {
		warm = true
		Activations.BinaryZeroPos.applyFromMatrixTo(gate.matrix, actBuffer)
		for (y in 0 until gateBuffer.height) {
			for (x in 0 until gateBuffer.width) {

			}
		}
		MatrixMath.hadamard(kernel.matrix, actBuffer, gateBuffer)
	}

}