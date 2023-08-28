package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.*

object SinActivationImpl : ActivationFunction {

	override val typeName: String = "Sin"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()
	private const val PI = kotlin.math.PI.toFloat()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> sin(x * PI * 10f) }
	}
}
