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

class AbsCapActivationImpl(internal val cap: Float) : ActivationFunction {

	override val typeName: String = AbsCapFactory.typeName
	override val factory: ActivationFunctionFactory<*> = AbsCapFactory

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { max(min(it, cap), -cap) }
	}
}

object AbsCapFactory : ActivationFunctionTypedFactory<AbsCapActivationImpl, Float> {
	override val typeName: String = "AbsCap"

	override val inputType: KClass<AbsCapActivationImpl> = AbsCapActivationImpl::class
	override val outputSerializer: KSerializer<Float> = Float.serializer()

	override fun deserialize(value: Float): AbsCapActivationImpl {
		return AbsCapActivationImpl(cap = value)
	}

	override fun serialize(value: AbsCapActivationImpl): Float {
		return value.cap
	}
}