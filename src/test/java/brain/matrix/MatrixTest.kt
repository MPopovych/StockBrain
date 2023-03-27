package brain.matrix

import brain.assertEqualModel
import brain.suppliers.ConstSupplier
import brain.suppliers.Suppliers
import brain.utils.print
import brain.utils.printGreenBr
import brain.utils.printRedBr
import brain.utils.reshapeToMatrix
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MatrixTest {

	@Test
	fun testSaveAndLoadString() {
		val a = Matrix(2, 3, Suppliers.RandomRangeNP)
		a.print()
		val stringData = a.readStringData()
		printGreenBr("String data of matrix: $stringData")

		val b = Matrix(2, 3)
		b.writeStringData(stringData)
		b.print()

		assert(a !== b)
		assertEqualModel(a, b)
	}

	@Test
	fun testSaveAndLoadFloat() {
		val a = Matrix(2, 3, Suppliers.RandomRangeNP)
		a.print()
		val floatData = a.readFloatData()
		printGreenBr("Float data of matrix: ${floatData.toList()}")

		val b = Matrix(2, 3)
		b.writeFloatData(floatData)
		b.print()

		assert(a !== b)
		assertEqualModel(a, b)
	}

	@Test
	fun testMultiplyConst2() {
		val a = Matrix(4, 1) { _, _, y -> 1f }
		val b = floatArrayOf(1f, 0.5f, 0.3f, 0.1f).reshapeToMatrix(1, 4)
		b.printRedBr()
		val d = Matrix(1, 1, Suppliers.Ones)
		MatrixMath.multiply(a, b, d)
		d.print()
		assertEquals(d.values[0][0], 1.9f)
	}

	@Test
	fun testMultiplyConst() {
		val a = Matrix(3, 2) { _, _, y -> y + 1f }
		a.print()
		val b = Matrix(3, 3) { _, x, y -> y.toFloat() * x }
		b.printRedBr()
		val d = Matrix(3, 2, Suppliers.Zero)
		MatrixMath.multiply(a, b, d)
		d.print()

		assertEquals(d.values[0][0], 0f)
		assertEquals(d.values[0][1], 3f)
		assertEquals(d.values[0][2], 6f)

		assertEquals(d.values[1][0], 0f)
		assertEquals(d.values[1][1], 6f)
		assertEquals(d.values[1][2], 12f)

	}

	@Test
	fun testMultiply() {
		val a = Matrix(2, 10, Suppliers.RandomZP)
		a.print()
		val b = Matrix(2, 2, Suppliers.Ones)
		b.print()
		val d = Matrix(2, 10, Suppliers.Zero)
		MatrixMath.multiply(a, b, d)
		d.print()
	}

	@Test
	fun testMultiplyBig() {
		val a = Matrix(64, 64, Suppliers.Ones)
//		a.print()
		val b = Matrix(64, 64, Suppliers.const(2f))
		val d = Matrix(64, 64, Suppliers.Zero)
		MatrixMath.multiply(a, b, d)
		println()
		d.print()
	}

	@Test
	fun testAdd() {
		val a = Matrix(2, 10, Suppliers.RandomZP)
		a.print()
		val b = Matrix(2, 10, Suppliers.Ones)
		val d = Matrix(2, 10, Suppliers.Zero)
		MatrixMath.add(a, b, d)
		println()
		d.print()
	}

	@Test
	fun testSelfAdd() {
		val a = Matrix(2, 10, Suppliers.RandomZP)
		a.print()
		println()
		val b = Matrix(2, 10, Suppliers.Ones)
		MatrixMath.add(a, b, b)
		b.print()
	}

	@Test
	fun testHadamard() {
		val a = Matrix(3, 1, Suppliers.RandomZP)
		a.print()
		println()
		val b = Matrix(3, 1, ConstSupplier(2f))
		MatrixMath.hadamard(a, b, b)
		b.print()
	}

	@Test
	fun testFlush() {
		val a = Matrix(3, 3, Suppliers.Ones)

		a.print()
		println()

		for (array in a.values) {
			for (v in array) {
				assertEquals(1f, v)
			}
		}
		MatrixMath.flush(a)
		a.print()
		for (array in a.values) {
			for (v in array) {
				assertEquals(0f, v)
			}
		}
	}

}