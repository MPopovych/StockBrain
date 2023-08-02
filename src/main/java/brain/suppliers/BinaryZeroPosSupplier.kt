package brain.suppliers

import kotlin.random.Random

object BinaryZeroPosSupplier : ValueSupplier {
	override fun fill(array: FloatArray) {
		for (i in array.indices) {
			array[i] = if (Random.nextBoolean()) 1f else 0f
		}
	}

	override fun create(size: Int): FloatArray {
		return FloatArray(size) { if (Random.nextBoolean()) 1f else 0f }
	}
}