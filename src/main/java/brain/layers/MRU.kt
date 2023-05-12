package brain.layers

import brain.activation.ActivationFunction
import brain.activation.Activations
import brain.activation.nameType
import brain.matrix.Matrix
import brain.matrix.MatrixMath
import brain.suppliers.Suppliers

class MRU(
	val units: Int,
	val activation: ActivationFunction? = Activations.FastTanh,
	val updateActivation: ActivationFunction? = Activations.Sigmoid,
	val resetActivation: ActivationFunction? = Activations.Sigmoid,
	val reverse: Boolean = false,
	val useBias: Boolean = true,
	override var name: String = Layer.DEFAULT_NAME,
	parentLayerBlock: (() -> LayerBuilder<*>),
) : LayerBuilder.SingleInput<MRUImpl> {
	companion object {
		const val defaultNameType = "MRU"
	}

	override val nameType: String = defaultNameType
	override val parentLayer: LayerBuilder<*> = parentLayerBlock()

	private val shape = LayerShape(height = 1, width = units)

	init {
		if (shape.height < 1) throw IllegalStateException("height cant be zero or less")
	}

	override fun create(): MRUImpl {
		return MRUImpl(
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
		return LayerMetaData.GRUMeta(useBias = useBias,
			reverse = reverse,
			updateActivation = updateActivation?.nameType(),
			resetActivation = resetActivation?.nameType())
	}
}

open class MRUImpl(
	override val activation: ActivationFunction?,
	val updateActivation: ActivationFunction?,
	val resetActivation: ActivationFunction?,
	val units: Int,
	val reverse: Boolean,
	val useBias: Boolean,
	val parentShape: LayerShape,
	override var name: String,
) : Layer.SingleInputLayer() {
	override val nameType: String = MRU.defaultNameType

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
	private val zRecGateBufferM2: Matrix = Matrix(units, 1)
	private val zAddGateBufferA1: Matrix = Matrix(units, 1)

	private val rGateBufferM1: Matrix = Matrix(units, 1)
	private val rRecGateBufferM2: Matrix = Matrix(units, 1)
	private val rAddGateBufferA1: Matrix = Matrix(units, 1)

	private val nGateBufferM1: Matrix = Matrix(units, 1)
	private val nRecGateBufferM2: Matrix = Matrix(units, 1)
	private val nGateBufferHadamard3: Matrix = Matrix(units, 1)
	private val nAddGateBufferA1: Matrix = Matrix(units, 1)

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
		MatrixMath.multiply(cellStateBufferPrev, zRecGate.matrix, zRecGateBufferM2)
		MatrixMath.add(zGateBufferM1, zRecGateBufferM2, zAddGateBufferA1)
		if (useBias) MatrixMath.add(zAddGateBufferA1, zBias.matrix, zAddGateBufferA1)
		resetActivation?.also {
			Activations.activate(zAddGateBufferA1, zAddGateBufferA1, it)
		}

		// r
		MatrixMath.multiply(cellStateBufferCurrent, rGate.matrix, rGateBufferM1)
		MatrixMath.multiply(cellStateBufferPrev, rRecGate.matrix, rRecGateBufferM2)
		MatrixMath.add(rGateBufferM1, rRecGateBufferM2, rAddGateBufferA1)
		if (useBias) MatrixMath.add(rAddGateBufferA1, rBias.matrix, rAddGateBufferA1)
		updateActivation?.also {
			Activations.activate(rAddGateBufferA1, rAddGateBufferA1, it)
		}

		// n
		MatrixMath.multiply(cellStateBufferCurrent, hGate.matrix, nGateBufferM1)
		MatrixMath.multiply(cellStateBufferPrev, hRecGate.matrix, nRecGateBufferM2)
		MatrixMath.hadamard(rAddGateBufferA1, nGateBufferM1, nGateBufferHadamard3)
		MatrixMath.add(nGateBufferM1, nGateBufferHadamard3, nAddGateBufferA1)
		if (useBias) MatrixMath.add(nAddGateBufferA1, hBias.matrix, nAddGateBufferA1)
		activation?.let {
			Activations.activate(nAddGateBufferA1, nAddGateBufferA1, it)
		}

		// h
		MatrixMath.constantSub(1f, zAddGateBufferA1, cGateBufferS1) // (1 - z)
		MatrixMath.hadamard(cGateBufferS1, cellStateBufferPrev, cGateBufferM1) // (1 - z) * h_t-1
		MatrixMath.hadamard(zAddGateBufferA1, nAddGateBufferA1, cGateBufferM2) // z * n
		// save to previous
		MatrixMath.add(cGateBufferM1, cGateBufferM2, cellStateBufferPrev) // (1 - z) * h_t-1 +  z * n
	}

}
