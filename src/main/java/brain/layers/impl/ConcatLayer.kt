package brain.layers.impl

import brain.abs.Dim
import brain.abs.DimShape
import brain.layers.abs.*
import brain.layers.weights.WeightData
import brain.matrix.Matrix
import brain.matrix.concat
import brain.serialization.tools.Injector
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass

class Concat(
	uplink: () -> List<LayerRef>,
) : LayerRef {

	constructor(vararg links: LayerRef) : this(uplink = { links.toList() })

	private val parents = uplink()
	override val outputShape: DimShape

	init {
		val featureDim = if (parents.all { it.outputShape.width.isConst() }) {
			Dim.Const(parents.sumOf { it.outputShape.width.requireConst().x })
		} else {
			Dim.Variable
		}
		val timeDim = if (parents.all { it.outputShape.height.isConst() }) {
			Dim.Const(parents.sumOf { it.outputShape.height.requireConst().x })
		} else {
			Dim.Variable
		}
		outputShape = DimShape(featureDim, timeDim)
	}

	override val nodeType: LayerNodeType = LayerNodeType.MultiParent(parents)
	override val factory = ConcatFactory

	override fun createInstance(name: String): LayerPropagationEnum {
		val impl = ConcatLayerImpl(name)
		return LayerPropagationEnum.MultiInput(impl)
	}
}

class ConcatLayerImpl(override val id: String) : LayerImpl.LayerMultiInput {

	companion object {
		val f = ConcatFactory.asGeneric()
	}

	override val factory = f

	override fun propagate(inputs: List<Matrix>): Matrix {
		return inputs.concat(1)
	}

	override fun weightData(): List<WeightData> = emptyList()
}

object ConcatFactory : LayerTypedFactory<ConcatLayerImpl, ConcatSerialized> {
	override val typeName: String = "Concat"

	override val inputType: KClass<ConcatLayerImpl> = ConcatLayerImpl::class
	override val outputSerializer: SerializationStrategy<ConcatSerialized> = ConcatSerialized.serializer()
	override val outputDeserializer: DeserializationStrategy<ConcatSerialized> = ConcatSerialized.serializer()

	override fun copy(value: ConcatLayerImpl): ConcatLayerImpl {
		return ConcatLayerImpl(value.id)
	}

	override fun serialize(value: ConcatLayerImpl): ConcatSerialized {
		return ConcatSerialized(value.id)
	}

	override fun deserialize(injector: Injector, value: ConcatSerialized): ConcatLayerImpl {
		return ConcatLayerImpl(value.id)
	}
}

@Serializable
data class ConcatSerialized(
	val id: String,
)