package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.nameType
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class GRU(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val updateActivation: ActivationFunction? = Activations.Sigmoid,
	val resetActivation: ActivationFunction? = Activations.Sigmoid,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<GRUImpl> {
	companion object {
		const val defaultNameType = "GRU"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): GRUImpl {
		return GRUImpl(
			activation = activation,
			updateActivation = updateActivation,
			resetActivation = resetActivation,
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
			reverse = reverse,
			updateActivation = updateActivation?.nameType(),
			resetActivation = resetActivation?.nameType())
	}
}

open class GRUImpl(
	override val activation: ActivationFunction?,
	val updateActivation: ActivationFunction?,
	val resetActivation: ActivationFunction?,
	val units: Int,
	val reverse: Boolean,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = GRU.defaultNameType

	private lateinit var zBias: WeightData
	private lateinit var rBias: WeightData
	private lateinit var hBias: WeightData

	private lateinit var zGate: WeightData
	private lateinit var rGate: WeightData
	private lateinit var hGate: WeightData

	private lateinit var zRecGate: WeightData
	private lateinit var rRecGate: WeightData
	private lateinit var hRecGate: WeightData

	private val zGateBufferM1: Matrix = Matrix(units, 1)
	private val zGateBufferM2: Matrix = Matrix(units, 1)
	private val zGateBufferA1: Matrix = Matrix(units, 1)

	private val rGateBufferM1: Matrix = Matrix(units, 1)
	private val rGateBufferM2: Matrix = Matrix(units, 1)
	private val rGateBufferA1: Matrix = Matrix(units, 1)

	private val nGateBufferM1: Matrix = Matrix(units, 1)
	private val nGateBufferM2: Matrix = Matrix(units, 1)
	private val nGateBufferM3: Matrix = Matrix(units, 1)
	private val nGateBufferA1: Matrix = Matrix(units, 1)

	private val cGateBufferS1: Matrix = Matrix(units, 1)
	private val cGateBufferM1: Matrix = Matrix(units, 1)
	private val cGateBufferM2: Matrix = Matrix(units, 1)

	lateinit var cellStateBufferCurrent: Matrix
	lateinit var cellStateBufferPrev: Matrix
	override lateinit var outputBuffer: Matrix

	override fun init() {
		zGate = WeightData("zGate", Matrix(units, parentShape.width), trainable = true)
		registerWeight(zGate)
		rGate = WeightData("rGate", Matrix(units, parentShape.width), trainable = true)
		registerWeight(rGate)
		hGate = WeightData("hGate", Matrix(units, parentShape.width), trainable = true)
		registerWeight(hGate)

		zRecGate = WeightData("zRecGate", Matrix(units, units), trainable = true)
		registerWeight(zRecGate)
		rRecGate = WeightData("rRecGate", Matrix(units, units), trainable = true)
		registerWeight(rRecGate)
		hRecGate = WeightData("hRecGate", Matrix(units, units), trainable = true)
		registerWeight(hRecGate)

		for (w in weights.values) {
			Suppliers.fillFull(w.matrix, Suppliers.RandomHE)
		}

		zBias = WeightData("zBias", Matrix(units, 1), trainable = useBias)
		registerWeight(zBias)
		rBias = WeightData("rBias", Matrix(units, 1), trainable = useBias)
		registerWeight(rBias)
		hBias = WeightData("hBias", Matrix(units, 1), trainable = useBias)
		registerWeight(hBias)

		cellStateBufferCurrent = Matrix(parentShape.width, 1)
		cellStateBufferPrev = Matrix(units, 1)
		outputBuffer = Matrix(units, 1)
	}

	private var cachedIterator: IntArray? = null // micro-optimisation for iterative operations
	override fun call(input: Matrix): Matrix {
		flushBuffer()
		MatrixMath.flush(cellStateBufferPrev) // h_prev

		val rowIterator = cachedIterator ?: (0 until input.height).let {
			var list = (0 until input.height).toList()
			if (reverse) {
				list = list.asReversed()
			}
			val array = list.toIntArray()
			cachedIterator = array
			return@let array
		}
		for (t in rowIterator) {
			MatrixMath.transferSingleRow(input, cellStateBufferCurrent, t, 0)

			transferCurrentToOutput()
		}
		MatrixMath.transfer(cellStateBufferPrev, outputBuffer)
		return outputBuffer
	}

	protected fun transferCurrentToOutput() {
		// z
		MatrixMath.multiply(cellStateBufferCurrent, zGate.matrix, zGateBufferM1)
		MatrixMath.multiply(cellStateBufferPrev, zRecGate.matrix, zGateBufferM2)
		MatrixMath.add(zGateBufferM1, zGateBufferM2, zGateBufferA1)
		if (useBias) MatrixMath.add(zGateBufferA1, zBias.matrix, zGateBufferA1)
		resetActivation?.also {
			Activations.activate(zGateBufferA1, zGateBufferA1, it)
		}

		// r
		MatrixMath.multiply(cellStateBufferCurrent, rGate.matrix, rGateBufferM1)
		MatrixMath.multiply(cellStateBufferPrev, rRecGate.matrix, rGateBufferM2)
		MatrixMath.add(rGateBufferM1, rGateBufferM2, rGateBufferA1)
		if (useBias) MatrixMath.add(rGateBufferA1, rBias.matrix, rGateBufferA1)
		updateActivation?.also {
			Activations.activate(rGateBufferA1, rGateBufferA1, it)
		}

		// n
		MatrixMath.multiply(cellStateBufferCurrent, hGate.matrix, nGateBufferM1)
		MatrixMath.multiply(cellStateBufferPrev, hRecGate.matrix, nGateBufferM2)
		MatrixMath.hadamard(rGateBufferA1, nGateBufferM1, nGateBufferM3)
		MatrixMath.add(nGateBufferM1, nGateBufferM3, nGateBufferA1)
		if (useBias) MatrixMath.add(nGateBufferA1, hBias.matrix, nGateBufferA1)
		activation?.let {
			Activations.activate(nGateBufferA1, nGateBufferA1, it)
		}

		// h
		MatrixMath.constantSub(1f, zGateBufferA1, cGateBufferS1) // (1 - z)
		MatrixMath.hadamard(cGateBufferS1, cellStateBufferPrev, cGateBufferM1) // (1 - z) * h_t-1
		MatrixMath.hadamard(zGateBufferA1, nGateBufferA1, cGateBufferM2) // z * n
		// save to previous
		MatrixMath.add(cGateBufferM1, cGateBufferM2, cellStateBufferPrev) // (1 - z) * h_t-1 +  z * n
	}

}
