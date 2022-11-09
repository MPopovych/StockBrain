import matrix.Matrix
import matrix.MatrixMath
import suppliers.*
import utils.print
import utils.printGreen
import kotlin.test.Test

class MatrixTest {

	@Test
	fun testSaveAndLoad() {
		val a = Matrix(2, 2, Suppliers.RandomRangeNP)
		val stringData = a.readStringData()
		a.print()
		printGreen("String data of matrix: $stringData")

		val b = Matrix(2, 2)
		b.writeStringData(stringData)
		b.print()

		assertEqual(a, b)
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
		val a = Matrix(2, 10, Suppliers.RandomZP)
		a.print()
		println()
		val b = Matrix(2, 10, ConstSupplier(2f))
		MatrixMath.hadamard(a, b, b)
		b.print()
	}

}