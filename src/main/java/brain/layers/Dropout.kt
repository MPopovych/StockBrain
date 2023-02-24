package brain.layers

import brain.matrix.Matrix
import brain.matrix.MatrixMath
import kotlin.math.roundToInt

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
	override val nameType: String = Direct.defaultNameType
	override lateinit var outputBuffer: Matrix

	private var trainable = false
	private val countToTake = ((directShape.width * directShape.height).toFloat() * (1.0 - rate)).roundToInt()
	private val dropoutMConst = 1 / (1 - rate)

	init {
		require(rate > 0 && rate < 1.0)
		require(countToTake > 0)
		require(countToTake < directShape.height * directShape.width)
	}

	private val indexMatrix = (0 until directShape.width)
		.map { x -> (0 until directShape.height).map { y-> Pair(x, y) } }.flatten()

	override fun init() {
		outputBuffer = Matrix(directShape.width, directShape.height)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		if (!trainable) {
			MatrixMath.transfer(input, outputBuffer)
		} else {
			MatrixMath.transfer(input, outputBuffer)
			val shuffled = indexMatrix.shuffled()
			shuffled.take(countToTake).forEach { pair ->
				outputBuffer.values[pair.first][pair.second] = 0f
			}
			shuffled.drop(countToTake).forEach { pair ->
				outputBuffer.values[pair.first][pair.second] = outputBuffer.values[pair.first][pair.second] * dropoutMConst
			}
		}
		return outputBuffer
	}

	override fun setTrainable(trainable: Boolean) {
		this.trainable = trainable
	}

}