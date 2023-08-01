package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.ActivationFunctionTypedFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlin.math.max
import kotlin.reflect.KClass

class LeakyReLuActivationImpl(internal val e: Float) : ActivationFunction {

	override val typeName: String = LeakyReluFactory.typeName
	override val factory: ActivationFunctionFactory<*> = LeakyReluFactory

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { max(it, it * e) }
	}
}

object LeakyReluFactory : ActivationFunctionTypedFactory<LeakyReLuActivationImpl, Float> {
	override val typeName: String = "LeakyReLu"

	override val inputType: KClass<LeakyReLuActivationImpl> = LeakyReLuActivationImpl::class
	override val outputSerializer: KSerializer<Float> = Float.serializer()

	override fun deserialize(value: Float): LeakyReLuActivationImpl {
		return LeakyReLuActivationImpl(e = value)
	}

	override fun serialize(value: LeakyReLuActivationImpl): Float {
		return value.e
	}
}