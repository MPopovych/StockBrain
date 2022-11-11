package brain.models

import brain.activation.Activations
import com.google.gson.GsonBuilder

object ModelWriter {

	private val gson by lazy { GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create() }
	private val prettyGson by lazy { GsonBuilder().setPrettyPrinting().create() }

	fun toJson(model: ModelSerialized, pretty: Boolean = false): String {
		return if (pretty) {
			prettyGson.toJson(model)
		} else {
			gson.toJson(model)
		}
	}

	fun toJson(model: Model, pretty: Boolean = false): String {
		return toJson(serialize(model), pretty)
	}

	fun serialize(model: Model): ModelSerialized {
		val inputs = model.originInputs.mapValues { it.value.name }
		val outputs = model.originOutputs.mapValues { it.value.name }

		val layers = model.nodeGraph.map { entry ->
			val layer = entry.value.layer
			val parents = when (val g = entry.value) {
				is GraphBuffer.DeadEnd -> null
				is GraphBuffer.MultiParent -> g.parents.map { p -> p.layer.name }
				is GraphBuffer.SingleParent -> listOf(g.parent.layer.name)
			}
			return@map LayerSerialized(
				name = layer.name,
				nameType = layer.nameType,
				width = layer.getShape().width,
				height = layer.getShape().height,
				activation = layer.activation?.let { Activations.serialize(it) },
				weights = layer.weights.values.filter { it.trainable }.map { w ->
					WeightSerialized(name = w.name, w.matrix.readStringData())
				}.let { wl -> wl.ifEmpty { null } },
				parents = parents,
				builderData = entry.key.getSerializedBuilderData()
			)
		}

		return ModelSerialized(inputs, outputs, layers)
	}

}
