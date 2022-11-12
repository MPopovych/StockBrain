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

}