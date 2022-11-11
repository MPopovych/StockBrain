package ga

import ga.policies.AdditiveMutationPolicy
import ga.policies.CyclicMutationPolicy
import layers.Direct
import layers.InputLayer
import models.ModelBuilder
import suppliers.Suppliers
import utils.*
import kotlin.math.abs
import kotlin.test.Test

class GATest {

	@Test
	fun testForReadMe() {
		val input = InputLayer(15)
		val direct = Direct(useBias = false) { input }
		val modelBuilder = ModelBuilder(input = input, output = direct)
		modelBuilder.build()
	}

	@Test
	fun testInputInversion() {
		val input = InputLayer(15)
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

		printBlue("Input array:\t ${inputArray.describe()}")
		printGray("Expected array:\t ${targetArray.describe()}")

		val settings = GASettings(
			topParentCount = 5,
			totalPopulationCount = 10,
			scoreBoardOrder = GAScoreBoardOrder.Descending,
			initialMutationPolicy = AdditiveMutationPolicy(1.0),
			mutationPolicy = CyclicMutationPolicy(0.3),
		)

		val ga = GA(settings, model, earlyStopCallback = { i, ga ->
			val top = ga.scoreBoard.getTop()?.score ?: return@GA false
			if (top == 0.0) {
				printRed("Stop on gen $i with $top")
				return@GA true
			}
			return@GA false
		})

		logBenchmarkResult("Training session") {
			ga.runFor(generations = 100000, silent = true) {
				val newInput = Suppliers.createMatrix(input.getShape(), Suppliers.RandomBinNP)
				val output = it.model.getOutput(newInput)
				var absoluteError = 0.0
				output.values.forEachIndexed { x, a ->
					a.forEachIndexed { y, value ->
						val error = abs(value - newInput.values[x][y] * -1)
						absoluteError += error
					}
				}

				return@runFor absoluteError
			}
		}

		val top = ga.scoreBoard.getTop() ?: throw IllegalStateException()
		top.genes.applyToModel(model)
		val output = model.getOutput(inputArray)

		printRed("Final array:\t ${output.describe()}")
		printGreen("Expected array:\t ${targetArray.describe()}")
		ga.scoreBoard.printScoreBoard()
	}

}