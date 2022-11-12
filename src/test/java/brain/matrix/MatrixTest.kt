package brain.matrix

import brain.assertEqualModel
import brain.suppliers.ConstSupplier
import brain.suppliers.Suppliers
import brain.utils.print
import brain.utils.printGreenBr
import brain.utils.printRedBr
import kotlin.test.Test
import kotlin.test.assertEquals

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
	fun testMultiplyConst() {
		val a = Matrix(3, 2) { _, y -> y + 1f }
		a.print()
		val b = Matrix(3, 3) { x, y -> y.toFloat() * x }
		b.printRedBr()
		val d = Matrix(3, 2, Suppliers.Zero)
		MatrixMath.multiply(a, b, d)
		d.print()

		assertEquals(d.values[0][0], 0f)
		assertEquals(d.values[1][0], 3f)
		assertEquals(d.values[2][0], 6f)

		assertEquals(d.values[0][1], 0f)
		assertEquals(d.values[1][1], 6f)
		assertEquals(d.values[2][1], 12f)

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

}