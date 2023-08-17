package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.concat
import brain.matrix.multiply
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

// Multiplicative attention
class Attention(
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
	override val factory = AttentionFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = AttentionLayerImpl(name)
		return LayerPropagationEnum.MultiInput(impl)
	}
}

class AttentionLayerImpl(override val id: String) : LayerImpl.LayerMultiInput {

	companion object {
		val f = AttentionFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(inputs: List<Matrix>): Matrix {
		var initial = inputs.first()
		for (next in inputs.drop(1)) {
			initial = initial.multiply(next)
		}
		return initial
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object AttentionFactory : LayerTypedFactory<AttentionLayerImpl, AttentionSerialized> {
	override val typeName: String = "Attention"

	override val inputType: KClass<AttentionLayerImpl> = AttentionLayerImpl::class
	override val outputSerializer: SerializationStrategy<AttentionSerialized> = AttentionSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<AttentionSerialized> = AttentionSerialized.serializer()

	override fun copy(value: AttentionLayerImpl): AttentionLayerImpl {
		return AttentionLayerImpl(value.id)
	}

	override fun serialize(value: AttentionLayerImpl): AttentionSerialized {
		return AttentionSerialized(value.id)
	}

	override fun deserialize(injector: Injector, value: AttentionSerialized): AttentionLayerImpl {
		return AttentionLayerImpl(value.id)
	}
}

@Serializable
data class AttentionSerialized(
	val id: String,
)