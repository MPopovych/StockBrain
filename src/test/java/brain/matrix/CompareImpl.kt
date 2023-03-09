package brain.matrix

import brain.suppliers.Suppliers
import brain.utils.brBenchmark
import brain.utils.printGreenBr
import kotlin.test.Test

class CompareImpl {

	@Test
	fun testDefaultMatrix() {
		printGreenBr("Starting default test")
		brBenchmark("default") {
			repeat(10000) {
				val a = Matrix(100, 100, Suppliers.RandomBinNP)
				val b = Matrix(100, 100, Suppliers.RandomBinNP)
				val d = Matrix(100, 100)
				MatrixMath.multiply(a, b, d)
			}
		}
	}

	@Test
	fun testFastMatrix() {
		printGreenBr("Starting fast test")
		brBenchmark("fast") {
			repeat(10000) {
				val a = MatrixF(100, 100, Suppliers.RandomBinNP)
				val b = MatrixF(100, 100, Suppliers.RandomBinNP)
				val d = MatrixF(100, 100)
				MatrixFMath.multiply(a, b, d)
			}
		}
	}

	// this is still 30% faster, but will require rewriting everything
//	@Test
//	fun testEJMLMatrix() {
//		printGreenBr("Starting ejm test")
//		brBenchmark("ejm") {
//			repeat(10000) {
//				val a = RandomMatrices_FDRM.rectangle(100, 100, Random(System.currentTimeMillis()))
//				val b = RandomMatrices_FDRM.rectangle(100, 100, Random(System.currentTimeMillis()))
//				val d = FMatrixRMaj(100, 100)
//				CommonOps_FDRM.mult(a, b, d)
//			}
//		}
//	}


}