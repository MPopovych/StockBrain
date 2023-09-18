package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.abs
import kotlin.math.exp

object SiLUActivationImpl : ActivationFunction {
	override val typeName: String = "SiLU"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {

		return matrix.map { x -> (x * (1f / (1 + exp(-x)))) }
	}
}