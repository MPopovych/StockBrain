package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.max
import kotlin.math.min

object HardTanhActivationImpl : ActivationFunction {

	override val typeName: String = "HardTanh"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { x -> max(min(max(x - 0.5f, min(x + 0.5f, 0f)), 2f), -2f) + x * 0.2f }
	}
}
