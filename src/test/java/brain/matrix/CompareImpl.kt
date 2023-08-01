package brain.matrix

import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printGreenBr
import org.ejml.data.FMatrixRMaj
import org.ejml.dense.row.CommonOps_FDRM
import org.ejml.dense.row.RandomMatrices_FDRM
import java.util.*
import kotlin.test.Test

class CompareImpl {

	private val size = 32
	private val iterations = 100000

	//	 this is still 30% faster, but will require rewriting everything
	@Test
	fun testEJMLMatrix() {
		val supplier = Random(System.currentTimeMillis())
		val a = RandomMatrices_FDRM.rectangle(size, size, supplier)
		val b = RandomMatrices_FDRM.rectangle(size, size, supplier)
		val d = FMatrixRMaj(size, size)
		printGreenBr("Starting ejm test")
		brBenchmark("ejm") {
			repeat(iterations) {
				CommonOps_FDRM.mult(a, b, d)
			}
		}
	}

	@Test
	fun testUJMPMatrix() {
		val a = org.ujmp.core.Matrix.Factory.rand(size.toLong(), size.toLong())
		val b = org.ujmp.core.Matrix.Factory.rand(size.toLong(), size.toLong())
		printGreenBr(b.size.toList())

		printGreenBr("Starting UJMP test")
		brBenchmark("UJMP") {
			repeat(iterations) {
				val c = a.mtimes(b)
			}
		}
	}

	@Test
	fun testMultikMatrix() {
		val supplier = Suppliers.RandomHE
		val a = Matrix.ofSupply(size, size, supplier)
		val b = Matrix.ofSupply(size, size, supplier)

		printGreenBr("Starting Multik test")
		brBenchmark("Multik") {
			repeat(iterations) {
				val c = a multiplyDot b
				if (it == 0) {
					println("shape: ${c.describe()}")
				}
			}
		}
	}


}