package brain.matrix

import brain.abs.Shape
import brain.suppliers.Suppliers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MatrixMathOpsTest {

	@Test
	fun testSumHorizontal() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = a.sumHorizontal()

		assertEquals(1, b.width)
		assertEquals(3, b.height)
	}

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
		val c = a dot b
		assert(c.all { it == 0f })
		assertEquals(Shape(3, 3), c.shape())

		val d = Matrix.ofSupply(3, 2, Suppliers.const(1f))
		val e = a dot d
		assert(e.all { it == 2f })
		assertEquals(Shape(3, 3), e.shape())

		assertFails {
			a dot a
		}
	}

	@Test
	fun testMatrixAddRowWise() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		val b = Matrix.ofSupply(2, 1, Suppliers.const(-1f))
		val c = a addBroadcast b
		println(c.describe())
		assert(c.all { it == 0f })

		val d = b addBroadcast b
		println(d.describe())
		assert(d.all { it == -2f })

		assertFails {
			a addBroadcast a
		}
	}

	@Test
	fun testMatrixMultiplyRowWise() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(10f))
		val b = Matrix.ofSupply(2, 1, Suppliers.const(0f))
		val c = a multiplyBroadcast b
		println(c.describe())
		assert(c.all { it == 0f })

		val d = Matrix.ofSupply(2, 1, Suppliers.const(-1f))
		val e = a multiplyBroadcast d
		println(e.describe())
		assert(e.all { it == -10f })

		val f = d multiplyBroadcast d
		println(f.describe())
		assert(f.all { it == 1f })

		assertFails {
			a multiplyBroadcast a
		}
	}

	@Test
	fun testMatrixAddRowWiseDiff() {
		val a = Matrix.ofLambda(2, 4) { x: Int, y: Int, c: Int ->
			y.toFloat()
		}
		val b = Matrix.ofSupply(2, 1, Suppliers.const(1f))
		val c = a addBroadcast b
		println(a.describe())
		println()
		println(c.describe())
	}

	@Test
	fun testMatrixAddAssignRowWiseDiff() {
		val a = Matrix.ofLambda(2, 4) { x: Int, y: Int, c: Int ->
			y.toFloat()
		}
		val b = Matrix.ofSupply(2, 1, Suppliers.const(1f))
		val c = a assignAddBroadcast b
		println(a.describe())
		println()
		println(c.describe())
	}

	@Test
	fun testMatrixMultiplyRowWiseDiff() {
		val a = Matrix.ofLambda(2, 4) { x: Int, y: Int, c: Int ->
			y.toFloat()
		}
		val b = Matrix.ofSupply(2, 1, Suppliers.const(2f))
		val c = a multiplyBroadcast b
		println(a.describe())
		println()
		println(c.describe())
	}

}