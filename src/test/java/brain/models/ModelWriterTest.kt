package brain.models

import brain.activation.Activations
import brain.assertEqualModel
import brain.layers.*
import brain.suppliers.Suppliers
import brain.utils.print
import brain.utils.printRedBr
import brain.utils.printYellowBr
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
		val json = ModelWriter.toJson(sm, pretty = true)
		printYellowBr(json)

		val model2 = ModelReader.modelInstance(json)
		printYellowBr(model2.revertToBuilder().summary())
	}

	@Test
	fun testSaveAndLoadEqual() {
		val input = InputLayer(3, 2)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0", useBias = false) { input }
		val d1 = Dense(4, activation = Activations.LeReLu, name = "d1") { d0 }
		val d2 = Direct(activation = Activations.ReLu, name = "d2") { d0 }
		val d3 = Direct(useBias = false, name = "d3") { d2 }
		val a2 = Activation(function = Activations.ReLu) { d3 }
		val concat = Concat { listOf(d1, a2) }
		val convDelta = ConvDelta { concat }
		val flatten = Flatten { convDelta }
		val model = ModelBuilder(input, flatten).build()

		val sm1 = ModelWriter.serialize(model)
		val json = ModelWriter.toJson(sm1, pretty = true)
		printYellowBr(json)
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
		val d3 = Direct(name = "d3", useBias = false) { d2 }
		val concat = Concat { listOf(a1, d3) }
		val convDelta = ConvDelta { concat }
		val flatten = Flatten { convDelta }
		val output = Dense(3, activation = Activations.Tanh) { flatten }
		val builder = ModelBuilder(input, output)
		val modelOriginal = builder.build()

		val inputData = Suppliers.createMatrix(input.getShape(), Suppliers.RandomRangeNP)
		val resultOriginal = modelOriginal.getOutput(inputData)
		resultOriginal.print()

		val sm1 = ModelWriter.serialize(modelOriginal)
		val json = ModelWriter.toJson(sm1, pretty = true)

		val modelCopy = ModelReader.modelInstance(json)
		val resultCopy = modelCopy.getOutput(inputData)
		resultCopy.printRedBr()

		assertNotEquals(modelCopy, modelOriginal) // make sure those are not the same objects, snh
		assertEqualModel(resultCopy, resultOriginal) // check array per element
	}

}