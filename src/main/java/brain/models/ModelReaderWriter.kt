package brain.models

import brain.serialization.LayerNodeSerialized
import brain.serialization.ModelSerialized
import brain.serialization.tools.Injector
import brain.serialization.tools.ModelDefaultSerializer
import kotlinx.serialization.encodeToString

object ModelReaderWriter {

	fun serializedFromJson(json: String): ModelSerialized {
		return ModelDefaultSerializer.defaultPretty.decodeFromString<ModelSerialized>(json)
	}

	fun modelFromJson(json: String, injector: Injector = Injector.default) = serializedFromJson(json).toModel(injector)

	fun toJson(model: ModelSerialized): String {
		return ModelDefaultSerializer.defaultPretty.encodeToString(model)
	}

	fun toJson(model: Model): String {
		return toJson(serialize(model))
	}

	fun serialize(model: Model): ModelSerialized {
		return ModelSerialized(model.outputKeyByLayerName, model.callOrderedGraph.map {
			LayerNodeSerialized.wrap(it.type)
		})
	}

}
