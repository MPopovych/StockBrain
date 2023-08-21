package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.*

object FTActivationImpl : ActivationFunction {

	override val typeName: String = "FT"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> max(-1.5f + x, min(1.5f + x, x * x * x)) }
	}
}