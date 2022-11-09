import activation.Activations
import layers.*
import models.ModelBuilder
import models.ModelReader
import models.ModelWriter
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import utils.print
import utils.printRed
import utils.printYellow
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ModelWriterTest {

	@Test
	fun previewJson() {
		val input = InputLayer(3)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0") { input }
		val d1 = Dense(4, activation = Activations.LeReLu, name = "d1") { d0 }
		val d2 = Direct(activation = Activations.ReLu, name = "d2") { d0 }
		val concat = Concat { listOf(d1, d2) }
		val builder = ModelBuilder(input, concat)
		val model = builder.build()


		val sm1 = ModelWriter.serialize(model)
		val json = ModelWriter.toJson(sm1)
		printYellow(json)
		val sm2 = ModelReader.fromJson(json)
		assert(sm1 == sm2)

		assertNull(sm2.layers[0].getMetaData())
		assertNotNull(sm2.layers[1].getMetaData())
		assertNotNull(sm2.layers[2].getMetaData())
		assertNotNull(sm2.layers[3].getMetaData())
		assertNull(sm2.layers[4].getMetaData())
	}

	@Test
	fun testWriteModelReadModelCheckResult() {
		val input = InputLayer(3)
		val d0 = Dense(4, activation = Activations.ReLu, name = "d0") { input }
		val d1 = Dense(4, name = "d1") { d0 }
		val a1 = Activation(Activations.LeReLu) { d1 }
		val d2 = Direct(activation = Activations.LeReLu, name = "d2") { d0 }
		val d3 = Direct(name = "d3") { d2 }
		val concat = Concat { listOf(a1, d3) }
		val builder = ModelBuilder(input, concat)
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
		assertEqual(resultCopy, resultOriginal) // check array per element
	}

}