package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.*
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Flatten(
	uplink: () -> LayerRef,
) : LayerRef {

	private val parent = uplink()
	private val parentWidth = parent.outputShape.width.requireConst()
	private val timeDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(parentWidth, timeDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = ScaleFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = FlattenLayerImpl(name)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class FlattenLayerImpl(
	override val id: String,
) : LayerImpl.LayerSingleInput {

	override val factory = FlattenFactory.asGeneric()

	override fun propagate(input: Matrix): Matrix {
		return input.flatten()
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object FlattenFactory : LayerTypedFactory<FlattenLayerImpl, FlattenSerialized> {
	override val typeName: String = "Flatten"

	override val inputType: KClass<FlattenLayerImpl> = FlattenLayerImpl::class
	override val outputSerializer: SerializationStrategy<FlattenSerialized> = FlattenSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<FlattenSerialized> = FlattenSerialized.serializer()

	override fun copy(value: FlattenLayerImpl): FlattenLayerImpl {
		return FlattenLayerImpl(
			value.id,
		)
	}

	override fun serialize(value: FlattenLayerImpl): FlattenSerialized {
		return FlattenSerialized(
			value.id,
		)
	}

	override fun deserialize(injector: Injector, value: FlattenSerialized): FlattenLayerImpl {
		return FlattenLayerImpl(
			value.id,
		)
	}
}

@Serializable
data class FlattenSerialized(
	val id: String,
)