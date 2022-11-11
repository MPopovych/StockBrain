package models

import activation.Activations
import assertEqualModel
import layers.*
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import utils.print
import utils.printRed
import utils.printYellow
import kotlin.test.Test
import kotlin.test.assertNotEquals

class ModelWriterTest {

	@Test
	fun testForReadMe() {
		val input = InputLayer(3)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0", useBias = false) { input }
		val d1 = Direct(activation = Activations.ReLu, name = "d2") { input }
		val model = ModelBuilder(input, Concat(name = "output") { listOf(d0, d1) }).build()

		val sm = ModelWriter.serialize(model)
		val json = ModelWriter.toJson(sm)
		printYellow(json)

		val model2 = ModelReader.modelInstance(json)
		printYellow(model2.revertToBuilder().summary())
	}

	@Test
	fun testSaveAndLoadEqual() {
		val input = InputLayer(3)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0", useBias = false) { input }
		val d1 = Dense(4, activation = Activations.LeReLu, name = "d1") { d0 }
		val d2 = Direct(activation = Activations.ReLu, name = "d2") { d0 }
		val concat = Concat { listOf(d1, d2) }
		val model = ModelBuilder(input, concat).build()

		val sm1 = ModelWriter.serialize(model)
		val json = ModelWriter.toJson(sm1)
		printYellow(json)
		val sm2 = ModelReader.fromJson(json)
		assert(sm1 == sm2)
	}

	@Test
	fun testWriteModelReadModelCheckResult() {
		val input = InputLayer(3, 2)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0") { input }
		val d1 = Dense(4, name = "d1", useBias = false) { d0 }
		val a1 = Activation(Activations.LeReLu) { d1 }
		val d2 = Direct(activation = Activations.LeReLu, name = "d2") { d0 }
		val d3 = Direct(name = "d3") { d2 }
		val concat = Concat { listOf(a1, d3) }
		val convDelta = ConvDelta { concat }
		val builder = ModelBuilder(input, convDelta)
		val modelOriginal = builder.build()

		val inputData = Suppliers.createMatrix(input.getShape(), RandomRangeSupplier.INSTANCE)
		val resultOriginal = modelOriginal.getOutput(inputData)
		resultOriginal.print()

		val sm1 = ModelWriter.serialize(modelOriginal)
		val json = ModelWriter.toJson(sm1)

		val modelCopy = ModelReader.modelInstance(json)
		val resultCopy = modelCopy.getOutput(inputData)
		resultCopy.printRed()

		assertNotEquals(modelCopy, modelOriginal) // make sure those are not the same objects, snh
		assertEqualModel(resultCopy, resultOriginal) // check array per element
	}

}