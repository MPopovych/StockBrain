package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map

object ZeroActivationImpl : ActivationFunction {

	override val typeName: String = "Zero"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { 0f }
	}
}
