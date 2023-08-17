package brain.activation.impl

import brain.activation.FastTanhFunction
import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map

object TanhActivationImpl : ActivationFunction {

	override val typeName: String = "Tanh"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()
	private val impl = FastTanhFunction()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> impl.apply(x * 2.5f) }
	}
}