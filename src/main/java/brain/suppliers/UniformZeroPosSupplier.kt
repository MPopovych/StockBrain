package brain.suppliers

import kotlin.random.Random

object UniformZeroPosSupplier : ValueSupplier {
	override fun fill(array: FloatArray) {
		for (i in array.indices) {
			array[i] = Random.nextFloat()
		}
	}

	override fun create(size: Int): FloatArray {
		return FloatArray(size) { Random.nextFloat() }
	}
}