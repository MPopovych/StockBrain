package brain.matrix

import brain.abs.Shape
import brain.suppliers.Suppliers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MatrixMathOpsTest {

	@Test
	fun testMatrixAdd() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = Matrix.ofSupply(2, 3, Suppliers.const(-1f))
		val c = a add b
		assert(c.all { it == 0f })

		val d = a add -2f
		assert(d.all { it == -1f })

		assertFails {
			a add Matrix.ofSupply(2, 2, Suppliers.const(1f))
		}
	}

	@Test
	fun testMatrixSub() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val c = a sub b
		assert(c.all { it == 0f })

		val d = a sub 2f
		assert(d.all { it == -1f })

		assertFails {
			a sub Matrix.ofSupply(2, 2, Suppliers.const(1f))
		}
	}

	@Test
	fun testMatrixDot() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = Matrix.ofSupply(3, 2, Suppliers.const(0f))
		val c = a multiplyDot b
		assert(c.all { it == 0f })
		assertEquals(Shape(3, 3), c.shape())

		val d = Matrix.ofSupply(3, 2, Suppliers.const(1f))
		val e = a multiplyDot d
		assert(e.all { it == 2f })
		assertEquals(Shape(3, 3), e.shape())

		assertFails {
			a multiplyDot a
		}
	}

	@Test
	fun testMatrixAddRowWise() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = Matrix.ofSupply(2, 1, Suppliers.const(-1f))
		val c = a add1DToEachRow b
		assert(c.all { it == 0f })

		val d = b add1DToEachRow b
		assert(d.all { it == -2f })

		assertFails {
			a add1DToEachRow a
		}
	}

	@Test
	fun testMatrixMultiplyRowWise() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(10f))
		val b = Matrix.ofSupply(2, 1, Suppliers.const(0f))
		val c = a multiply1DToEachRow b
		assert(c.all { it == 0f })

		val d = Matrix.ofSupply(2, 1, Suppliers.const(-1f))
		val e = a multiply1DToEachRow d
		assert(e.all { it == -10f })

		val f = d multiply1DToEachRow d
		assert(f.all { it == 1f })

		assertFails {
			a multiply1DToEachRow a
		}
	}

}