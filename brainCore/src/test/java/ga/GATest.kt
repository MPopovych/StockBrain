package ga

import ga.policies.AdditiveMutationPolicy
import layers.Direct
import layers.InputLayer
import models.ModelBuilder
import suppliers.Suppliers
import utils.describe
import utils.print
import utils.printGray
import utils.printRed
import kotlin.math.max
import kotlin.math.pow
import kotlin.test.Test

class GATest {

	@Test
	fun testInputInversion() {
		val input = InputLayer(12)
		val direct = Direct(useBias = false) { input }
		val modelBuilder = ModelBuilder(input = input, output = direct)
		val model = modelBuilder.build()

		val inputArray = Suppliers.createMatrix(input.getShape(), Suppliers.RandomBinNP)
		val targetArray = inputArray.copy().also { m ->
			m.values.forEachIndexed { x, a ->
				a.forEachIndexed { y, value ->
					m.values[x][y] = -value
				}
			}
		}

		inputArray.print()
		targetArray.print()

		val settings = GASettings(
			topParentCount = 4,
			totalPopulationCount = 100,
			initialMutationPolicy = AdditiveMutationPolicy(0.3),
			mutationPolicy = AdditiveMutationPolicy(0.01),
		)

		val ga = GA(settings, model, onGeneration = { localGa ->
//			val top = localGa.scoreBoard.getTop() ?: throw IllegalStateException()
//			top.genes.applyToModel(model)
//			val output = model.getOutput(inputArray)
//			printGray("prelim: ${output.describe()}")
		})

		ga.runFor(50000) {
			val output = it.model.getOutput(inputArray)
			var mse = 0.0

			output.values.forEachIndexed { x, a ->
				a.forEachIndexed { y, value ->
					val error = (value - targetArray.values[x][y]).pow(2) / output.height * output.width
					mse += error
				}
			}
//				printRed("MSE score: $mse")
			return@runFor 1.0 / max(mse, 0.0000001)
		}

		val top = ga.scoreBoard.getTop() ?: throw IllegalStateException()
		top.genes.applyToModel(model)
		val output = model.getOutput(inputArray)
		output.printRed()
		targetArray.print()

	}

}