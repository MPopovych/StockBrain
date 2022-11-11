package brain.activation

import brain.matrix.Matrix

object Activations {
	val ReLu = ReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val NegZeroPos = NegZeroPosFunction()
	val BinaryNegPos = BinaryNegPosFunction()
	val Binary = BinaryStepFunction()
	val Zero = ZeroFunction() // test
	val Tanh = TanhFunction()

	fun activate(matrix: Matrix, buffer: Matrix, function: ActivationFunction) {
		for (x in 0 until matrix.width) {
			for (y in 0 until matrix.height) {
				buffer.values[x][y] = function.apply(matrix.values[x][y])
			}
		}
	}

	fun deserialize(name: String?): ActivationFunction? {
		name ?: return null

		return when (name.lowercase()) {
			ReLu.nameType() -> ReLu
			LeReLu.nameType() -> LeReLu
			NegZeroPos.nameType() -> NegZeroPos
			BinaryNegPos.nameType() -> BinaryNegPos
			Binary.nameType() -> Binary
			Zero.nameType() -> Zero
			Tanh.nameType() -> Tanh
			else -> throw IllegalArgumentException("unsupported type: $name")
		}
	}

	fun serialize(function: ActivationFunction): String {
		return function.nameType()
	}
}

fun ActivationFunction.applyFromMatrixTo(matrix: Matrix, buffer: Matrix) {
	Activations.activate(matrix, buffer, this)
}

fun ActivationFunction.nameType() = this.javaClass.name.lowercase()