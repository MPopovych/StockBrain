package utils.frames.modelframe

import utils.frames.ColumnScaleFilter

interface NamedFrameAsset<Self : NamedFrameAsset<Self>> : FrameAsset, NamedPropModel<Self> {

	override val describeHeader: Array<String>
		get() = propGetter().properties.keys.toTypedArray()

	override fun getValueByKey(key: String): Float? {
		val prop = propGetter().properties[key] ?: return null
		return prop.get(selfUnsafe()).toFloat()
	}

	override fun fill2FArray(destination: FloatArray) {
		var i = 0
		val self = selfUnsafe()
		for (prop in propGetter().properties.values) {
			destination[i++] = prop.get(self).toFloat()
		}
	}

	override fun fill2FArray(destination: FloatArray, featureMasks: ColumnScaleFilter) {
		val self = selfUnsafe()
		val getter = propGetter()

		var i = 0
		for ((key, scale) in featureMasks) {
			val prop = getter.properties[key] ?: throw IllegalStateException("No $key in ${describeHeader.toList()}")
			destination[i++] = scale.applyToValue(prop.get(self).toFloat())
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun selfUnsafe(): Self = this as? Self ?: throw IllegalStateException("Does not point to generic self")
}