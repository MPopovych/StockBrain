package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Input(
	featuresDim: Dim,
	stepsDim: Dim = Dim.Const(1),
) : InputLayerRef {

	constructor(features: Int, steps: Int = 1) : this(Dim.Const(features), Dim.Const(steps))

	override val typeName: String = "Input"
	override val outputShape: DimShape = DimShape(featuresDim, stepsDim)
	override val nodeType: LayerNodeType.InputIO = LayerNodeType.InputIO
	override val factory: LayerTypedFactory<*, *> = InputFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		return LayerPropagationEnum.SingleInput(InputLayerImpl(name))
	}
}

class InputLayerImpl(
	override val id: String,
) : LayerImpl.LayerSingleInput {

	override val factory = InputFactory.asGeneric()

	override fun propagate(input: Matrix): Matrix {
		return input
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object InputFactory : LayerTypedFactory<InputLayerImpl, InputSerialized> {
	override val typeName: String = "Input"

	override val inputType: KClass<InputLayerImpl> = InputLayerImpl::class
	override val outputSerializer: SerializationStrategy<InputSerialized> = InputSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<InputSerialized> = InputSerialized.serializer()

	override fun copy(value: InputLayerImpl): InputLayerImpl {
		return InputLayerImpl(value.id)
	}

	override fun deserialize(injector: Injector, value: InputSerialized): InputLayerImpl {
		return InputLayerImpl(value.id)
	}

	override fun serialize(value: InputLayerImpl): InputSerialized {
		return InputSerialized(value.id)
	}
}

@Serializable
data class InputSerialized(
	val id: String,
)