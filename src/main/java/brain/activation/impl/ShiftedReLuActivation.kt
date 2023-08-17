package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.max

object ShiftedReLuActivationImpl : ActivationFunction {

	override val typeName: String = "ShiftedReLu"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { max(it, 0f) + 0.5f }
	}
}