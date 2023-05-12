package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath
import kotlin.math.abs
import kotlin.random.Random

class GNoise(
	private val rate: Float,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<GNoiseLayerImpl> {
	companion object {
		const val defaultNameType = "GNoise"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): GNoiseLayerImpl {
		return GNoiseLayerImpl(
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

class GNoiseLayerImpl(
	private val rate: Float,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer(), LayerTrainableMode {
	override val nameType: String = GNoise.defaultNameType
	override lateinit var outputBuffer: Matrix

	private var trainable = false
	private val jRandom = java.util.Random()

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
			for (y in 0 until directShape.height) {
				for (x in 0 until directShape.width) {
					outputBuffer.values[y][x] = input.values[y][x] * (1 + jRandom.nextGaussian().toFloat() * rate)
				}
			}
		}
		return outputBuffer
	}

	override fun setTrainable(trainable: Boolean) {
		this.trainable = trainable
	}

}