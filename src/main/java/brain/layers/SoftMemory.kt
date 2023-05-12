package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.utils.printCyanBr


class SoftMemory(
	val units: Int,
	val options: Int,
	val useBias: Boolean = true,
	private val activation: ActivationFunction? = null,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> List<LayerBuilder<*>>),
) : LayerBuilder.MultiInput<SoftMemoryImpl> {
	companion object {
		const val defaultNameType = "SoftMemory"
	}

	override val nameType: String = defaultNameType
	override val parentLayers: List<LayerBuilder<*>> = parentLayerBlock()

	private val outputShape = parentLayers[0].getShape().copy(width = units)
	private val optionShape = parentLayers[1].getShape()

	init {
		if (optionShape.height != 1 || optionShape.width != options) {
			throw IllegalStateException("option input has to be zero height")
		}
	}

	override fun create(): SoftMemoryImpl {
		return SoftMemoryImpl(
			units = units,
			options = options,
			inputShape = parentLayers[0].getShape(),
			useBias = useBias,
			activation = activation,
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return outputShape
	}

	override fun getSerializedBuilderData(): LayerMetaData.SoftMemoryMeta {
		return LayerMetaData.SoftMemoryMeta(useBias = useBias, units = units, options = options)
	}
}

class SoftMemoryImpl(
	val units: Int,
	val options: Int,
	val inputShape: LayerShape,
	val useBias: Boolean,
	override val activation: ActivationFunction? = null,
	override var name: String,
) : Layer.MultiInputLayer() {
	override val nameType: String = SoftMemory.defaultNameType
	override lateinit var outputBuffer: Matrix

	private lateinit var kernels: ArrayList<WeightData>
	private lateinit var bias: WeightData

	private lateinit var kernelBuffer: Matrix
	private lateinit var kernelSumBuffer: Matrix
	private lateinit var voteBuffer: Matrix

	override fun init() {
		kernels = ArrayList()
		for (i in 0 until options) {
			val localKernel = WeightData("option_$i", Matrix(units, inputShape.width), true)
			Suppliers.fillFull(localKernel.matrix, Suppliers.RandomHE)
			registerWeight(localKernel)
			kernels.add(localKernel)
		}

		bias = WeightData("bias", Matrix(units, 1), trainable = useBias)
		registerWeight(bias)

		outputBuffer = Matrix(units, inputShape.height)
		kernelBuffer = Matrix(units, inputShape.width)
		kernelSumBuffer = Matrix(units, inputShape.width)
		voteBuffer = Matrix(options, 1)
	}

	override fun call(inputs: List<Matrix>): Matrix {
		flushBuffer()
		MatrixMath.flush(kernelBuffer)
		MatrixMath.flush(kernelSumBuffer)
		MatrixMath.flush(voteBuffer)

		Activations.SoftMax.applyFromMatrixTo(inputs[1], voteBuffer)
		for (i in 0 until voteBuffer.width) {
			val ratio = voteBuffer.values[0][i]
			MatrixMath.multiply(kernels[i].matrix, ratio, kernelSumBuffer)
			MatrixMath.add(kernelBuffer, kernelSumBuffer, kernelBuffer)
		}
		MatrixMath.multiply(inputs[0], kernelBuffer, outputBuffer)
		if (useBias) MatrixMath.addSingleToEveryRow(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
