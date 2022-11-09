package models

import com.google.gson.*
import layers.*
import utils.fromJson
import java.lang.reflect.Type

object ModelReader {

	internal val innerGson by lazy { GsonBuilder().create() }
	private val gson by lazy {
		GsonBuilder()
			.registerTypeAdapter(LayerSerialized::class.java, LayerDeserializer())
			.create()
	}

	fun fromJson(json: String): ModelSerialized {
		return gson.fromJson(json)
	}

}

private class LayerDeserializer : JsonDeserializer<LayerSerialized> {
	override fun deserialize(
		json: JsonElement,
		typeOfT: Type,
		context: JsonDeserializationContext,
	): LayerSerialized {
		val temp = ModelReader.innerGson.fromJson<LayerSerialized>(json)

		return when (temp.nameType) {
			Activation.defaultNameType -> {
				val data = ModelReader.innerGson.fromJson<LayerMetaData.Activation>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			Dense.defaultNameType -> {
				val data = ModelReader.innerGson.fromJson<LayerMetaData.Dense>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			Direct.defaultNameType -> {
				val data = ModelReader.innerGson.fromJson<LayerMetaData.Direct>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			else -> temp
		}
	}

}