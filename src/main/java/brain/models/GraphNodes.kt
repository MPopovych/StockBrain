package brain.models

import brain.layers.abs.LayerImpl
import brain.matrix.Matrix
import brain.serialization.LayerJsonSerialized
import brain.serialization.LayerNodeSerialized
import brain.serialization.LayerNodeTypeSerialized

sealed interface GraphNodeType {
	val impl: LayerImpl
	val id: String
		get() = impl.id

	data class InputIO(val ioKey: String, override val impl: LayerImpl.LayerSingleInput) : GraphNodeType {
		override fun invoke(buffer: Map<String, Matrix>): Matrix {
			val input = buffer[ioKey] ?: throw IllegalStateException("Missing data in graph: $ioKey")
			return impl.propagate(input)
		}

		override fun copy(): GraphNodeType {
			return InputIO(ioKey, impl.factory.copy(impl) as LayerImpl.LayerSingleInput)
		}
	}

	data class SingleParent(val parent: String, override val impl: LayerImpl.LayerSingleInput) : GraphNodeType {
		override fun invoke(buffer: Map<String, Matrix>): Matrix {
			val input = buffer[parent] ?: throw IllegalStateException("Missing data in graph: $parent")
			return impl.propagate(input)
		}

		override fun copy(): GraphNodeType {
			return SingleParent(parent, impl.factory.copy(impl) as LayerImpl.LayerSingleInput)
		}
	}

	data class MultiParent(val parents: List<String>, override val impl: LayerImpl.LayerMultiInput) : GraphNodeType {
		override fun invoke(buffer: Map<String, Matrix>): Matrix {
			val input = parents.map { parent ->
				buffer[parent] ?: throw IllegalStateException("Missing data in graph: $parent")
			}
			return impl.propagate(input)
		}

		override fun copy(): GraphNodeType {
			return MultiParent(parents, impl.factory.copy(impl) as LayerImpl.LayerMultiInput)
		}
	}

	fun invoke(buffer: Map<String, Matrix>): Matrix
	fun copy(): GraphNodeType

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
}