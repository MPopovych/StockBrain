package brain.layers.impl

import brain.abs.DimShape
import brain.activation.abs.ActivationFunction
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.propagation.PropagationContext
import brain.serialization.ActivationJsonSerialized
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Activation(
	private val activation: ActivationFunction,
	uplink: () -> LayerRef,
) : LayerRef {
	private val parent = uplink()
	override val outputShape: DimShape = parent.outputShape
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = ActivationFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = ActivationLayerImpl(name, activation = activation)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class ActivationLayerImpl(
	override val id: String,
	internal val activation: ActivationFunction,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = ActivationFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		return activation.call(input)
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object ActivationFactory : LayerTypedFactory<ActivationLayerImpl, ActivationSerialized> {
	override val typeName: String = "Activation"

	override val inputType: KClass<ActivationLayerImpl> = ActivationLayerImpl::class
	override val outputSerializer: SerializationStrategy<ActivationSerialized> = ActivationSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<ActivationSerialized> = ActivationSerialized.serializer()

	override fun copy(value: ActivationLayerImpl): ActivationLayerImpl {
		return ActivationLayerImpl(
			value.id,
			activation = value.activation,
		)
	}

	override fun serialize(value: ActivationLayerImpl): ActivationSerialized {
		val activationWrap = value.activation.let { ActivationJsonSerialized.wrap(it) }
		return ActivationSerialized(
			value.id,
			activation = activationWrap
		)
	}

	override fun deserialize(injector: Injector, value: ActivationSerialized): ActivationLayerImpl {
		val activation = value.activation.read(injector)
		return ActivationLayerImpl(
			value.id,
			activation = activation
		)
	}
}

@Serializable
data class ActivationSerialized(
	val id: String,
	val activation: ActivationJsonSerialized,
)