package brain.genes

import brain.utils.encodeToBase64

class WeightGenes(
	val width: Int,
	val height: Int,
	val genes: FloatArray,
	val trainable: Boolean,
) {
	val size: Int
		get() = genes.size

	fun chromosome() = genes.encodeToBase64()

	fun copy() = WeightGenes(width, height, genes.copyOf(), trainable)
}