import ga.weights.WeightGenes
import matrix.Matrix
import utils.printBlueBr
import utils.printGrayBr
import utils.upscale
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

fun assertEqualModel(a: Matrix, b: Matrix) {
	assert(Arrays.deepEquals(a.values, b.values))
}

fun assertNotEqualModel(a: Matrix, b: Matrix) {
	assert(!Arrays.deepEquals(a.values, b.values))
}

fun assertNotEqualModel(a: WeightGenes, b: WeightGenes) {
	assert(!a.genes.toTypedArray().contentDeepEquals(b.genes.toTypedArray()))
}


class TestExtensions {
	@Test
	fun testUpscale() {
		val f1 = 0.2144f
		val f2 = f1.upscale()
		printGrayBr(f1)
		printBlueBr(f2)
		assertEquals(0.214f, f2)
	}
}