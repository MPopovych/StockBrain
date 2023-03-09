package brain.matrix

import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printGreenBr
//import org.ejml.data.FMatrixRMaj
//import org.ejml.dense.row.CommonOps_FDRM
//import org.ejml.dense.row.RandomMatrices_FDRM
//import java.util.*
import kotlin.test.Test

class CompareImpl {

	private val size = 100
	private val iterations = 100000000 / (size * size)
	@Test
	fun testDefaultMatrix() {
		printGreenBr("Starting default test")
		brBenchmark("default") {
			repeat(iterations) {
				val a = Matrix(size, size, Suppliers.RandomBinNP)
				val b = Matrix(size, size, Suppliers.RandomBinNP)
				val d = Matrix(size, size)
				MatrixMath.multiply(a, b, d)
			}
		}
	}

	/**
	 * for some reason this implementation is slower
	 */
	@Test
	fun testUniArrayMatrix() {
		printGreenBr("Starting uni array test")
		brBenchmark("uni array") {
			repeat(iterations) {
				val a = MatrixF(size, size, Suppliers.RandomBinNP)
				val b = MatrixF(size, size, Suppliers.RandomBinNP)
				val d = MatrixF(size, size)
				MatrixFMath.multiply(a, b, d)
			}
		}
	}

////	 this is still 30% faster, but will require rewriting everything
//	@Test
//	fun testEJMLMatrix() {
//		printGreenBr("Starting ejm test")
//		brBenchmark("ejm") {
//			repeat(iterations) {
//				val a = RandomMatrices_FDRM.rectangle(size, size, Random(System.currentTimeMillis()))
//				val b = RandomMatrices_FDRM.rectangle(size, size, Random(System.currentTimeMillis()))
//				val d = FMatrixRMaj(size, size)
//				CommonOps_FDRM.mult(a, b, d)
//			}
//		}
//	}


}