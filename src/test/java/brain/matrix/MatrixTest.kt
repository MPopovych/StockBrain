package brain.matrix

import brain.assertEqual
import brain.assertNotEqual
import brain.suppliers.Suppliers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MatrixTest {

	@Test
	fun testWrap() {
		val a = Matrix.wrap(2, 3, FloatArray(2 * 3))
		val b = Matrix.zeroes(2, 3)
		a.withPos().also {
			assert(it.isNotEmpty())
		}.forEach { (_, _, f) ->
			assert(f == 0f)
		}
		assertEqual(a, b)

		val c = Matrix.wrap(2, 3, FloatArray(2 * 3) { 1f })
		val d = Matrix.ofSupply(2, 3, Suppliers.Ones)
		assertEqual(c, d)
		assertNotEqual(a, c)
	}

	@Test
	fun testZeroInit() {
		val a = Matrix.zeroes(2, 3)
		a.withPos().also {
			assert(it.isNotEmpty())
		}.forEach { (x, y, f) ->
			assert(f == 0f)
		}
	}

	@Test
	fun testSaveAndLoadString() {
		val a = Matrix.ofSupply(2, 3, Suppliers.RandomRangeNP)
		println(a.describe())
		val stringData = a.readStringData()
		println("String data of matrix: $stringData")

		val b = Matrix.fromEncoded(2, 3, stringData)
		println(a.describe())

		assert(a !== b)
		assertEqual(a, b)
		assertEquals(2, b.width)
		assertEquals(3, b.height)
	}

	@Test
	fun testSaveAndWrapFloat() {
		val a = Matrix.ofSupply(2, 3, Suppliers.RandomRangeNP)
		println(a.describe())
		val floatData = a.readFloatData()
		println("Float data of matrix: ${floatData.toList()}")

		val b = Matrix.wrap(2, 3, floatData)
		println(a.describe())

		assert(a !== b)
		assertEqual(a, b)
		assertEquals(2, b.width)
		assertEquals(3, b.height)
	}

	@Test
	fun testSaveAndWriteFloat() {
		val a = Matrix.ofSupply(2, 3, Suppliers.RandomRangeNP)
		println(a.describe())
		val floatData = a.readFloatData()
		println("Float data of matrix: ${floatData.toList()}")

		val b = Matrix.zeroes(2, 3)
		b.writeFloatData(floatData)
		println(a.describe())

		assert(a !== b)
		assertEqual(a, b)
		assertEquals(2, b.width)
		assertEquals(3, b.height)
	}
}