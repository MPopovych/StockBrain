package brain.suppliers

import kotlin.math.sqrt
import kotlin.random.Random

object UniformHeSupplier : ValueSupplier {
	override fun fill(array: FloatArray) {
		for (i in array.indices) {
			array[i] = (Random.nextFloat() * 2f - 1f) * sqrt(2.0f / array.size)
		}
	}

	override fun create(size: Int): FloatArray {
		return FloatArray(size) { (Random.nextFloat() * 2f - 1f) * sqrt(2.0f / size) }
	}
}