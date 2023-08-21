package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.abs

object SigmoidActivationImpl : ActivationFunction {

	override val typeName: String = "Sigmoid"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()
	private const val alpha = 2f


	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> (x * alpha / (1f + abs(x * alpha))) + 0.5f }
	}
}