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

		when (temp.nameType) {
			Activation.defaultNameType -> {
				val elem = json.asJsonObject["builderData"]
				val data = ModelReader.innerGson.fromJson<ActivationSerialized>(elem)
				return temp.copy(builderData = data)
			}
			Dense.defaultNameType -> {
				val elem = json.asJsonObject["builderData"]
				val data = ModelReader.innerGson.fromJson<DenseSerialized>(elem)
				return temp.copy(builderData = data)
			}
			Direct.defaultNameType -> {
				val elem = json.asJsonObject["builderData"]
				val data = ModelReader.innerGson.fromJson<DirectSerialized>(elem)
				return temp.copy(builderData = data)
			}
			else -> return temp
		}
	}

}