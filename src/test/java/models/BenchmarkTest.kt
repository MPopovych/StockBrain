package models

import activation.Activations
import layers.*
import org.junit.jupiter.api.Test
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import utils.brBenchmark
import utils.printBlueBr

class BenchmarkTest {

	@Test
	fun testModelSingle() {
		val featureSize = 10
		val input = InputLayer(featureSize)
		val d10 = Dense(10) { input }
		val relu10 = Activation(Activations.ReLu) { d10 }
		val d11 = Dense(10) { relu10 }
		val relu11 = Activation(Activations.ReLu) { d11 }

		val d20 = Dense(10) { input }
		val relu20 = Activation(Activations.ReLu) { d20 }
		val d21 = Dense(10) { relu20 }
		val relu21 = Activation(Activations.ReLu) { d21 }

		val concat = Concat { listOf(relu11, relu21) }
		val dc0 = Dense(10) { concat }
		val reluC0 = Activation(Activations.ReLu) { dc0 }

		val builder = ModelBuilder(input, reluC0, debug = false)
		val model = builder.build(debug = true)
		printBlueBr(builder.summary())

		brBenchmark("iteration") {
			for (i in 0..300000) {
				val inputData = Suppliers.createMatrix(LayerShape(featureSize, 1), RandomRangeSupplier.INSTANCE)
				model.getOutput(inputData)
			}
		}
	}

}