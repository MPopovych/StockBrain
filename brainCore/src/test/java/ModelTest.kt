import activation.Activations
import layers.*
import models.ModelBuilder
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import utils.print
import utils.printBlue
import utils.printGreen
import utils.printRed
import kotlin.test.Test

class ModelTest {

	@Test
	fun testModel() {
		val input = InputLayer(3)
		val d1 = Dense(4) { input }
		val relu = Activation(Activations.ReLu) { d1 }

		val builder = ModelBuilder(input, relu, debug = false)
		val model = builder.build(debug = true)

		val inputData = Suppliers.createMatrix(LayerShape(3, 1), RandomRangeSupplier.INSTANCE)
		model.getOutput(inputData).print()
	}

	@Test
	fun testBranchedModel() {
		val input = InputLayer(3)

		val d0 = Dense(4) { input }
		val d1 = Dense(4) { d0 }
		val relu1 = Activation(Activations.ReLu) { d1 }
		val d2 = Dense(4) { d0 }
		val relu2 = Activation(Activations.ReLu) { d2 }

		val concat = Concat { listOf(relu1, relu2) }

		val builder = ModelBuilder(input, concat, debug = false)
		builder.build(debug = true)
	}

	@Test
	fun testBranchedModel2() {
		val input = InputLayer(3)

		val ad0 = Dense(2) { input }
		val ad1 = Dense(2) { ad0 }

		val bd0 = Dense(4) { input }
		val bd1 = Dense(4) { bd0 }
		val relu1 = Activation(Activations.ReLu) { bd1 }
		val bd2 = Dense(4) { bd0 }
		val relu2 = Activation(Activations.ReLu) { bd2 }

		val concat = Concat { listOf(relu1, relu2, ad1) }

		val builder = ModelBuilder(input, concat, debug = false)
		builder.build(debug = true)
	}

	@Test
	fun testLayersManual() {
		val input = InputLayer(3)
		val d1 = Dense(4) { input }
		val activate = Activation(Activations.ReLu) { d1 }


		val inputImpl = input.create()
		printGreen("input", inputImpl.getShape())
		val d1Impl = d1.createFrom(inputImpl.getShape())
		printGreen("dense 1", inputImpl.getShape())
		d1Impl.getTrainable().forEach {
			printBlue("dense w: ${it.describe()}")
		}
		val activateImpl = activate.createFrom(d1Impl.getShape())
		printGreen("activate", activateImpl.getShape())

		val inputData = Suppliers.createMatrix(inputImpl.getShape(), RandomRangeSupplier.INSTANCE)
		var b = inputImpl.call(inputData)
		b.print()
		b = d1Impl.call(b)
		printRed(d1Impl.name)
		b.print()
		b = activateImpl.call(b)
		b.print()
	}

}