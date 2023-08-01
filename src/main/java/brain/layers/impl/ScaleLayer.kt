package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.activation.abs.ActivationFunction
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.add1DToEachRow
import brain.matrix.multiply1DToEachRow
import brain.serialization.ActivationJsonSerialized
import brain.serialization.WeightSerialized
import brain.serialization.tools.Injector
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Scale(
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomHE,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	uplink: () -> LayerRef,
) : LayerRef {

	private val parent = uplink()
	private val parentWidth = parent.outputShape.width.requireConst()
	private val timeDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(parentWidth, timeDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = ScaleFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val weightMatrix = Matrix.ofSupply(parentWidth, Dim.Const(1), kernelInit)
		val biasMatrix = Matrix.ofSupply(parentWidth, Dim.Const(1), biasInit)
		val weight = WeightData("weight", weightMatrix, active = true, trainable = true)
		val bias = WeightData("bias", biasMatrix, active = useBias, trainable = useBias)
		val impl = ScaleLayerImpl(name, activation = activation, weight = weight, bias = bias)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class ScaleLayerImpl(
	override val id: String,
	internal val activation: ActivationFunction?,
	internal val weight: WeightData,
	internal val bias: WeightData,
) : LayerImpl.LayerSingleInput {

	override val factory = ScaleFactory.asGeneric()

	override fun propagate(input: Matrix): Matrix {
		var result = input multiply1DToEachRow weight.matrix
		if (bias.active) {
			result = result add1DToEachRow bias.matrix
		}
		if (activation != null) {
			result = activation.call(result)
		}
		return result
	}

	override fun weightData(): List<WeightData> = listOf(weight, bias)
}

object ScaleFactory : LayerTypedFactory<ScaleLayerImpl, ScaleSerialized> {
	override val typeName: String = "Scale"

	override val inputType: KClass<ScaleLayerImpl> = ScaleLayerImpl::class
	override val outputSerializer: SerializationStrategy<ScaleSerialized> = ScaleSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<ScaleSerialized> = ScaleSerialized.serializer()

	override fun copy(value: ScaleLayerImpl): ScaleLayerImpl {
		return ScaleLayerImpl(
			value.id,
			activation = value.activation,
			weight = value.weight.copy(), bias = value.bias.copy()
		)
	}

	override fun serialize(value: ScaleLayerImpl): ScaleSerialized {
		val activationWrap = value.activation?.let { ActivationJsonSerialized.wrap(it) }
		return ScaleSerialized(
			value.id,
			activation = activationWrap,
			weight = value.weight.serialize(), bias = value.bias.serialize()
		)
	}

	override fun deserialize(injector: Injector, value: ScaleSerialized): ScaleLayerImpl {
		val activation = value.activation?.read(injector)
		return ScaleLayerImpl(
			value.id,
			activation = activation,
			weight = value.weight.toWeightData(), bias = value.bias.toWeightData()
		)
	}
}

@Serializable
data class ScaleSerialized(
	val id: String,
	val activation: ActivationJsonSerialized?,
	val weight: WeightSerialized,
	val bias: WeightSerialized,
)