package brain.models

import brain.activation.abs.Activations
import brain.layers.impl.Activation
import brain.layers.impl.Concat
import brain.layers.impl.Dense
import brain.layers.impl.Input
import brain.matrix.Matrix
import brain.matrix.describe
import brain.multik.MultikLoader
import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printBlueBr
import brain.utils.printGreenBr
import org.junit.jupiter.api.Test


class BenchmarkTest {

	@Test
	fun testModelSingle() {
		MultikLoader.loadSync()

		val input = Input(10)
		val d10 = Dense(10) { input }
		val relu10 = Activation(Activations.LeakyReLu(0.5f)) { d10 }
		val d11 = Dense(10) { relu10 }
		val relu11 = Activation(Activations.LeakyReLu) { d11 }

		val d20 = Dense(60) { input }
		val relu20 = Activation(Activations.ReLu) { d20 }
		val d21 = Dense(10) { relu20 }
		val relu21 = Activation(Activations.LeakyReLu(0.3f)) { d21 }

		val concat = Concat { listOf(relu11, relu21) }
		val dc0 = Dense(10) { concat }
		val reluC0 = Activation(Activations.LeakyReLu) { dc0 }

		val builder = ModelBuilder(input, reluC0)
		val model = builder.build()
		printBlueBr(builder.summary())

		brBenchmark("iteration") {
			for (i in 0..100000) {
				val inputData = Matrix.ofSupply(10, 1, Suppliers.UniformNegPos)
				model.getOutput(inputData)
			}
		}
		printGreenBr(model.getOutput(Matrix.ofSupply(10, 1, Suppliers.UniformNegPos)).describe())
	}

}