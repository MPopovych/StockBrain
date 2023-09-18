package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.assignAddBroadcast
import brain.matrix.multiplyBroadcast
import brain.propagation.PropagationContext
import brain.serialization.WeightSerialized
import brain.serialization.tools.Injector
import brain.suppliers.Suppliers
import brain.suppliers.ValueSupplier
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class GlobalNormLayer(
	private val kernelInit: ValueSupplier = Suppliers.BinaryNegPos,
	private val biasInit: ValueSupplier = Suppliers.Zero,
	private val useWeight: Boolean = true,
	private val useBias: Boolean = true,
	uplink: () -> LayerRef,
) : LayerRef {

	private val parent = uplink()
	private val parentWidth = parent.outputShape.width.requireConst()
	private val timeDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(parentWidth, timeDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = GlobalNormFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val weightMatrix = Matrix.ofSupply(parentWidth, Dim.Const(1), kernelInit)
		val biasMatrix = Matrix.ofSupply(parentWidth, Dim.Const(1), biasInit)
		val weight = WeightData("weight", weightMatrix, active = useWeight, trainable = useWeight)
		val bias = WeightData("bias", biasMatrix, active = useBias, trainable = useBias)
		val impl = GlobalNormLayerImpl(name, weight = weight, bias = bias)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class GlobalNormLayerImpl(
	override val id: String,
	internal val weight: WeightData,
	internal val bias: WeightData,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = GlobalNormFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		var result = input multiplyBroadcast weight.matrix
		result = result assignAddBroadcast bias.matrix
		return result
	}

	override fun weightData(): List<WeightData> = listOf(weight, bias)
}

object GlobalNormFactory : LayerTypedFactory<GlobalNormLayerImpl, GlobalNormSerialized> {
	override val typeName: String = "GlobalNorm"

	override val inputType: KClass<GlobalNormLayerImpl> = GlobalNormLayerImpl::class
	override val outputSerializer: SerializationStrategy<GlobalNormSerialized> = GlobalNormSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<GlobalNormSerialized> = GlobalNormSerialized.serializer()

	override fun copy(value: GlobalNormLayerImpl): GlobalNormLayerImpl {
		return GlobalNormLayerImpl(
			value.id,
			weight = value.weight.copy(),
			bias = value.bias.copy()
		)
	}

	override fun serialize(value: GlobalNormLayerImpl): GlobalNormSerialized {
		return GlobalNormSerialized(
			value.id,
			weight = value.weight.serialize(), bias = value.bias.serialize()
		)
	}

	override fun deserialize(injector: Injector, value: GlobalNormSerialized): GlobalNormLayerImpl {
		return GlobalNormLayerImpl(
			value.id,
			weight = value.weight.toWeightData(), bias = value.bias.toWeightData()
		)
	}
}

@Serializable
data class GlobalNormSerialized(
	val id: String,
	val weight: WeightSerialized,
	val bias: WeightSerialized,
)