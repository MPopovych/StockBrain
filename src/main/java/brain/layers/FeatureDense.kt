package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath

class FeatureDense(
	val units: Int,
	private val activation: ActivationFunction? = null,
	private val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FeatureDenseImpl> {
	companion object {
		const val defaultNameType = "FeatureDense"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = parentLayer.getShape().copy(height = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): FeatureDenseImpl {
		return FeatureDenseImpl(
			units = units,
			parentShape = parentLayer.getShape(),
			useBias = useBias,
			activation = activation,
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.FeatureDenseMeta {
		return LayerMetaData.FeatureDenseMeta(useBias = useBias)
	}
}

class FeatureDenseImpl(
	val units: Int,
	val parentShape: LayerShape,
	val useBias: Boolean,
	override val activation: ActivationFunction? = null,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FeatureDense.defaultNameType
	override lateinit var outputBuffer: Matrix
	private lateinit var transposeFeatureBuffer: Matrix
	private lateinit var transposeOutputBuffer: Matrix

	private lateinit var kernels: ArrayList<WeightData>
	private lateinit var bias: WeightData

	override fun init() {
		kernels = ArrayList()
		for (i in 0 until parentShape.width) {
			val localKernel = WeightData("weight_f$i", Matrix(units, parentShape.height), true)
			addWeights(localKernel)
			kernels.add(localKernel)
		}

		bias = WeightData("bias", Matrix(parentShape.width, units), trainable = useBias)
		addWeights(bias)

		transposeFeatureBuffer = Matrix(parentShape.height, 1) // width = height, height = 1, that's correct
		transposeOutputBuffer = Matrix(units, 1) // width = height, height = 1, that's correct
		outputBuffer = Matrix(parentShape.width, units)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()

		for (i in 0 until input.width) {
			MatrixMath.transferArrayToMatrixRoot(input.values[i], transposeFeatureBuffer) // performs additional checks
			val kernel = kernels[i]
			MatrixMath.flush(transposeOutputBuffer)
			MatrixMath.multiply(transposeFeatureBuffer, kernel.matrix, transposeOutputBuffer)
			MatrixMath.transferMatrixRootToArray(transposeOutputBuffer, outputBuffer.values[i])
		}

		if (useBias) MatrixMath.add(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
