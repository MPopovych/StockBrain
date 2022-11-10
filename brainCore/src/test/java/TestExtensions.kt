import ga.weights.WeightGenes
import matrix.Matrix
import java.util.*

fun assertEqualModel(a: Matrix, b: Matrix) {
	assert(Arrays.deepEquals(a.values, b.values))
}

fun assertNotEqualModel(a: Matrix, b: Matrix) {
	assert(!Arrays.deepEquals(a.values, b.values))
}

fun assertNotEqualModel(a: WeightGenes, b: WeightGenes) {
	assert(!a.genes.toTypedArray().contentDeepEquals(b.genes.toTypedArray()))
}