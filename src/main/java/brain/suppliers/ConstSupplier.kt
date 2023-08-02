package brain.suppliers

open class ConstSupplier(val const: Float) : ValueSupplier {
	override fun fill(array: FloatArray) {
		array.fill(const)
	}

	override fun create(size: Int): FloatArray {
		return FloatArray(size) { const }
	}
}