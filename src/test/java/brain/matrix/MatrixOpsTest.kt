package brain.matrix

import brain.suppliers.Suppliers
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MatrixOpsTest {

	@Test
	fun testConcat() {
		val a = Matrix.zeroes(2, 3)
		val b = Matrix.zeroes(2, 3)

		val c = listOf(a, b).concat(1)
		assertEquals(4, c.width)
		assertEquals(3, c.height)

		val d = listOf(a, b).concat(0)
		assertEquals(2, d.width)
		assertEquals(6, d.height)
	}

	@Test
	fun testMap() {
		val a = Matrix.ofLambda(2, 3) { x, y, c ->
			1f + (x + y).toFloat()
		}
		val b = a.map { 0f }
		assert(!a.any { fl -> fl == 0f })
		assert(b.all { fl -> fl == 0f })
	}

	@Test
	fun testWithPos() {
		val a = Matrix.ofLambda(2, 3) { x, y, _ ->
			(x + y).toFloat()
		}
		val iterList = a.withPos()
		assert(iterList.isNotEmpty())
		iterList.forEach { (x, y, f) ->
			assert(f == (x + y).toFloat())
		}
	}

	@Test
	fun testIterator() {
		var initCount = 0
		val a = Matrix.ofLambda(2, 3) { _, _, _ ->
			(initCount++).toFloat()
		}
		var assertCount = 0
		a.iteratorWithPos().also {
			assert(it.hasNext())
		}.forEach { (_, _, v) ->
			assertEquals((assertCount++).toFloat(), v)
		}
	}

	@Test
	fun testFlatten() {
		var initCount = 0
		val a = Matrix.ofLambda(2, 3) { _, _, _ ->
			(initCount++).toFloat()
		}.flatten()

		var assertCount = 0
		a.iteratorWithPos().also {
			assert(it.hasNext())
		}.forEach { (x, y, v) ->
			assertEquals((assertCount++).toFloat(), v)
			assertEquals(x.toFloat(), v)
			assertEquals(0, y)
		}
	}

	@Test
	fun testIteratorWithPos() {
		var count = 0
		val a = Matrix.ofLambda(2, 3) { x, y, _ ->
			(x + y).toFloat()
		}
		val iterList = a.iteratorWithPos()
		assert(iterList.hasNext())
		iterList.forEach { (x, y, f) ->
			count++
			assert(f == (x + y).toFloat())
		}
		assert(count == 2 * 3)
	}

	@Test
	fun testMatrixAllFunction() {
		var count = 0
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))
		assert(a.all {
			count++
			it == 1f
		})
		assert(count == 2 * 3)
	}

	@Test
	fun testMatrixAnyFunction() {
		val a = Matrix.ofSupply(2, 3, Suppliers.const(1f))

		var countA = 0
		assert(a.any {
			countA++
			it == 1f
		})
		assert(countA == 1) // until first

		var countB = 0
		assert(!a.any {
			countB++
			it == 0f
		})
		assert(countB == 2 * 3) // until end
	}

}