package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.max

object ReLuActivationImpl : ActivationFunction {

	override val typeName: String = "ReLu"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { max(it, 0f) }
	}
}

//object ReluFactory : ActivationTypedFactory<ReLuImpl, Boolean> {
//	override val inputType: KClass<ReLuImpl> = ReLuImpl::class
//	override val outputSerializer: KSerializer<Boolean> = Boolean.serializer()
//
//	override fun deserialize(value: Boolean): ReLuImpl {
//		return ReLuImpl
//	}
//
//	override fun serialize(value: ReLuImpl): Boolean {
//		return false
//	}
//}