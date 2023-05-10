package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath
import kotlin.math.abs
import kotlin.random.Random

class DropoutEq(
	private val rate: Float,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DropoutEqLayerImpl> {
	companion object {
		const val defaultNameType = "DropoutEq"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): DropoutEqLayerImpl {
		return DropoutEqLayerImpl(
			rate = rate,
			directShape = shape,
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.DropoutMeta {
		return LayerMetaData.DropoutMeta(rate = rate)
	}
}

class DropoutEqLayerImpl(
	private val rate: Float,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer(), LayerTrainableMode {
	override val nameType: String = DropoutEq.defaultNameType
	override lateinit var outputBuffer: Matrix

	private var trainable = false

	init {
		require(rate >= 0 && rate < 1.0)
	}

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		if (!trainable) {
			MatrixMath.transfer(input, outputBuffer)
		} else {
			var inputPositiveSum = 0f
			var outputPositiveSum = 0f
			var pDefCount = 0

			var inputNegativeSum = 0f
			var outputNegativeSum = 0f
			var nDefCount = 0

			for (y in 0 until directShape.height) {
				for (x in 0 until directShape.width) {
					val vIn = input.values[y][x]
					if (vIn > 0) {
						inputPositiveSum += vIn
						pDefCount++
					} else {
						inputNegativeSum += vIn
						nDefCount++
					}
					if (Random.nextFloat() < rate) {
						outputBuffer.values[y][x] = 0f
					} else {
						outputBuffer.values[y][x] = vIn
						if (vIn > 0) {
							outputPositiveSum += vIn
						} else {
							outputNegativeSum += vIn
						}
					}
				}
			}
			val ratioPositive = (1 + inputPositiveSum) / (1 + outputPositiveSum)
			val ratioNegative = (1 + abs(inputNegativeSum)) / (1 + abs(outputNegativeSum))
			for (y in 0 until directShape.height) {
				for (x in 0 until directShape.width) {
					val v = outputBuffer.values[y][x]
					if (v > 0) {
						outputBuffer.values[y][x] *= ratioPositive
					} else {
						outputBuffer.values[y][x] *= ratioNegative
					}
				}
			}
		}
		return outputBuffer
	}

	override fun setTrainable(trainable: Boolean) {
		this.trainable = trainable
	}

}