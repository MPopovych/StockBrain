package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller
import brain.suppliers.ValueSupplier

/**
 * Output width = input width
 * for zero step:
 *  Output height = (input height - kernelSize + 1) * units
 * for non-zero step:
 *  Output height = ((input height  - kernelSize) / step + 1)
 */

class FeatureConv(
	val units: Int,
	val kernelSize: Int,
	val step: Int = 1,
	val reverse: Boolean = false,
	private val activation: ActivationFunction? = null,
	private val useBias: Boolean = true,
	private val kernelInit: ValueFiller = Suppliers.RandomRangeNP,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FeatureConvImpl> {
	companion object {
		const val defaultNameType = "FeatureConv"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val slidingCount = (parentLayer.getShape().height - kernelSize) / step + 1
	private val shape = parentLayer.getShape().copy(height = slidingCount * units)

	init {
		require(slidingCount <= parentLayer.getShape().height)
		require(shape.height >= 1)
		require(step >= 1)
	}

	override fun create(): FeatureConvImpl {
		return FeatureConvImpl(
			units = units,
			kernelSize = kernelSize,
			step = step,
			parentShape = parentLayer.getShape(),
			useBias = useBias,
			reverse = reverse,
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
		return LayerMetaData.FeatureConvMeta(
			useBias = useBias,
			units = units,
			kernels = kernelSize,
			step = step,
			reverse = reverse
		)
	}
}

class FeatureConvImpl(
	val units: Int,
	val kernelSize: Int,
	val step: Int,
	val parentShape: LayerShape,
	val useBias: Boolean,
	val reverse: Boolean,
	override val activation: ActivationFunction? = null,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FeatureConv.defaultNameType
	override lateinit var outputBuffer: Matrix
	private lateinit var transposeFeatureBuffer: Matrix
	private lateinit var transposeOutputBuffer: Matrix

	internal lateinit var kernels: ArrayList<WeightData>
	private lateinit var biases: ArrayList<WeightData>

	private val windowCount = ((parentShape.height - kernelSize) / step + 1)

	init {
		require(windowCount <= parentShape.height)
	}

	override fun init() {
		kernels = ArrayList()
		for (i in 0 until parentShape.width) {
			val localKernel = WeightData("weight_f$i", Matrix(units, kernelSize), true)
			addWeights(localKernel)
			kernels.add(localKernel)
		}

		biases = ArrayList()
		for (i in 0 until parentShape.width) {
			val localKernel = WeightData("bias_f$i", Matrix(units, 1), useBias)
			addWeights(localKernel)
			biases.add(localKernel)
		}

		transposeFeatureBuffer = Matrix(kernelSize, 1) // width = height, height = 1, that's correct
		transposeOutputBuffer = Matrix(units, 1) // width = height, height = 1, that's correct
		outputBuffer = Matrix(parentShape.width, windowCount * units)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(transposeFeatureBuffer)

		for (x in 0 until input.width) { // per feature
			for (y in 0 until windowCount) {
				for (t in 0 until kernelSize) {
					// fill horizontally
					val pos = (t + y * step)
					if (reverse) {
						transposeFeatureBuffer.values[0][t] = input.values[input.height - 1 - pos][x]
					} else {
						transposeFeatureBuffer.values[0][t] = input.values[pos][x]
					}
				}
				val kernel = kernels[x]
//				MatrixMath.flush(transposeOutputBuffer) // no need
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
