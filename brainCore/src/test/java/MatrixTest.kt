import matrix.Matrix
import matrix.MatrixMath
import suppliers.*
import utils.print
import kotlin.test.Test

class MatrixTest {

	@Test
	fun testMultiply() {
		val a = Matrix(2, 10, RandomSupplier.INSTANCE)
		a.print()
		val b = Matrix(2, 2, OnesSupplier.INSTANCE)
		b.print()
		val d = Matrix(2, 10, ZeroSupplier.INSTANCE)
		MatrixMath.multiply(a, b, d)
		d.print()
	}

	@Test
	fun testAdd() {
		val a = Matrix(2, 10, RandomSupplier.INSTANCE)
		a.print()
		val b = Matrix(2, 10, OnesSupplier.INSTANCE)
		val d = Matrix(2, 10, ZeroSupplier.INSTANCE)
		MatrixMath.add(a, b, d)
		d.print()
	}

	@Test
	fun testSelfAdd() {
		val a = Matrix(2, 10, RandomSupplier.INSTANCE)
		a.print()
		val b = Matrix(2, 10, OnesSupplier.INSTANCE)
		MatrixMath.add(a, b, b)
		b.print()
	}

	@Test
	fun testHadamard() {
		val a = Matrix(2, 10, RandomSupplier.INSTANCE)
		a.print()
		val b = Matrix(2, 10, ConstSupplier(2f))
		MatrixMath.hadamard(a, b, b)
		b.print()
	}

}