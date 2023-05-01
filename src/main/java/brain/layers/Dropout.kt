package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath
import kotlin.random.Random

class Dropout(
	private val rate: Float,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<DropoutLayerImpl> {
	companion object {
		const val defaultNameType = "Dropout"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()
	private val shape = parentLayer.getShape()

	override fun create(): DropoutLayerImpl {
		return DropoutLayerImpl(
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

class DropoutLayerImpl(
	private val rate: Float,
	private val directShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer(), LayerTrainableMode {
	override val nameType: String = Dropout.defaultNameType
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
			var countToMutate = 0
			val total = directShape.height * directShape.width
			val dropMap = (0 until directShape.height * directShape.width).map {
				if (Random.nextFloat() < rate) {
					countToMutate++
					return@map true
				} else {
					return@map false
				}
			}
			val dropVar = 1f + countToMutate.toFloat() / total
			for (y in 0 until directShape.height) {
				for (x in 0 until directShape.width) {
					if (dropMap[y * directShape.width + x]) {
						outputBuffer.values[y][x] = 0f
					} else {
						outputBuffer.values[y][x] = input.values[y][x] * dropVar
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