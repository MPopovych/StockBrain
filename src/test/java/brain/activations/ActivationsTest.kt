package brain.activations

import brain.activation.abs.Activations
import brain.matrix.Matrix
import brain.matrix.describe
import brain.suppliers.Suppliers
import kotlin.test.Test

class ActivationsTest {


	@Test
	fun testSoftMax() {
		val a = Matrix.ofSupply(4, 4, Suppliers.UniformHE)
		val b = Activations.Softmax.call(a)
		println(a.describe())
		println()
		println(b.describe())
	}



}
