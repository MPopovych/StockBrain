package brain.models

import brain.layers.LayerMetaData

data class ModelSerialized(
	val inputs: Map<String, String>, // key : gate
	val outputs: Map<String, String>, // key : gate
	val layers: List<LayerSerialized>,
)

data class LayerSerialized(
	val name: String,
	val nameType: String,
	val width: Int, // LayerShape.width
	val height: Int, // LayerShape.height
	val activation: String?,
	val weights: List<WeightSerialized>?,
	val parents: List<String>?,
	private val builderData: Any? = null,
) {
	fun getMetaData(): LayerMetaData? {
		if (builderData == null) return null
		if (builderData is LayerMetaData) {
			return builderData
		}
		throw IllegalStateException("meta data is not of LayerMetaData, its : ${builderData.javaClass.name}")
	}
}

data class WeightSerialized(
	val name: String,
	val value: String,
)