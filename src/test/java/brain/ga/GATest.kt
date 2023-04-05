package brain.ga

import brain.ga.policies.AdditiveMutationPolicy
import brain.ga.policies.CyclicMutationPolicy
import brain.layers.Direct
import brain.layers.InputLayer
import brain.models.ModelBuilder
import brain.suppliers.Suppliers
import brain.utils.*
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
			m.values.forEachIndexed { y, a ->
				a.forEachIndexed { x, value ->
					m.values[y][x] = -value
				}
			}
		}

		printBlueBr("Input array:\t ${inputArray.describe()}")
		printGrayBr("Expected array:\t ${targetArray.describe()}")

		val settings = GASettings(
			topParentCount = 5,
			totalPopulationCount = 10,
			scoreBoardClearOnGeneration = false,
			scoreBoardAllowSameResult = true,
			scoreBoardOrder = GAScoreBoardOrder.Descending,
			initialMutationPolicy = AdditiveMutationPolicy(1.0),
			mutationPolicy = CyclicMutationPolicy(0.3, 1, 1, 0, 0, 0),
		)

		val ga = GA(settings, model, earlyStopCallback = { i, ga ->
			val top = ga.scoreBoard.getTop()?.score ?: return@GA false
			if (top == 0.0) {
				printRedBr("Stop on gen $i with $top")
				return@GA true
			}
			return@GA false
		})

		brBenchmark("Training session") {
			ga.runFor(generations = 100000, silent = true) {
				val newInput = Suppliers.createMatrix(input.getShape(), Suppliers.RandomBinNP)
				val output = it.model.getOutput(newInput)
				var absoluteError = 0.0
				output.values.forEachIndexed { y, a ->
					a.forEachIndexed { x, value ->
						val error = abs(value - newInput.values[y][x] * -1)
						absoluteError += error
					}
				}

				return@runFor absoluteError
			}
		}

		val top = ga.scoreBoard.getTop() ?: throw IllegalStateException()
		top.genes.applyToModel(model)
		val output = model.getOutput(inputArray)

		printRedBr("Final array:\t ${output.describe()}")
		printGreenBr("Expected array:\t ${targetArray.describe()}")
		ga.scoreBoard.printScoreBoard()
	}

}