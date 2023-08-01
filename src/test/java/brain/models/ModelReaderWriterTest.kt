package brain.models

import brain.activation.abs.Activations
import brain.assertEqual
import brain.layers.impl.Concat
import brain.layers.impl.Dense
import brain.layers.impl.Input
import brain.layers.impl.Scale
import brain.matrix.Matrix
import brain.matrix.describe
import brain.suppliers.Suppliers
import brain.utils.printGreenBr
import brain.utils.printRedBr
import brain.utils.printYellowBr
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelReaderWriterTest {

	@Test
	fun testSerializeAndCompare() {
		val input = Input(3)
		val d0 = Dense(4, activation = Activations.ReLu, useBias = false) { input }
		val d1 = Scale(activation = Activations.LeakyReLu(0.5f)) { input }
		val concat = Concat { listOf(d0, d1) }
		val model = ModelBuilder(input = input, output = concat).build()

		val sm = ModelReaderWriter.serialize(model)
		val json = ModelReaderWriter.toJson(sm)
		printYellowBr(json)

		val model2 = ModelReaderWriter.modelFromJson(json)
		printYellowBr(model2.summary())
		assert(model !== model2)

		val inputData = Matrix.ofSupply(3, 1, Suppliers.Ones)
		printRedBr(inputData.describe())

		val r1 = model.getOutput(inputData)
		val r2 = model2.getOutput(inputData)
		printGreenBr(r1.describe())
		assertEquals(r1.width, 4 + 3)

		assert(r1 !== r2)
		assertEqual(r1, r2)

	}

}