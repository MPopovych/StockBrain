package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.add
import brain.matrix.concat
import brain.matrix.multiply
import brain.propagation.PropagationContext
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

// Multiplicative Additive
class Additive(
	uplink: () -> List<LayerRef>,
) : LayerRef {

	constructor(vararg links: LayerRef) : this(uplink = { links.toList() })

	private val parents = uplink()
	override val outputShape: DimShape

	init {
		val firstFeature = parents[0].outputShape.width
		val featureDim = if (parents.all { it.outputShape.width == firstFeature }) {
			firstFeature.requireConst()
		} else {
			Dim.Variable
		}
		val firstTime = parents[0].outputShape.height
		val timeDim = if (parents.all { it.outputShape.height == firstTime }) {
			firstTime
		} else {
			Dim.Variable
		}
		outputShape = DimShape(featureDim, timeDim)
	}

	override val nodeType: LayerNodeType = LayerNodeType.MultiParent(parents)
	override val factory = AdditiveFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = AdditiveLayerImpl(name)
		return LayerPropagationEnum.MultiInput(impl)
	}
}

class AdditiveLayerImpl(override val id: String) : LayerImpl.LayerMultiInput {

	companion object {
		val f = AdditiveFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(inputs: List<Matrix>, propagationContext: PropagationContext?): Matrix {
		var initial = inputs.first()
		for (next in inputs.drop(1)) {
			initial = initial.add(next)
		}
		return initial
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object AdditiveFactory : LayerTypedFactory<AdditiveLayerImpl, AdditiveSerialized> {
	override val typeName: String = "Additive"

	override val inputType: KClass<AdditiveLayerImpl> = AdditiveLayerImpl::class
	override val outputSerializer: SerializationStrategy<AdditiveSerialized> = AdditiveSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<AdditiveSerialized> = AdditiveSerialized.serializer()

	override fun copy(value: AdditiveLayerImpl): AdditiveLayerImpl {
		return AdditiveLayerImpl(value.id)
	}

	override fun serialize(value: AdditiveLayerImpl): AdditiveSerialized {
		return AdditiveSerialized(value.id)
	}

	override fun deserialize(injector: Injector, value: AdditiveSerialized): AdditiveLayerImpl {
		return AdditiveLayerImpl(value.id)
	}
}

@Serializable
data class AdditiveSerialized(
	val id: String,
)