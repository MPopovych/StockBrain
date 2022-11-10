package models

import assertEqualModel
import matrix.Matrix
import matrix.MatrixMath
import suppliers.*
import utils.print
import utils.printGreen
import kotlin.test.Test

class MatrixTest {

	@Test
	fun testSaveAndLoadString() {
		val a = Matrix(2, 2, Suppliers.RandomRangeNP)
		a.print()
		val stringData = a.readStringData()
		printGreen("String data of matrix: $stringData")

		val b = Matrix(2, 2)
		b.writeStringData(stringData)
		b.print()

		assert(a !== b)
		assertEqualModel(a, b)
	}

	@Test
	fun testSaveAndLoadFloat() {
		val a = Matrix(2, 2, Suppliers.RandomRangeNP)
		a.print()
		val floatData = a.readFloatData()
		printGreen("Float data of matrix: ${floatData.toList()}")

		val b = Matrix(2, 2)
		b.writeFloatData(floatData)
		b.print()

		assert(a !== b)
		assertEqualModel(a, b)
	}

	@Test
	fun testMultiplyСonst() {
		val a = Matrix(3, 2) { x, y -> y + 1f }
		a.print()
		val b = Matrix(3, 3, Suppliers.Ones)
		b.print()
		val d = Matrix(3, 2, Suppliers.Zero)
		MatrixMath.multiply(a, b, d)
		d.print()
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