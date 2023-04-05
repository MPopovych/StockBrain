package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

/**
 * Similar to a dense layer, but is transposed in weights
 * which applies to Column(t) and returns same count of columns but with *units* rows
 *
 *  output size is:
 *  column features - same as input features
 *  rows - equal to *units*
 */
class FeatureDense(
	val units: Int,
	private val activation: ActivationFunction? = null,
	private val useBias: Boolean = true,
	private val pivotAvg: Boolean = false,
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
		return LayerMetaData.FeatureDenseMeta(useBias = useBias, pivotAvg = pivotAvg)
	}
}

class FeatureDenseImpl(
	val units: Int,
	val parentShape: LayerShape,
	val useBias: Boolean,
	val pivotAvg: Boolean = false,
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
			Suppliers.fillFull(localKernel.matrix, Suppliers.RandomHE)
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
		MatrixMath.flush(transposeFeatureBuffer)
		MatrixMath.flush(transposeOutputBuffer)

		for (x in 0 until input.width) { // per feature
			MatrixMath.flush(transposeFeatureBuffer)
			for (y in 0 until input.height) {
				// fill horizontally
				transposeFeatureBuffer.values[0][y] = input.values[y][x]
			}
			if (pivotAvg) {
//				val avg = transposeFeatureBuffer.values[0].average().toFloat()
				val avg = transposeFeatureBuffer.values[0][0]
				for (y in 0 until input.height) {
					// fill horizontally
					transposeFeatureBuffer.values[0][y] -= avg
				}
			}
			val kernel = kernels[x]
			MatrixMath.multiply(transposeFeatureBuffer, kernel.matrix, transposeOutputBuffer)

			for (y in 0 until units) {
				// fill vertically
				outputBuffer.values[y][x] = transposeOutputBuffer.values[0][y]
			}
		}

		if (useBias) MatrixMath.add(outputBuffer, bias.matrix, outputBuffer)
		activation?.also {
			Activations.activate(outputBuffer, outputBuffer, it)
		}
		return outputBuffer
	}

}
