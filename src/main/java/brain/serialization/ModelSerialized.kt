@file:OptIn(ExperimentalSerializationApi::class)

package brain.serialization

import brain.models.Model
import brain.serialization.tools.Injector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class ModelSerialized(
	@ProtoNumber(1) val outputs: Map<String, String>, // key : gate
	@ProtoNumber(2) val layers: List<LayerNodeSerialized>, // in call order
) {
	fun toModel(injector: Injector = Injector.default, check: Boolean = false): Model {
		return Model(
			outputKeyByLayerName = outputs,
			callOrderedGraph = layers.map { it.deserialize(injector) },
			check = check
		)
	}
}