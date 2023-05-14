package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.applyFromMatrixTo
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class FRU(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<FRUImpl> {
	companion object {
		const val defaultNameType = "FRU"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): FRUImpl {
		return FRUImpl(
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
			units = units,
			useBias = useBias,
			reverse = reverse, null, null
		)
	}
}

open class FRUImpl(
	override val activation: ActivationFunction?,
	val units: Int,
	val reverse: Boolean,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = FRU.defaultNameType

	private lateinit var pGate: WeightData
	private lateinit var pBias: WeightData
	private lateinit var nGate: WeightData
	private lateinit var nBias: WeightData
	private lateinit var oBias: WeightData
	private lateinit var oGate: WeightData
	private val iBufferTransfer: Matrix = Matrix(parentShape.width, 1)
	private val pSingleBufferTransfer: Matrix = Matrix(1, 1)
	private val nSingleBufferTransfer: Matrix = Matrix(1, 1)
	private val pBufferAggregate: Matrix = Matrix(parentShape.height, 1)
	private val nBufferAggregate: Matrix = Matrix(parentShape.height, 1)
	private val oDeltaBuffer: Matrix = Matrix(parentShape.width, 1)

	override lateinit var outputBuffer: Matrix

	override fun init() {
		pGate = WeightData("pGate", Matrix(1, parentShape.width), trainable = true)
		registerWeight(pGate)
		nGate = WeightData("nGate", Matrix(1, parentShape.width), trainable = true)
		registerWeight(nGate)
		pBias = WeightData("pBias", Matrix(parentShape.height, 1), trainable = true)
		registerWeight(pBias)
		nBias = WeightData("nBias", Matrix(parentShape.height, 1), trainable = true)
		registerWeight(nBias)

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
		MatrixMath.flush(nBufferAggregate)

		for (t in (0 until input.height)) {
			MatrixMath.transferSingleRow(input, iBufferTransfer, t, 0)
			MatrixMath.multiply(iBufferTransfer, pGate.matrix, pSingleBufferTransfer)
			pBufferAggregate.values[0][t] = pSingleBufferTransfer.values[0][0]
			MatrixMath.multiply(iBufferTransfer, nGate.matrix, nSingleBufferTransfer)
			nBufferAggregate.values[0][t] = nSingleBufferTransfer.values[0][0]
		}

		Activations.ReLu.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)
		Activations.ReLu.applyFromMatrixTo(nBufferAggregate, nBufferAggregate)

		MatrixMath.add(pBufferAggregate, pBias.matrix, pBufferAggregate)
		MatrixMath.add(nBufferAggregate, nBias.matrix, nBufferAggregate)

		Activations.SoftMax.applyFromMatrixTo(pBufferAggregate, pBufferAggregate)
		Activations.SoftMax.applyFromMatrixTo(nBufferAggregate, nBufferAggregate)

		val maxIndexP = pBufferAggregate.values[0].indexOfMax()
		val maxIndexN = nBufferAggregate.values[0].indexOfMax()

		val kMax = input.values[maxIndexP]
		val kMin = input.values[maxIndexN]

		for (i in kMax.indices) {
			oDeltaBuffer.values[0][i] = kMax[i] - kMin[i]
		}

		MatrixMath.multiply(oDeltaBuffer, oGate.matrix, outputBuffer)
		MatrixMath.add(outputBuffer, oBias.matrix, outputBuffer)
		activation?.also {
			it.applyFromMatrixTo(outputBuffer, outputBuffer)
		}

		return outputBuffer
	}

	private fun FloatArray.indexOfMax(): Int {
		var pos = 0
		var maxV = this[pos]
		for (i in indices) {
			val v = this[i]
			if (v > maxV) {
				maxV = kotlin.math.max(v, maxV)
				pos = i
			}
		}
		return pos
	}

	private fun FloatArray.indexOfMin(): Int {
		var pos = 0
		var minV = this[pos]
		for (i in indices) {
			val v = this[i]
			if (v <= minV) {
				minV = kotlin.math.min(v, minV)
				pos = i
			}
		}
		return pos
	}

}
