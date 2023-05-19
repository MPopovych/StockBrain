package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class CRU(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val useBias: Boolean = true, // not implemented
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<CRUImpl> {
	companion object {
		const val defaultNameType = "CRU"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = parentLayer.getShape().width)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): CRUImpl {
		return CRUImpl(
			activation = activation,
			units = units,
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
			units = units,
			useBias = useBias,
			reverse = false,
			updateActivation = null,
			resetActivation = null)
	}
}

open class CRUImpl(
	override val activation: ActivationFunction?,
	val units: Int,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = CRU.defaultNameType

	private lateinit var pGate: WeightData
	private lateinit var uGate: WeightData
	private lateinit var dGate: WeightData
	private lateinit var uBias: WeightData
	private lateinit var pBias: WeightData

	private val iBufferTransfer: Matrix = Matrix(parentShape.width, 1)
	private val pUnitBufferTransfer: Matrix = Matrix(units, 1)
	private val pUnitBufferCache: Matrix = Matrix(units, 1)

	private val dUnitBufferTransfer: Matrix = Matrix(units, 1)

	private val pSingleBufferTransfer: Matrix = Matrix(1, 1)
	private val pBufferAggregate: Matrix = Matrix(parentShape.height, 1)

	override var outputBuffer: Matrix = Matrix(parentShape.width, 1)

	override fun init() {
		uGate = WeightData("uGate", Matrix(units, parentShape.width), trainable = true)
		registerWeight(uGate)
		uBias = WeightData("uBias", Matrix(units, 1), trainable = true)
		registerWeight(uBias)

		pGate = WeightData("pGate", Matrix(1, units), trainable = true)
		registerWeight(pGate)
		pBias = WeightData("pBias", Matrix(parentShape.height, 1), trainable = true)
		registerWeight(pBias)

		dGate = WeightData("dGate", Matrix(units, units), trainable = true)
		registerWeight(dGate)

		for (w in weights.values) {
			Suppliers.fillFull(w.matrix, Suppliers.RandomHE)
		}
	}

	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(pBufferAggregate)
		MatrixMath.flush(pUnitBufferCache)

		for (t in (0 until input.height)) {
			MatrixMath.transfer(pUnitBufferTransfer, pUnitBufferCache)

			MatrixMath.transferSingleRow(input, iBufferTransfer, t, 0)

			MatrixMath.multiply(iBufferTransfer, uGate.matrix, pUnitBufferTransfer)
			MatrixMath.add(pUnitBufferTransfer, uBias.matrix, pUnitBufferTransfer)

			activation?.also {
				it.applyFromMatrixTo(pUnitBufferTransfer, pUnitBufferTransfer)
			}

			MatrixMath.multiply(pUnitBufferCache, dGate.matrix, dUnitBufferTransfer)
			Activations.FastTanh.applyFromMatrixTo(dUnitBufferTransfer, dUnitBufferTransfer)
			MatrixMath.add(pUnitBufferTransfer, dUnitBufferTransfer, pUnitBufferTransfer)

			MatrixMath.multiply(pUnitBufferTransfer, pGate.matrix, pSingleBufferTransfer)
			pBufferAggregate.values[0][t] = pSingleBufferTransfer.values[0][0]
		}

		Activations.SoftMax.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)
		MatrixMath.add(pBufferAggregate, pBias.matrix, pBufferAggregate)
		Activations.ReLu.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)

		for (height in pBufferAggregate.values[0].indices) {
			for (feature in 0 until input.width) {
				val ratio = pBufferAggregate.values[0][height]
				outputBuffer.values[0][feature] += input.values[height][feature] * ratio
			}
		}

		return outputBuffer
	}
}
