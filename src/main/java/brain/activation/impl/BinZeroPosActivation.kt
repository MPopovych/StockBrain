package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.Matrix
import brain.matrix.map
import kotlin.math.abs

object BinZeroPosActivationImpl : ActivationFunction {

	override val typeName: String = "BinZeroPos"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		return matrix.map { if (it > 0) 1f else 0f }
	}
}
