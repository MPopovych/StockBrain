package brain.matrix

import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printGreenBr
import org.ejml.data.FMatrixRMaj
import org.ejml.dense.row.CommonOps_FDRM
import org.ejml.dense.row.RandomMatrices_FDRM
import org.jetbrains.kotlinx.multik.api.d2array
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import java.util.*
import kotlin.test.Test

class CompareImpl {

	private val size = 32
	private val iterations = 100000

	@Test
	fun testDefaultMatrix() {
		val supplier = Suppliers.RandomBinNP
		val a = Matrix(size, size, supplier)
		val b = Matrix(size, size, supplier)
		val d = Matrix(size, size)
		printGreenBr("Starting default test")
		brBenchmark("default") {
			repeat(iterations) {

				MatrixMath.multiply(a, b, d)
			}
		}
	}

	/**
	 * for some reason this implementation is slower
	 */
	@Test
	fun testUniArrayMatrix() {
		val supplier = Suppliers.RandomBinNP
		val a = MatrixF(size, size, supplier)
		val b = MatrixF(size, size, supplier)
		val d = MatrixF(size, size)
		printGreenBr("Starting uni array test")
		brBenchmark("uni array") {
			repeat(iterations) {

				MatrixFMath.multiply(a, b, d)
			}
		}
	}

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
		val supplier = Suppliers.RandomBinNP
		val randomAArray = Array(size) {
			val sub = FloatArray(size)
			supplier.fill(sub)
			sub
		}
		val a = org.ujmp.core.Matrix.Factory.rand(size.toLong(), size.toLong())
		val randomBArray = Array(size) {
			val sub = FloatArray(size)
			supplier.fill(sub)
			sub
		}
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
		val supplier = Suppliers.RandomBinNP
		val a = mk.d2array<Double>(10, 20) { supplier.supply(size, 0, 0).toDouble() }
		val b = mk.d2array<Double>(20, size) { supplier.supply(size, 0, 0).toDouble() }
		printGreenBr(b.shape.toList())

		printGreenBr("Starting Multik test")
		brBenchmark("Multik") {
			repeat(iterations) {
				val c = mk.linalg.dot(a, b)
				if (it == 0) {
					println("shape: ${c.shape.toList()}")
				}
			}
		}
	}


}