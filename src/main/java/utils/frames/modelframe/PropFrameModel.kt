package utils.frames.modelframe

interface PropFrameModel<Self : PropFrameModel<Self>>: DataFrameModel, NamedPropModel<Self> {

	override val describeHeader: Array<String>
		get() = propGetter().properties.keys.toTypedArray()

	override fun getValueByKey(key: String): Float? {
		val prop = propGetter().properties[key] ?: return null
		return prop.get(selfUnsafe()).toFloat()
	}

	override fun fill2FArray(destination: FloatArray) {
		var i = 0
		for (prop in propGetter().properties.values) {
			destination[i++] = prop.get(selfUnsafe()).toFloat()
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun selfUnsafe(): Self = this as? Self ?: throw IllegalStateException("Does not point to generic self")
}