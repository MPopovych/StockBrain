package brain.models

import brain.layers.abs.LayerImpl
import brain.matrix.Matrix
import brain.serialization.LayerJsonSerialized
import brain.serialization.LayerNodeSerialized
import brain.serialization.LayerNodeTypeSerialized

class GraphNode(
	val type: GraphNodeType,
) {
	val id: String
		get() = impl.id
	val impl: LayerImpl
		get() = type.impl

	fun copy() = GraphNode(type.copy())

	fun invoke(buffer: Map<String, Matrix>): Matrix {
		return when (type) {
			is GraphNodeType.InputIO -> {
				val input = buffer[type.ioKey] ?: throw IllegalStateException("Missing data in graph: ${type.ioKey}")
				type.impl.propagate(input)
			}

			is GraphNodeType.MultiParent -> {
				val input = type.parents.map { parent ->
					buffer[parent] ?: throw IllegalStateException("Missing data in graph: $parent")
				}
				type.impl.propagate(input)
			}

			is GraphNodeType.SingleParent -> {
				val input = buffer[type.parent] ?: throw IllegalStateException("Missing data in graph: ${type.parent}")
				type.impl.propagate(input)
			}
		}
	}

}

sealed class GraphNodeType {
	abstract val impl: LayerImpl
	val id: String
		get() = impl.id

	data class InputIO(val ioKey: String, override val impl: LayerImpl.LayerSingleInput) : GraphNodeType()
	data class SingleParent(val parent: String, override val impl: LayerImpl.LayerSingleInput) : GraphNodeType()
	data class MultiParent(val parents: List<String>, override val impl: LayerImpl.LayerMultiInput) : GraphNodeType()

	fun serialize(): LayerNodeSerialized {
		val id = impl.id
		val type = when (this) {
			is InputIO -> LayerNodeTypeSerialized.InputIO(ioKey)
			is MultiParent -> LayerNodeTypeSerialized.MultiParent(parents)
			is SingleParent -> LayerNodeTypeSerialized.SingleParent(parent)
		}
		val layerDetails = LayerJsonSerialized.wrap(impl)
		return LayerNodeSerialized(
			id, type, layerDetails
		)
	}

	fun copy(): GraphNodeType {
		return when (this) {
			is InputIO -> InputIO(
				ioKey, impl.factory.copy(impl) as? LayerImpl.LayerSingleInput
					?: throw IllegalStateException()
			)

			is MultiParent -> MultiParent(
				parents, impl.factory.copy(impl) as? LayerImpl.LayerMultiInput
					?: throw IllegalStateException()
			)

			is SingleParent -> SingleParent(
				parent, impl.factory.copy(impl) as? LayerImpl.LayerSingleInput
					?: throw IllegalStateException()
			)
		}
	}
}