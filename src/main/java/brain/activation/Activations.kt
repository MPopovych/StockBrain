package brain.activation

import brain.matrix.Matrix

object Activations {
	val Abs = AbsFunction()
	val ReLu = ReLuFunction()
	val ReverseReLu = ReverseReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val ReLuMinMax = ReLuMinMaxFunction()
	val NegZeroPos = NegZeroPosFunction()
	val BinaryNegPos = BinaryNegPosFunction()
	val Binary = BinaryStepFunction()
	val Zero = ZeroFunction() // test
	val Tanh = TanhFunction()
	val FastTanh = FastTanhFunction()
	val Sigmoid = SigmoidFunction()
	val HardSigmoid = HardSigmoidFunction()
	val MirrorReLu = MirrorReLuFunction()
	val MirrorReversedReLu = MirrorReLuReversedFunction()

	fun activate(matrix: Matrix, buffer: Matrix, function: ActivationFunction) {
		for (y in 0 until matrix.height) {
			for (x in 0 until matrix.width) {
				buffer.values[y][x] = function.apply(matrix.values[y][x])
			}
		}
	}

	fun deserialize(name: String?): ActivationFunction? {
		name ?: return null

		return when (name.lowercase()) {
			Abs.nameType() -> Abs
			ReLu.nameType() -> ReLu
			ReverseReLu.nameType() -> ReverseReLu
			LeReLu.nameType() -> LeReLu
			NegZeroPos.nameType() -> NegZeroPos
			BinaryNegPos.nameType() -> BinaryNegPos
			Binary.nameType() -> Binary
			Zero.nameType() -> Zero
			Tanh.nameType() -> Tanh
			FastTanh.nameType() -> FastTanh
			Sigmoid.nameType() -> Sigmoid
			ReLuMinMax.nameType() -> ReLuMinMax
			MirrorReLu.nameType() -> MirrorReLu
			MirrorReversedReLu.nameType() -> MirrorReversedReLu
			else -> throw IllegalArgumentException("unsupported type: $name")
		}
	}

	fun serialize(function: ActivationFunction): String {
		return function.nameType()
	}
}

fun ActivationFunction?.applyFromMatrixTo(matrix: Matrix, buffer: Matrix) {
	this ?: return
	Activations.activate(matrix, buffer, this)
}

fun ActivationFunction.nameType() = this.javaClass.name.lowercase()