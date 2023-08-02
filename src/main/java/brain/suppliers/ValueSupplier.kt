package brain.suppliers

interface ValueSupplier {
	fun fill(array: FloatArray)
	fun create(size: Int): FloatArray
}