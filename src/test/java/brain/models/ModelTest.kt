package brain.models

import brain.layers.impl.*
import brain.matrix.Matrix
import brain.matrix.describe
import brain.matrix.get
import brain.matrix.iteratorWithPos
import brain.suppliers.Suppliers
import brain.utils.printGreenBr
import brain.utils.printRedBr
import brain.utils.printYellowBr
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ModelTest {

	@Test
	fun testDense() {
		val input = Input(3, 2)
		val d1 = Dense(2, kernelInit = Suppliers.const(0.5f), biasInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix.ofLambda(3, 2) { x, y, _ ->
			(x + y).toFloat()
		}
		val r1 = model.getOutput(inputData).copy()
		printRedBr(r1.describe())
		assert(r1.shape().width == 2)
		assert(r1.shape().height == 2)

		assert(r1[0, 0] == 2.5f)
		assert(r1[1, 0] == 2.5f)
		assert(r1[0, 1] == 4f)
		assert(r1[1, 1] == 4f)
	}

	@Test
	fun testDirect() {
		val input = Input(3, 2)
		val d1 = Scale(kernelInit = Suppliers.const(0.5f), biasInit = Suppliers.Ones) { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix.ofLambda(3, 2) { x, y, c ->
			(x + y).toFloat()
		}
		printRedBr(inputData.describe())

		val r1 = model.getOutput(inputData).copy()
		printGreenBr(r1.describe())
		assert(r1.shape().width == 3)
		assert(r1.shape().height == 2)

		r1.iteratorWithPos().forEach { (x, y, v) ->
			assertEquals((x + y) * 0.5f + 1f, v)
		}
	}

	@Test
	fun testFlatten() {
		val input = Input(3, 3)
		val d1 = Flatten { input }

		val builder = ModelBuilder(input, d1, debug = false)
		val model = builder.build(debug = true)

		val inputData = Matrix.ofLambda(3, 3) { x, y, _ ->
			(x + 1f) * (y + 1f)
		}
		printRedBr(inputData.describe())
		val r1 = model.getOutput(inputData).copy()
		printGreenBr(r1.describe())
		assert(r1.shape().width == 9)
		assert(r1.shape().height == 1)

		var counter = 0
		inputData.iteratorWithPos().forEach { (x, y, v) ->
			counter++
			assertEquals(r1[y * 3 + x, 0], v)
		}
		assertEquals(9, counter)
	}

	@Test
	fun testConcat() {
		val input1 = Input(3, 2)
		val input2 = Input(3, 2)
		val c1 = Concat { listOf(input1, input2) }

		val builder = ModelBuilder(mapOf("i1" to input1, "i2" to input2), c1, debug = false)
		val model = builder.build(debug = true)

		val inputData1 = Matrix.ofLambda(3, 1) { _, x, y ->
			return@ofLambda (x + 1f) * (y + 1f)
		}
		printRedBr(inputData1.describe())
		val inputData2 = Matrix.ofLambda(3, 1) { _, x, y ->
			return@ofLambda -(x + 1f) * (y + 1f)
		}
		printYellowBr(inputData2.describe())

		val r1 = model.getOutput(mapOf("i1" to inputData1, "i2" to inputData2)).copy()
		printGreenBr(r1.describe())

		assert(r1.shape().width == 6)
		assert(r1.shape().height == 1)
	}

}