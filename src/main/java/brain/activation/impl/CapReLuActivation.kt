package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.ActivationFunctionTypedFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.math.max
import kotlin.math.min
import kotlin.reflect.KClass

class CapReLuActivationImpl(internal val cap: Float) : ActivationFunction {

	override val typeName: String = LeakyReluFactory.typeName
	override val factory: ActivationFunctionFactory<*> = CapReluFactory

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { min(max(it, 0f), cap) }
	}
}

object CapReluFactory : ActivationFunctionTypedFactory<CapReLuActivationImpl, Float> {
	override val typeName: String = "CapReLu"

	override val inputType: KClass<CapReLuActivationImpl> = CapReLuActivationImpl::class
	override val outputSerializer: KSerializer<Float> = Float.serializer()

	override fun deserialize(value: Float): CapReLuActivationImpl {
		return CapReLuActivationImpl(cap = value)
	}

	override fun serialize(value: CapReLuActivationImpl): Float {
		return value.cap
	}
}