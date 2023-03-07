package brain.layers

import brain.matrix.Matrix
import brain.models.ModelBuilder
import brain.suppliers.Suppliers
import brain.utils.getShape
import brain.utils.print
import brain.utils.printGreenBr
import brain.utils.printRedBr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class LayersTest {

	@Test
	fun testDense() {
		val input = InputLayer(3, steps = 2)
		val d1 = Dense(2, kernelInit = Suppliers.const(0.5f), biasInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, 2) { c, x, y ->
			return@Matrix (x + y).toFloat()
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 2)
		assert(r1.getShape().height == 2)

		assertEquals(2.5f, r1.values[0][0])
		assertEquals(2.5f, r1.values[0][1])
		assertEquals(4f, r1.values[1][0])
		assertEquals(4f, r1.values[1][1])
	}

	@Test
	fun testDenseMax() {
		val input = InputLayer(3, steps = 2)
		val d1 = DenseMax(2, kernelInit = Suppliers.RandomHE, biasInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, 2) { c, x, y ->
			return@Matrix (x + y).toFloat()
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 2)
		assert(r1.getShape().height == 2)
	}

	@Test
	fun testDirect() {
		val input = InputLayer(3, steps = 2)
		val d1 = Direct(kernelInit = Suppliers.const(0.5f), biasInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, 2) { c, x, y ->
			return@Matrix (x + y).toFloat()
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 2)

		for ((x, array) in r1.values.withIndex()) {
			for ((y, v) in array.withIndex()) {
				assertEquals((x + y) * 0.5f + 1f, v)
			}
		}
	}

	@Test
	fun testDropout() {
		val input = InputLayer(3, steps = 2)
		val d1 = Dropout(rate = 0.1f) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)
		model.setTrainable(true)

		val inputData = Matrix(3, 2) { c, x, y ->
			return@Matrix (x + y).toFloat() + 2f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 2)

		var zeroed = 0
		var scaled = 0
		for (array in r1.values) {
			for (v in array) {
				if (v == 0f) {
					zeroed++
				} else {
					scaled++
				}
			}
		}
		val totalNodes = d1.getShape().height * d1.getShape().width
		printGreenBr("different: ${scaled}, zeroed: $zeroed")
		assertEquals(totalNodes, zeroed + scaled)
		assertEquals(1, zeroed)
	}

	@Test
	fun testConvDelta() {
		val input = InputLayer(3, steps = 3)
		val d1 = ConvDelta { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, 3) { c, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 2)
	}

	@Test
	fun testFlatten() {
		val input = InputLayer(3, steps = 3)
		val d1 = Flatten { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, 3) { c, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 9)
		assert(r1.getShape().height == 1)
	}

	@Test
	fun testConcat() {
		val input1 = InputLayer(3, steps = 1)
		val input2 = InputLayer(3, steps = 1)
		val c1 = Concat { listOf(input1, input2) }

		val builder = ModelBuilder(mapOf("i1" to input1, "i2" to input2), c1, debug = false)
		val model = builder.build(debug = true)

		val inputData1 = Matrix(3, 1) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		inputData1.printRedBr()
		val inputData2 = Matrix(3, 1) { _, x, y ->
			return@Matrix - (x + 1f) * (y + 1f)
		}
		inputData2.print()
		val r1 = model.getOutput(mapOf("i1" to inputData1, "i2" to inputData2)).copy()
		r1.print()
		assert(r1.getShape().width == 6)
		assert(r1.getShape().height == 1)
	}

	@Test
	fun testTimeMask() {
		val input = InputLayer(3, steps = 3)
		val tm = TimeMask(fromStart = 2, fromEnd = 0) { input }

		val builder = ModelBuilder(input, tm, debug = false)
		val model = builder.build(debug = true)

		val inputData = Suppliers.createMatrix(LayerShape(input.features, input.steps), Suppliers.RandomRangeNP)
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 1)
	}

	@Test
	fun testFeatureDense() {
		val input = InputLayer(3, steps = 8)
		val fd1 = FeatureDense(2) { input }

		val builder = ModelBuilder(input, fd1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, input.steps) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		for (i in 0 until input.steps) {
			inputData.values[i][0] = 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 2)
		for (i in 0 until 2) {
			assertEquals(0f, r1.values[i][0])
		}
	}

	@Test
	fun testFeatureConv() {
		val input = InputLayer(3, steps = 8)
		val fd1 = FeatureConv(1, kernelSize = 4, reverse = false, kernelInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, fd1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, input.steps) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		for (i in 0 until input.steps) {
			inputData.values[i][0] = 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 8 - 4 + 1) // 5
		for (i in 0 until 2) {
			assertEquals(0f, r1.values[i][0])
		}
		assertEquals(20f, r1.values[0][1])
		assertEquals(52f, r1.values[4][1])
	}

	@Test
	fun testFeatureConv2() {
		val input = InputLayer(3, steps = 8)
		val fd1 = FeatureConv(2, kernelSize = 4, reverse = false, kernelInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, fd1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, input.steps) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		for (i in 0 until input.steps) {
			inputData.values[i][0] = 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == (8 - 4 + 1) * 2) // 10
		for (i in 0 until 2) {
			assertEquals(0f, r1.values[i][0])
		}
		assertEquals(20f, r1.values[0][1])
		assertEquals(20f, r1.values[1][1])
		assertEquals(52f, r1.values[8][1])
		assertEquals(52f, r1.values[9][1])
	}

	@Test
	fun testFeatureConv2Step() {
		val input = InputLayer(3, steps = 9)
		val fd1 = FeatureConv(1, kernelSize = 3, step = 3, reverse = false, kernelInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, fd1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, input.steps) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		for (i in 0 until input.steps) {
			inputData.values[i][0] = 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == input.steps / fd1.kernelSize)
	}

	@Test
	fun testFeatureConv2Reverse() {
		val input = InputLayer(3, steps = 10)
		val fd1 = FeatureConv(1, kernelSize = 3, step = 3, reverse = true, kernelInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, fd1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(3, input.steps) { _, x, y ->
			return@Matrix (x + 1f) * (y + 1f)
		}
		for (i in 0 until input.steps) {
			inputData.values[i][0] = 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 3)
		for (i in 0 until 3) {
			assertEquals(0f, r1.values[i][0])
		}
		assertEquals(18f, r1.values[2][1])
		assertEquals(36f, r1.values[1][1])
		assertEquals(54f, r1.values[0][1])
	}

	@Test
	fun testPivot() {
		val input = InputLayer(3, steps = 1)
		val pivot = PivotNorm (biasAInit = Suppliers.Ones, kernelInit = Suppliers.const(2f), biasBInit = Suppliers.const(-1f)){ input }

		val builder = ModelBuilder(input, pivot, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix(input.features, input.steps) { _, x, y ->
			return@Matrix 0f
		}
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == input.features)
		assert(r1.getShape().height == input.steps)
		for (y in 0 until input.steps) {
			for (x in 0 until input.features) {
				assertEquals(1f, r1.values[y][x])
			}
		}
	}

}