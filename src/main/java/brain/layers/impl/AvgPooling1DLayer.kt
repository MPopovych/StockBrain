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
import utils.ext.average
import kotlin.reflect.KClass

class AvgPooling1D(
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
	override val factory = AvgPooling1DFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = AvgPooling1DLayerImpl(name, shape1D = shape1D)
		return LayerPropagationEnum.SingleInput(impl)
	}
}

class AvgPooling1DLayerImpl(
	override val id: String,
	internal val shape1D: Int,
) : LayerImpl.LayerSingleInput {

	companion object {
		val f = AvgPooling1DFactory.asGeneric()
	}

	override val factory = f

	override fun weightData(): List<WeightData> = emptyList()

	override fun propagate(input: Matrix, propagationContext: PropagationContext?): Matrix {
		return input.mapRows { array ->
			array.toList().chunked(shape1D).map { chunk -> chunk.average() }.toFloatArray()
		}
	}

}

object AvgPooling1DFactory : LayerTypedFactory<AvgPooling1DLayerImpl, AvgPooling1DSerialized> {
	override val typeName: String = "AvgPooling1D"

	override val inputType: KClass<AvgPooling1DLayerImpl> = AvgPooling1DLayerImpl::class
	override val outputSerializer: SerializationStrategy<AvgPooling1DSerialized> = AvgPooling1DSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<AvgPooling1DSerialized> =
		AvgPooling1DSerialized.serializer()

	override fun copy(value: AvgPooling1DLayerImpl): AvgPooling1DLayerImpl {
		return AvgPooling1DLayerImpl(
			value.id,
			shape1D = value.shape1D
		)
	}

	override fun serialize(value: AvgPooling1DLayerImpl): AvgPooling1DSerialized {
		return AvgPooling1DSerialized(
			value.id,
			shape1D = value.shape1D
		)
	}

	override fun deserialize(injector: Injector, value: AvgPooling1DSerialized): AvgPooling1DLayerImpl {
		return AvgPooling1DLayerImpl(
			value.id,
			shape1D = value.shape1D
		)
	}
}

@Serializable
data class AvgPooling1DSerialized(
	val id: String,
	val shape1D: Int,
)