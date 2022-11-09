package models

class ModelWriter {

	fun write(model: Model) {
		val builder = model.revertToBuilder()


	}

}

data class ModelSerialized(
	val inputs: Map<String, String>, // key : gate
	val outputs: Map<String, String>, // key : gate

)

data class LayerSerialized(
	val name: String,
	val width: Int, // LayerShape.width
	val height: Int, // LayerShape.height
	val weights: List<WeightSerialized>?,

)

data class WeightSerialized(
	val name: String,
	val value: String
)