package models

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
	val weights: List<WeightSerialized>?,
	val parents: List<String>?,
	val builderData: Any? = null,
)

data class WeightSerialized(
	val name: String,
	val value: String,
)