package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier

/**
 * Output width = input width
 * Output height = (input height - kernelSize + 1) * units
 */

class FeatureConv(
	val units: Int,
	val kernelSize: Int,
	private val activation: ActivationFunction? = null,
	private val useBias: Boolean = true,
	private val kernelInit: ValueSupplier = Suppliers.RandomHE,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FeatureConvImpl> {
	companion object {
		const val defaultNameType = "FeatureConv"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = parentLayer.getShape().copy(height = (parentLayer.getShape().height - kernelSize + 1) * units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): FeatureConvImpl {
		return FeatureConvImpl(
			units = units,
			kernelSize = kernelSize,
			parentShape = parentLayer.getShape(),
			useBias = useBias,
			activation = activation,
			name = name,
		)
			.also {
				it.init()
				it.kernels.onEach { w ->
					Suppliers.fillFull(w.matrix, kernelInit)
				}
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.FeatureConvMeta {
		return LayerMetaData.FeatureConvMeta(useBias = useBias, units = units, kernels = kernelSize)
	}
}

class FeatureConvImpl(
	val units: Int,
	val kernelSize: Int,
	val parentShape: LayerShape,
	val useBias: Boolean,
	override val activation: ActivationFunction? = null,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FeatureConv.defaultNameType
	override lateinit var outputBuffer: Matrix
	private lateinit var transposeFeatureBuffer: Matrix
	private lateinit var transposeOutputBuffer: Matrix

	internal lateinit var kernels: ArrayList<WeightData>
	private lateinit var biases: ArrayList<WeightData>

	override fun init() {
		kernels = ArrayList()
		for (i in 0 until parentShape.width) {
			val localKernel = WeightData("weight_f$i", Matrix(units, kernelSize), true)
			addWeights(localKernel)
			kernels.add(localKernel)
		}

		biases = ArrayList()
		for (i in 0 until parentShape.width) {
			val localKernel = WeightData("bias_f$i", Matrix(units, 1), true)
			addWeights(localKernel)
			biases.add(localKernel)
		}

		val windowCount = (parentShape.height - kernelSize + 1) * units
		transposeFeatureBuffer = Matrix(kernelSize, 1) // width = height, height = 1, that's correct
		transposeOutputBuffer = Matrix(units, 1) // width = height, height = 1, that's correct
		outputBuffer = Matrix(parentShape.width, windowCount)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		for (x in 0 until input.width) { // per feature
			for (y in 0 until input.height - kernelSize + 1) {
				for (t in 0 until kernelSize) {
					// fill horizontally
					transposeFeatureBuffer.values[0][t] = input.values[t + y][x]
				}
				val kernel = kernels[x]
				MatrixMath.flush(transposeOutputBuffer)
				MatrixMath.multiply(transposeFeatureBuffer, kernel.matrix, transposeOutputBuffer)
				if (useBias) {
					val bias = biases[x]
					MatrixMath.add(transposeOutputBuffer, bias.matrix, transposeOutputBuffer)
				}

				for (t in 0 until units) {
					// fill vertically
					outputBuffer.values[t + y * units][x] = transposeOutputBuffer.values[0][t]
				}
			}
		}

		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
