package brain.matrix

import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printGreenBr
import kotlin.test.Test

class CompareImpl {

	private val size = 32
	private val iterations = 100000

	@Test
	fun testMultikMatrix() {
		val supplier = Suppliers.UniformHE
		val a = Matrix.ofSupply(size, size, supplier)
		val b = Matrix.ofSupply(size, size, supplier)

		printGreenBr("Starting Multik test")
		brBenchmark("Multik") {
			repeat(iterations) {
				val c = a dot b
				if (it == 0) {
					println("shape: ${c.describe()}")
				}
			}
		}
	}


}