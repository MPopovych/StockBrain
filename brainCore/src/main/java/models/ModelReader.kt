package models

import activation.Activations
import com.google.gson.*
import layers.*
import utils.fromJson
import utils.printGreen
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

	fun modelInstance(json: String, debug: Boolean = false): Model {
		return modelInstance(fromJson(json), debug)
	}

	fun modelInstance(serialized: ModelSerialized, debug: Boolean = false): Model {
		val buffer = LinkedHashMap<String, LayerBuilder<*>>()
		serialized.layers.forEach {
			buildLayer(it, buffer)
		}
		printGreen(buffer.keys)
		val inputMap = serialized.inputs.mapValues {
			buffer[it.value] as? InputLayer ?: throw IllegalStateException("Input ${it.value} can't be parsed")
		}
		val outputMap = serialized.outputs.mapValues {
			buffer[it.value] ?: throw IllegalStateException("Output ${it.value} can't be parsed")
		}
		val model = Model(inputMap, outputMap, debug = debug)

		serialized.layers.forEach { ls ->
			val layer = model.layersMap[ls.name] ?: throw IllegalStateException("No layer found with name ${ls.name}")
			ls.weights?.forEach {  w ->
				val matrix = layer.weights[w.name]?.matrix
					?: throw IllegalStateException("No weight found with name ${w.name} in ${ls.name}")
				matrix.writeStringData(w.value)
			}
		}

		return model
	}

	private fun buildLayer(ls: LayerSerialized, buffer: LinkedHashMap<String, LayerBuilder<*>>): LayerBuilder<*> {
		val lb = when (ls.nameType) {
			InputLayer.defaultNameType -> {
				InputLayer(features = ls.width, steps = ls.height, name = ls.name)
			}
			Activation.defaultNameType -> {
				val meta = (ls.getMetaData() as LayerMetaData.ActivationMeta)
				val activation = Activations.deserialize(meta.activation)
					?: throw IllegalStateException("No activation")
				val parent = ls.parents?.getOrNull(0)
					?: throw IllegalStateException("No parent in activation")
				Activation(activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}
			Dense.defaultNameType -> {
				val meta = (ls.getMetaData() as LayerMetaData.DenseMeta)
				val activation = Activations.deserialize(meta.activation)
					?: throw IllegalStateException("No activation")
				val parent = ls.parents?.getOrNull(0)
					?: throw IllegalStateException("No parent in dense")
				Dense(units = ls.width, activation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}
			Direct.defaultNameType -> {
				val meta = (ls.getMetaData() as LayerMetaData.DirectMeta)
				val activation = Activations.deserialize(meta.activation)
					?: throw IllegalStateException("No activation")
				val parent = ls.parents?.getOrNull(0)
					?: throw IllegalStateException("No parent in direct")
				Direct(activation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}
			Concat.defaultNameType -> {
				val parents = ls.parents
					?.map { p -> buffer[p] ?: throw IllegalStateException("No parent found in buffer") }
					?: throw IllegalStateException("No parent in concat")

				Concat(name = ls.name) {
					parents
				}
			}
			else -> throw NotImplementedError("${ls.nameType} parse not supported")
		}
		buffer[ls.name] = lb
		return lb
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
				val data = ModelReader.innerGson
					.fromJson<LayerMetaData.ActivationMeta>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			Dense.defaultNameType -> {
				val data = ModelReader.innerGson
					.fromJson<LayerMetaData.DenseMeta>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			Direct.defaultNameType -> {
				val data = ModelReader.innerGson
					.fromJson<LayerMetaData.DirectMeta>(json.asJsonObject["builderData"])
				temp.copy(builderData = data)
			}
			else -> temp
		}
	}

}