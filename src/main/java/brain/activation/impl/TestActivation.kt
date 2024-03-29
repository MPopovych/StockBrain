package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

object TestActivationImpl : ActivationFunction {

	override val typeName: String = "Test"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> cos(x) }
	}
}
