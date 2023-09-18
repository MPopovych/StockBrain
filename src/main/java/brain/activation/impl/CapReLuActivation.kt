package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.ActivationFunctionTypedFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CapLeReLuActivationImpl(internal val cap: Float, internal val e: Float) : ActivationFunction {

	override val typeName: String = LeakyReluFactory.typeName
	override val factory: ActivationFunctionFactory<*> = CapReluFactory

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { min(max(it, it * e), cap) }
	}
}

object CapReluFactory : ActivationFunctionTypedFactory<CapLeReLuActivationImpl, CapLeReluSerialized> {
	override val typeName: String = "CapReLu"

	override val inputType: KClass<CapLeReLuActivationImpl> = CapLeReLuActivationImpl::class
	override val outputSerializer: KSerializer<CapLeReluSerialized> = CapLeReluSerialized.serializer()

	override fun deserialize(value: CapLeReluSerialized): CapLeReLuActivationImpl {
		return CapLeReLuActivationImpl(cap = value.cap, e = value.e)
	}

	override fun serialize(value: CapLeReLuActivationImpl): CapLeReluSerialized {
		return CapLeReluSerialized(cap = value.cap, e = value.e)
	}
}

@Serializable
data class CapLeReluSerialized(
	val cap: Float,
	val e: Float,
)
