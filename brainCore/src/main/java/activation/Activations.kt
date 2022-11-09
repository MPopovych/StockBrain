package activation

import matrix.Matrix

object Activations {
	val ReLu = ReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val NegPos = NegPosFunction()
	val NegZeroPos = NegZeroPosFunction()
	val Binary = BinaryStepFunction()
	val Zero = ZeroFunction()

	fun activate(matrix: Matrix, buffer: Matrix, function: ActivationFunction) {
		for (x in 0 until matrix.width) {
			for (y in 0 until matrix.height) {
				buffer.values[x][y] = function.apply(matrix.values[x][y])
			}
		}
	}
}

fun ActivationFunction.applyFromMatrixTo(matrix: Matrix, buffer: Matrix) {
	Activations.activate(matrix, buffer, this)
}