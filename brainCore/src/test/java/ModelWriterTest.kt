import activation.Activations
import layers.Concat
import layers.Dense
import layers.InputLayer
import models.ModelBuilder
import models.ModelReader
import models.ModelWriter
import utils.printYellow
import kotlin.test.Test

class ModelWriterTest {

	@Test
	fun previewJson() {
		val input = InputLayer(3)
		val d0 = Dense(4, Activations.ReLu, name = "d0") { input }
		val d1 = Dense(4, Activations.LeReLu, name = "d1") { d0 }
		val d2 = Dense(4, Activations.ReLu, name = "d2") { d0 }

		val concat = Concat { listOf(d1, d2) }

		val builder = ModelBuilder(input, concat)
		val model = builder.build()

		val sm1 = ModelWriter.serialize(model)
		val json = ModelWriter.toJson(sm1)
		printYellow(json)
		val sm2 = ModelReader.fromJson(json)
		assert(sm1 == sm2)
	}

}