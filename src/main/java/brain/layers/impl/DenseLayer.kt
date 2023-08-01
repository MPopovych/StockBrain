package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.abs.toDim
import brain.activation.abs.ActivationFunction
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.addAssign1DToEachRow
import brain.matrix.multiplyDot
import brain.serialization.ActivationJsonSerialized
import brain.serialization.WeightSerialized
import brain.serialization.tools.Injector
import brain.suppliers.Suppliers
import brain.suppliers.ValueFiller
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Dense(
	featureDim: Int,
	private val activation: ActivationFunction? = null,
	private val kernelInit: ValueFiller = Suppliers.RandomHE,
	private val biasInit: ValueFiller = Suppliers.Zero,
	private val useBias: Boolean = true,
	uplink: () -> LayerRef,
) : LayerRef {

	private val featuresDim = featureDim.toDim()

	private val parent = uplink()
	private val parentWidth = parent.outputShape.width.requireConst()

	private val stepsDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(featureDim.toDim(), stepsDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = DenseFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val weightMatrix = Matrix.ofSupply(featuresDim, parentWidth, kernelInit)
		val biasMatrix = Matrix.ofSupply(featuresDim, Dim.Const(1), biasInit)
		val weight = WeightData("weight", weightMatrix, active = true, trainable = true)
		val bias = WeightData("bias", biasMatrix, active = useBias, trainable = useBias)
		val impl = DenseLayerImpl(name, activation = activation, weight = weight, bias = bias)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class DenseLayerImpl(
	override val id: String,
	internal val activation: ActivationFunction?,
	internal val weight: WeightData,
	internal val bias: WeightData,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = DenseFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(input: Matrix): Matrix {
		var result = input multiplyDot weight.matrix
		if (bias.active) {
			result = result addAssign1DToEachRow bias.matrix
		}
		if (activation != null) {
			result = activation.call(result)
		}
		return result
	}

	override fun weightData(): List<WeightData> = listOf(weight, bias)
}

object DenseFactory : LayerTypedFactory<DenseLayerImpl, DenseSerialized> {
	override val typeName: String = "Dense"

	override val inputType: KClass<DenseLayerImpl> = DenseLayerImpl::class
	override val outputSerializer: SerializationStrategy<DenseSerialized> = DenseSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<DenseSerialized> = DenseSerialized.serializer()

	override fun copy(value: DenseLayerImpl): DenseLayerImpl {
		return DenseLayerImpl(
			value.id,
			activation = value.activation,
			weight = value.weight.copy(), bias = value.bias.copy()
		)
	}

	override fun serialize(value: DenseLayerImpl): DenseSerialized {
		val activationWrap = value.activation?.let { ActivationJsonSerialized.wrap(it) }
		return DenseSerialized(
			value.id,
			activation = activationWrap,
			weight = value.weight.serialize(), bias = value.bias.serialize()
		)
	}

	override fun deserialize(injector: Injector, value: DenseSerialized): DenseLayerImpl {
		val activation = value.activation?.read(injector)
		return DenseLayerImpl(
			value.id,
			activation = activation,
			weight = value.weight.toWeightData(), bias = value.bias.toWeightData()
		)
	}
}

@Serializable
data class DenseSerialized(
	val id: String,
	val activation: ActivationJsonSerialized?,
	val weight: WeightSerialized,
	val bias: WeightSerialized,
)