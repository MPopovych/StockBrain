package brain.models

import brain.activation.Activations
import brain.layers.*
import brain.utils.fromJson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

object ModelReader {

	internal val innerGson by lazy { GsonBuilder().create() }
	private val gson by lazy {
		GsonBuilder().registerTypeAdapter(LayerSerialized::class.java, LayerDeserializer()).create()
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
		val inputMap = serialized.inputs.mapValues {
			buffer[it.value] as? InputLayer ?: throw IllegalStateException("Input ${it.value} can't be parsed")
		}
		val outputMap = serialized.outputs.mapValues {
			buffer[it.value] ?: throw IllegalStateException("Output ${it.value} can't be parsed")
		}
		val model = Model(inputMap, outputMap, debug = debug)

		serialized.layers.forEach { ls ->
			val layer = model.layersMap[ls.name] ?: throw IllegalStateException("No layer found with name ${ls.name}")
			ls.weights?.forEach { w ->
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

			Transpose.defaultNameType -> {
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in transpose")
				Transpose(name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}
			Pivot.defaultNameType -> {
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in Pivot")
				Pivot(name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Activation.defaultNameType -> {
				val activation = Activations.deserialize(ls.activation) ?: throw IllegalStateException("No activation")
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in activation")
				Activation(activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Dense.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.OnlyBiasMeta)
					?: throw IllegalStateException("No meta for dense")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in dense")
				Dense(units = ls.width, activation = activation, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Sparse.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.OnlyBiasMeta)
					?: throw IllegalStateException("No meta for sparse")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in sparse")
				Sparse(units = ls.width, activation = activation, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Disperse.defaultNameType -> {
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in disperse")
				Disperse(units = ls.width, activation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			GRU.defaultNameType -> {
				val meta =
					(ls.getMetaData() as? LayerMetaData.GRUMeta) ?: throw IllegalStateException("No meta for GRU")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in GRU")
				GRU(activation = activation, units = ls.width, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			GRUIterative.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.GRUMeta)
					?: throw IllegalStateException("No meta for GRU iterative")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in GRU iterative")
				GRUIterative(activation = activation, units = ls.width, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			RNN.defaultNameType -> {
				val meta =
					(ls.getMetaData() as? LayerMetaData.RNNMeta) ?: throw IllegalStateException("No meta for RNN")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in RNN")
				RNN(activation = activation, units = ls.width, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			RNNIterative.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.RNNMeta)
					?: throw IllegalStateException("No meta for RNN iterative")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in RNN iterative")
				RNNIterative(activation = activation, units = ls.width, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Direct.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.OnlyBiasMeta)
					?: throw IllegalStateException("No meta for direct")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in direct")
				Direct(activation = activation, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			ScaleSeries.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.OnlyBiasMeta)
					?: throw IllegalStateException("No meta for scale series")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in scale series")
				ScaleSeries(activation = activation, useBias = meta.useBias, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			PivotNorm.defaultNameType -> {
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in pivot norm")
				PivotNorm(activation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			FeatureFilter.defaultNameType -> {
				val activation = Activations.deserialize(ls.activation)
					?: throw IllegalStateException("No Activation in feature filter")
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in feature filter")
				FeatureFilter(weightActivation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Dropout.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.DropoutMeta)
					?: throw IllegalStateException("No meta for dropout")
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in dropout")
				Dropout(rate = meta.rate, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			ConvDelta.defaultNameType -> {
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in conv delta")
				ConvDelta(activation = activation, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			Concat.defaultNameType -> {
				val parents =
					ls.parents?.map { p -> buffer[p] ?: throw IllegalStateException("No parent found in buffer") }
						?: throw IllegalStateException("No parent in concat")

				Concat(name = ls.name) {
					parents
				}
			}

			AttentionMultiply.defaultNameType -> {
				val parents =
					ls.parents?.map { p -> buffer[p] ?: throw IllegalStateException("No parent found in buffer") }
						?: throw IllegalStateException("No parent in attention multiply")

				AttentionMultiply(name = ls.name) {
					parents
				}
			}

			AttentionAdd.defaultNameType -> {
				val parents =
					ls.parents?.map { p -> buffer[p] ?: throw IllegalStateException("No parent found in buffer") }
						?: throw IllegalStateException("No parent in attention add")

				AttentionAdd(name = ls.name) {
					parents
				}
			}

			AttentionPivot.defaultNameType -> {
				val parents =
					ls.parents?.map { p -> buffer[p] ?: throw IllegalStateException("No parent found in buffer") }
						?: throw IllegalStateException("No parent in attention pivot")

				AttentionPivot(name = ls.name) {
					parents
				}
			}

			Flatten.defaultNameType -> {
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in Flatten")
				Flatten(name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			TimeMask.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.TimeMaskMeta)
					?: throw IllegalStateException("No meta for time mask")
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in time mask")
				TimeMask(fromStart = meta.fromStart, fromEnd = meta.fromEnd, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			FeatureDense.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.FeatureDenseMeta)
					?: throw IllegalStateException("No meta for feature dense")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in feature dense")
				FeatureDense(
					units = ls.height,
					activation = activation,
					useBias = meta.useBias,
					pivotAvg = meta.pivotAvg,
					name = ls.name
				) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			FeatureConv.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.FeatureConvMeta)
					?: throw IllegalStateException("No meta for feature conv")
				val activation = Activations.deserialize(ls.activation)
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in feature conv")
				FeatureConv(
					units = meta.units,
					kernelSize = meta.kernels,
					step = meta.step,
					activation = activation,
					useBias = meta.useBias,
					name = ls.name
				) {
					buffer[parent] ?: throw IllegalStateException("No parent found in buffer")
				}
			}

			FeatureMask.defaultNameType -> {
				val meta = (ls.getMetaData() as? LayerMetaData.FeatureMaskMeta)
					?: throw IllegalStateException("No meta for feature mask")
				val parent = ls.parents?.getOrNull(0) ?: throw IllegalStateException("No parent in feature mask")
				FeatureMask(filterIndexes = meta.allowIndexes, name = ls.name) {
					buffer[parent] ?: throw IllegalStateException("No parent found in feature mask")
				}
			}

			else -> throw NotImplementedError("${ls.nameType} parse not supported")
		}
		buffer[ls.name] = lb
		return lb
	}

}

private class LayerDeserializer : JsonDeserializer<LayerSerialized> {

	companion object {
		const val FIELD_BUILDER_DATA = "builderData"
	}

	override fun deserialize(
		json: JsonElement,
		typeOfT: Type,
		context: JsonDeserializationContext,
	): LayerSerialized {
		val temp = ModelReader.innerGson.fromJson<LayerSerialized>(json)

		return when (temp.nameType) {
			Dense.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.OnlyBiasMeta>(element)
				temp.copy(builderData = data)
			}

			Sparse.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.OnlyBiasMeta>(element)
				temp.copy(builderData = data)
			}

			Direct.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.OnlyBiasMeta>(element)
				temp.copy(builderData = data)
			}

			ScaleSeries.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.OnlyBiasMeta>(element)
				temp.copy(builderData = data)
			}

			PivotNorm.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.OnlyBiasMeta>(element)
				temp.copy(builderData = data)
			}

			Dropout.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.DropoutMeta>(element)
				temp.copy(builderData = data)
			}

			GRU.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.GRUMeta>(element)
				temp.copy(builderData = data)
			}

			GRUIterative.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.GRUMeta>(element)
				temp.copy(builderData = data)
			}

			RNN.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.RNNMeta>(element)
				temp.copy(builderData = data)
			}

			RNNIterative.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.RNNMeta>(element)
				temp.copy(builderData = data)
			}

			FeatureDense.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.FeatureDenseMeta>(element)
				temp.copy(builderData = data)
			}

			FeatureConv.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.FeatureConvMeta>(element)
				temp.copy(builderData = data)
			}

			TimeMask.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.TimeMaskMeta>(element)
				temp.copy(builderData = data)
			}

			FeatureMask.defaultNameType -> {
				val element = json.asJsonObject[FIELD_BUILDER_DATA]
				val data = ModelReader.innerGson.fromJson<LayerMetaData.FeatureMaskMeta>(element)
				temp.copy(builderData = data)
			}

			else -> temp
		}
	}
}