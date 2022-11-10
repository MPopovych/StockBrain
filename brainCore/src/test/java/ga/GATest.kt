package ga

import activation.Activations
import ga.policies.AdditiveMutationPolicy
import layers.Direct
import layers.InputLayer
import models.ModelBuilder
import suppliers.Suppliers
import utils.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test

class GATest {

	@Test
	fun testInputInversion() {
		val input = InputLayer(12)
		val direct = Direct(activation = Activations.BinaryNegPos, useBias = false) { input }
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

		printBlue("Input array: ${inputArray.describe()}")
		printGray("Expected array: ${targetArray.describe()}")

		val settings = GASettings(
			topParentCount = 50,
			totalPopulationCount = 100,
			initialMutationPolicy = AdditiveMutationPolicy(0.3),
			mutationPolicy = AdditiveMutationPolicy(0.2),
		)

		val ga = GA(settings, model, earlyStopCallback = { i, best ->
			val top = best.scoreBoard.getTop()?.score ?: return@GA false
			if (top == Double.MAX_VALUE) {
				printRed("Stop on gen $i with $top")
				return@GA true
			}
			return@GA false
		})

		logBenchmarkResult("Training session") {
			ga.runFor(2000, silent = true) {
				val output = it.model.getOutput(inputArray)
				var sae = 0.0
				output.values.forEachIndexed { x, a ->
					a.forEachIndexed { y, value ->
						val error = abs(value - targetArray.values[x][y])
						sae += error
					}
				}
				sae = sae.pow(2)
				if (sae == 0.0) return@runFor Double.MAX_VALUE
				return@runFor 1.0 / sae
			}
		}

		val top = ga.scoreBoard.getTop() ?: throw IllegalStateException()
		top.genes.applyToModel(model)
		val output = model.getOutput(inputArray)

		printRed("Final array: ${output.describe()}")
		printGreen("Expected array: ${targetArray.describe()}")
	}

}