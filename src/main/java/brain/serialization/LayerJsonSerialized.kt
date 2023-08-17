package brain.serialization

import brain.layers.abs.LayerImpl
import brain.serialization.tools.Injector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LayerJsonSerialized(
	@ProtoNumber(1) val nameType: String,
	@ProtoNumber(2) val obj: JsonElement,
) {
	companion object {
		fun <T : LayerImpl> wrap(layer: T): LayerJsonSerialized {
			val factory = layer.factory
			return LayerJsonSerialized(
				nameType = factory.typeName,
				obj = factory.serializeJson(layer)
			)
		}
	}

	fun read(injector: Injector): LayerImpl {
		return injector.parseJsonElemAsLayer(nameType, obj)
	}
}