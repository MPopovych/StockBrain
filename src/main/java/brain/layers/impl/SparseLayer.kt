package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.abs.toDim
import brain.activation.abs.ActivationFunction
import brain.activation.abs.Activations
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.assignAddBroadcast
import brain.matrix.dot
import brain.matrix.multiply
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

class Sparse(
	featureDim: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueSupplier = Suppliers.UniformHE,
	private val maskInit: ValueSupplier = Suppliers.UniformHE,
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
	override val factory = SparseFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val weightMatrix = Matrix.ofSupply(featuresDim, parentWidth, kernelInit)
		val maskMatrix = Matrix.ofSupply(featuresDim, parentWidth, maskInit)
		val biasMatrix = Matrix.ofSupply(featuresDim, Dim.Const(1), biasInit)
		val weight = WeightData("weight", weightMatrix, active = true, trainable = true)
		val mask = WeightData("mask", maskMatrix, active = true, trainable = true)
		val bias = WeightData("bias", biasMatrix, active = useBias, trainable = useBias)
		val impl = SparseLayerImpl(name, activation = activation, weight = weight, bias = bias, mask = mask)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class SparseLayerImpl(
	override val id: String,
	internal val activation: ActivationFunction?,
	internal val weight: WeightData,
	internal val mask: WeightData,
	internal val bias: WeightData,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = SparseFactory.asGeneric()
	}

	override val factory = f

	private var weightActivated: Matrix? = null

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		var result = input dot (weightActivated ?: throw IllegalStateException("Inactive mask"))
		if (bias.active) {
			result = result assignAddBroadcast bias.matrix
		}
		if (activation != null) {
			result = activation.call(result)
		}
		return result
	}

	override fun onWeightUpdated() {
		weightActivated = weight.matrix multiply Activations.BinZP.call(mask.matrix)
	}

	override fun weightData(): List<WeightData> = listOf(weight, mask, bias)
}

object SparseFactory : LayerTypedFactory<SparseLayerImpl, SparseSerialized> {
	override val typeName: String = "Sparse"

	override val inputType: KClass<SparseLayerImpl> = SparseLayerImpl::class
	override val outputSerializer: SerializationStrategy<SparseSerialized> = SparseSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<SparseSerialized> = SparseSerialized.serializer()

	override fun copy(value: SparseLayerImpl): SparseLayerImpl {
		return SparseLayerImpl(
			value.id,
			activation = value.activation,
			weight = value.weight.copy(), mask = value.mask.copy(), bias = value.bias.copy()
		)
	}

	override fun serialize(value: SparseLayerImpl): SparseSerialized {
		val activationWrap = value.activation?.let { ActivationJsonSerialized.wrap(it) }
		return SparseSerialized(
			value.id,
			activation = activationWrap,
			weight = value.weight.serialize(),
			mask = value.mask.serialize(),
			bias = value.bias.serialize(),
		)
	}

	override fun deserialize(injector: Injector, value: SparseSerialized): SparseLayerImpl {
		val activation = value.activation?.read(injector)
		return SparseLayerImpl(
			value.id,
			activation = activation,
			weight = value.weight.toWeightData(),
			mask = value.mask.toWeightData(),
			bias = value.bias.toWeightData()
		)
	}
}

@Serializable
data class SparseSerialized(
	val id: String,
	val activation: ActivationJsonSerialized?,
	val weight: WeightSerialized,
	val mask: WeightSerialized,
	val bias: WeightSerialized,
)