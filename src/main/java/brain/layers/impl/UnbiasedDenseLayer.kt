package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.abs.toDim
import brain.activation.abs.ActivationFunction
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.*
import brain.matrix.assignAddBroadcast
import brain.propagation.PropagationContext
import brain.serialization.ActivationJsonSerialized
import brain.serialization.WeightSerialized
import brain.serialization.tools.Injector
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class UnBiasedDense(
	featureDim: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = Suppliers.UniformHE,
	private val biasInit: ValueSupplier = Suppliers.Zero,
	private val useBias: Boolean = true,
	uplink: () -> LayerRef,
) : LayerRef {

	private val featuresDim = featureDim.toDim()

	private val parent = uplink()
	private val parentWidth = parent.outputShape.width.requireConst()

	private val stepsDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(featureDim.toDim(), stepsDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = UnBiasedDenseFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val weightMatrix = Matrix.ofSupply(featuresDim, parentWidth, kernelInit)
		val biasMatrix = Matrix.ofSupply(featuresDim, Dim.Const(1), biasInit)
		val weight = WeightData("weight", weightMatrix, active = true, trainable = true)
		val bias = WeightData("bias", biasMatrix, active = useBias, trainable = useBias)
		val impl = UnBiasedDenseLayerImpl(name, activation = activation, weight = weight, bias = bias)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class UnBiasedDenseLayerImpl(
	override val id: String,
	internal val activation: ActivationFunction?,
	internal val weight: WeightData,
	internal val bias: WeightData,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = UnBiasedDenseFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		var result = input dot weight.matrix
		if (bias.active) {
			result = result assignAddBroadcast bias.matrix
		}
		if (activation != null) {
			result = activation.call(result)
		}
		if (bias.active) {
			result = result assignSubBroadcast bias.matrix
		}
		return result
	}

	override fun weightData(): List<WeightData> = listOf(weight, bias)
}

object UnBiasedDenseFactory : LayerTypedFactory<UnBiasedDenseLayerImpl, DenseSerialized> {
	override val typeName: String = "UnBiasedDense"

	override val inputType: KClass<UnBiasedDenseLayerImpl> = UnBiasedDenseLayerImpl::class
	override val outputSerializer: SerializationStrategy<DenseSerialized> = DenseSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<DenseSerialized> = DenseSerialized.serializer()

	override fun copy(value: UnBiasedDenseLayerImpl): UnBiasedDenseLayerImpl {
		return UnBiasedDenseLayerImpl(
			value.id,
			activation = value.activation,
			weight = value.weight.copy(), bias = value.bias.copy()
		)
	}

	override fun serialize(value: UnBiasedDenseLayerImpl): DenseSerialized {
		val activationWrap = value.activation?.let { ActivationJsonSerialized.wrap(it) }
		return DenseSerialized(
			value.id,
			activation = activationWrap,
			weight = value.weight.serialize(), bias = value.bias.serialize()
		)
	}

	override fun deserialize(injector: Injector, value: DenseSerialized): UnBiasedDenseLayerImpl {
		val activation = value.activation?.read(injector)
		return UnBiasedDenseLayerImpl(
			value.id,
			activation = activation,
			weight = value.weight.toWeightData(), bias = value.bias.toWeightData()
		)
	}
}