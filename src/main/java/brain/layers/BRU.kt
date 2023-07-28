package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.activation.nameType
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class BRU(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val updateActivation: ActivationFunction? = Activations.Sigmoid, // not implemented
	val resetActivation: ActivationFunction? = Activations.Sigmoid, // not implemented
	val reverse: Boolean = false, // not implemented
	val useBias: Boolean = true, // not implemented
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<BRUImpl> {
	companion object {
		const val defaultNameType = "BRU"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): BRUImpl {
		return BRUImpl(
			activation = activation,
			units = units,
			reverse = reverse,
			useBias = useBias,
			parentShape = parentLayer.getShape(),
			name = name,
		)
			.also {
				it.init()
			}
	}

	override fun getShape(): LayerShape {
		return shape
	}

	override fun getSerializedBuilderData(): LayerMetaData.GRUMeta {
		return LayerMetaData.GRUMeta(
			useBias = useBias,
			units = units,
			reverse = reverse,
			updateActivation = updateActivation?.nameType(),
			resetActivation = resetActivation?.nameType()
		)
	}
}

open class BRUImpl(
	override val activation: ActivationFunction?,
	val units: Int,
	val reverse: Boolean,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = BRU.defaultNameType

	private lateinit var pGate: WeightData
	private lateinit var pBias: WeightData
	private lateinit var nGate: WeightData
	private lateinit var nBias: WeightData
	private lateinit var oBias: WeightData
	private lateinit var oGate: WeightData
	private val iBufferTransfer: Matrix = Matrix(parentShape.width, 1)
	private val pSingleBufferTransfer: Matrix = Matrix(1, 1)
	private val pBufferAggregate: Matrix = Matrix(parentShape.height, 1)
	private val oWeightedBuffer: Matrix = Matrix(parentShape.width, 1)

	override lateinit var outputBuffer: Matrix

	override fun init() {
		pGate = WeightData("pGate", Matrix(1, parentShape.width), trainable = true)
		registerWeight(pGate)
		pBias = WeightData("pBias", Matrix(parentShape.height, 1), trainable = true)
		registerWeight(pBias)

		oGate = WeightData("oGate", Matrix(units, parentShape.width), trainable = true)
		registerWeight(oGate)
		oBias = WeightData("oBias", Matrix(units, 1), trainable = true)
		registerWeight(oBias)

		for (w in weights.values) {
			Suppliers.fillFull(w.matrix, Suppliers.RandomHE)
		}

		outputBuffer = Matrix(units, 1)
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(pBufferAggregate)
		MatrixMath.flush(oWeightedBuffer)

		for (t in (0 until input.height)) {
			MatrixMath.transferSingleRow(input, iBufferTransfer, t, 0)
			MatrixMath.multiply(iBufferTransfer, pGate.matrix, pSingleBufferTransfer)
			pBufferAggregate.values[0][t] = pSingleBufferTransfer.values[0][0]
		}

		Activations.SoftMax.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)
		MatrixMath.add(pBufferAggregate, pBias.matrix, pBufferAggregate)
		Activations.ReLu.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)

		for (height in pBufferAggregate.values[0].indices) {
			for (feature in 0 until input.width) {
				val ratio = pBufferAggregate.values[0][height]
				oWeightedBuffer.values[0][feature] += input.values[height][feature] * ratio
			}
		}

		MatrixMath.multiply(oWeightedBuffer, oGate.matrix, outputBuffer)
		MatrixMath.add(outputBuffer, oBias.matrix, outputBuffer)
		activation?.also {
			it.applyFromMatrixTo(outputBuffer, outputBuffer)
		}

		return outputBuffer
	}
}
