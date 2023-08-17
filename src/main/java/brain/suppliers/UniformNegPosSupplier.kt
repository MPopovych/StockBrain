package brain.suppliers

import kotlin.random.Random

object UniformNegPosSupplier : ValueSupplier {
	override fun fill(array: FloatArray) {
		for (i in array.indices) {
			array[i] = Random.nextFloat() * 2f - 1f
		}
	}

	override fun create(size: Int): FloatArray {
		return FloatArray(size) { Random.nextFloat() * 2f - 1f }
	}
}