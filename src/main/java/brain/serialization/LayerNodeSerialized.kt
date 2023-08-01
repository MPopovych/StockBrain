package brain.serialization

import brain.layers.abs.LayerImpl
import brain.models.GraphNodeType
import brain.serialization.tools.Injector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class LayerNodeSerialized(
	@ProtoNumber(1) val id: String,
	@ProtoNumber(2) val type: LayerNodeTypeSerialized,
	@ProtoNumber(3) val layerDetails: LayerJsonSerialized,
) {

	companion object {
		fun wrap(layer: GraphNodeType): LayerNodeSerialized {
			return layer.serialize()
		}
	}

	fun deserialize(injector: Injector): GraphNodeType {
		val layerImpl = layerDetails.read(injector)
		return when (type) {
			is LayerNodeTypeSerialized.InputIO -> GraphNodeType.InputIO(
				type.ioKey, layerImpl as? LayerImpl.LayerSingleInput ?: throw IllegalStateException()
			)

			is LayerNodeTypeSerialized.MultiParent -> GraphNodeType.MultiParent(
				type.parents, layerImpl as? LayerImpl.LayerMultiInput ?: throw IllegalStateException()
			)

			is LayerNodeTypeSerialized.SingleParent -> GraphNodeType.SingleParent(
				type.parent, layerImpl as? LayerImpl.LayerSingleInput ?: throw IllegalStateException()
			)
		}
	}
}

@Serializable
sealed interface LayerNodeTypeSerialized {
	@Serializable
	class InputIO(val ioKey: String) : LayerNodeTypeSerialized

	@Serializable
	data class SingleParent(val parent: String) : LayerNodeTypeSerialized

	@Serializable
	data class MultiParent(val parents: List<String>) : LayerNodeTypeSerialized
}