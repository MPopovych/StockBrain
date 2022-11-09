import activation.Activations
import layers.*
import models.ModelBuilder
import models.summary
import suppliers.RandomRangeSupplier
import suppliers.Suppliers
import utils.*
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
		val r1 = model.getOutput(inputData).copy()
		val r2 = model.getOutput(inputData).copy()

		assertEqual(r1, r2)

		val inputData2 = Suppliers.createMatrix(LayerShape(3, 1), RandomRangeSupplier.INSTANCE)
		val r3 = model.getOutput(inputData2).copy()

		inputData.printRed()
		inputData2.printRed()
		r1.print()
		r3.print()
		assertNotEqual(r1, r3)
	}

	@Test
	fun testMultiInputModel() {
		val input1 = InputLayer(3)
		var d1: LB = Dense(4) { input1 }
		d1 = Dense(4) { d1 }
		d1 = Direct { d1 }

		val input2 = InputLayer(3)
		var d2: LB = Dense(4) { input2 }
		d2 = Dense(4) { d2 }
		d2 = Direct { d2 }

		val concat = Concat { listOf(d1, d2) }
		val output = Dense(4, name = "output") { concat }

		val builder = ModelBuilder(mapOf("a" to input1, "b" to input2), output, debug = false)
		printYellow(builder.summary())
		val model = builder.build(debug = false)

		val inputData1 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)
		val inputData2 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)

		val r1 = model.getOutput(mapOf("a" to inputData1, "b" to inputData2)).copy()
		val r2 = model.getOutput(mapOf("a" to inputData1, "b" to inputData2)).copy()
		assertEqual(r1, r2)
		r1.printRed()

		val inputData3 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)
		val inputData4 = Suppliers.createMatrix(LayerShape(3, 1), Suppliers.RandomRangeNP)
		val r3 = model.getOutput(mapOf("a" to inputData3, "b" to inputData4)).copy()
		assertNotEqual(r1, r3)
		r3.print()
	}

	@Test
	fun testBranchedModel() {
		val input = InputLayer(3)

		val d0 = Dense(4, Activations.ReLu, name = "d0") { input }

		val d1 = Dense(4, Activations.ReLu, name = "d1") { d0 }
		val d2 = Dense(4, Activations.ReLu, name = "d2") { d0 }

		val concat = Concat { listOf(d1, d2) }

		val builder = ModelBuilder(input, concat, debug = true)
		builder.build(debug = false)

		printYellow(builder.summary())
	}

	@Test
	fun testBranchedModel2() {
		val input = InputLayer(3)

		val ad0 = Dense(2) { input }
		val ad1 = Dense(2) { ad0 }
		val ad2 = Dense(2) { ad1 }
		val ad3 = Dense(2) { ad2 }

		val bd0 = Dense(4) { input }
		val bd1 = Dense(4) { bd0 }
		val bd2 = Dense(4) { bd0 }

		val concat = Concat { listOf(ad3, bd1, bd2) }

		val builder = ModelBuilder(input, concat, debug = false)
		printYellow(builder.summary())
	}

	@Test
	fun testLayersManual() {
		val input = InputLayer(3)
		val d1 = Dense(4) { input }
		val activate = Activation(Activations.ReLu) { d1 }


		val inputImpl = input.create()
		printGreen("input", inputImpl.getShape())
		val d1Impl = d1.create()
		printGreen("dense 1", inputImpl.getShape())
		d1Impl.getTrainable().forEach {
			printBlue("dense w: ${it.describe()}")
		}
		val activateImpl = activate.create()
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