package brain.activation.impl

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.objectFactory
import brain.matrix.*
import org.jetbrains.kotlinx.multik.api.math.cumSum
import org.jetbrains.kotlinx.multik.ndarray.data.get
import kotlin.math.exp
import kotlin.math.max

object SoftMaxActivationImpl : ActivationFunction {

	override val typeName: String = "SoftMax"
	override val factory: ActivationFunctionFactory<*> = this.objectFactory()

	override fun call(matrix: Matrix): Matrix {
		val expArray = matrix.exp()
		val sumArray = expArray.sumHorizontal()
		return expArray.mapWithPos {  (_, y, v)->
			v / sumArray[0, y]
		}
	}
}