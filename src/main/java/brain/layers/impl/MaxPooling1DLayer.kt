package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.mapRows
import brain.propagation.PropagationContext
import brain.serialization.tools.Injector
import brain.utils.roundUpInt
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class MaxPooling1D(
	private val shape1D: Int,
	uplink: () -> LayerRef,
) : LayerRef {

	private val parent = uplink()
	private val featureDim = parent.outputShape.width.mapConst {
		Dim.Const((it.x.toDouble() / shape1D).roundUpInt())
	}
	private val stepsDim: Dim = parent.outputShape.height

	override val outputShape: DimShape = DimShape(featureDim, stepsDim)
	override val nodeType: LayerNodeType = LayerNodeType.SingleParent(parent)
	override val factory = MaxPooling1DFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = MaxPooling1DLayerImpl(name, shape1D = shape1D)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class MaxPooling1DLayerImpl(
	override val id: String,
	internal val shape1D: Int,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = MaxPooling1DFactory.asGeneric()
	}

	override val factory = f

	override fun weightData(): List<WeightData> = emptyList()

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		return input.mapRows { array ->
			array.toList().chunked(shape1D).map { chunk -> chunk.max() }.toFloatArray()
		}
	}

}

object MaxPooling1DFactory : LayerTypedFactory<MaxPooling1DLayerImpl, MaxPooling1DSerialized> {
	override val typeName: String = "MaxPooling1D"

	override val inputType: KClass<MaxPooling1DLayerImpl> = MaxPooling1DLayerImpl::class
	override val outputSerializer: SerializationStrategy<MaxPooling1DSerialized> = MaxPooling1DSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<MaxPooling1DSerialized> =
		MaxPooling1DSerialized.serializer()

	override fun copy(value: MaxPooling1DLayerImpl): MaxPooling1DLayerImpl {
		return MaxPooling1DLayerImpl(
			value.id,
			shape1D = value.shape1D
		)
	}

	override fun serialize(value: MaxPooling1DLayerImpl): MaxPooling1DSerialized {
		return MaxPooling1DSerialized(
			value.id,
			shape1D = value.shape1D
		)
	}

	override fun deserialize(injector: Injector, value: MaxPooling1DSerialized): MaxPooling1DLayerImpl {
		return MaxPooling1DLayerImpl(
			value.id,
			shape1D = value.shape1D
		)
	}
}

@Serializable
data class MaxPooling1DSerialized(
	val id: String,
	val shape1D: Int,
)