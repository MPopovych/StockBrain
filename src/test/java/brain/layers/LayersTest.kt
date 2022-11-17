package brain.layers

import brain.models.ModelBuilder
import brain.suppliers.Suppliers
import brain.utils.getShape
import brain.utils.print
import brain.utils.printRedBr
import org.junit.jupiter.api.Test

class LayersTest {

	@Test
	fun testConvDelta() {
		val input = InputLayer(3, steps = 3)
		val d1 = ConvDelta { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Suppliers.createMatrix(LayerShape(3, 3), Suppliers.RandomRangeNP)
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

		val inputData = Suppliers.createMatrix(LayerShape(3, 3), Suppliers.RandomRangeNP)
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

		val inputData1 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)
		inputData1.printRedBr()
		val inputData2 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)
		inputData2.print()
		val r1 = model.getOutput(mapOf("i1" to inputData1, "i2" to inputData2)).copy()
		r1.print()
		assert(r1.getShape().width == 6)
		assert(r1.getShape().height == 1)
	}

	@Test
	fun testTimeMask() {
		val input = InputLayer(3, steps = 3)
		val tm = TimeMask(fromEnd = 0, fromStart = 2) { input }

		val builder = ModelBuilder(input, tm, debug = false)
		val model = builder.build(debug = true)

		val inputData = Suppliers.createMatrix(LayerShape(3, 3), Suppliers.RandomRangeNP)
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

		val inputData = Suppliers.createMatrix(LayerShape(3, 8), Suppliers.RandomRangeNP)
		inputData.printRedBr()
		val r1 = model.getOutput(inputData).copy()
		r1.print()
		assert(r1.getShape().width == 3)
		assert(r1.getShape().height == 2)
	}

}