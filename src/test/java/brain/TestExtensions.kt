package brain

import brain.genes.WeightGenes
import brain.matrix.Matrix

fun assertEqual(a: Matrix, b: Matrix) {
	assert(equal(a, b))
}

fun assertNotEqual(a: Matrix, b: Matrix) {
	assert(!equal(a, b))
}

fun equal(a: Matrix, b: Matrix): Boolean {
	return a.accessMutableArray().toTypedArray().contentDeepEquals(b.accessMutableArray().toTypedArray())
}

fun assertNotEqual(a: WeightGenes, b: WeightGenes) {
	assert(!equal(a, b))
}

fun assertEqual(a: WeightGenes, b: WeightGenes) {
	assert(equal(a, b))
}

fun equal(a: WeightGenes, b: WeightGenes): Boolean {
	return a.genes.toTypedArray().contentDeepEquals(b.genes.toTypedArray())
}