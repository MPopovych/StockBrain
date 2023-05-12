package brain.activation

import brain.matrix.Matrix

object Activations {
	val Abs = AbsFunction()
	val ReLu = ReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val Par = ParFunction()
	val ReLu6 = ReLu6Function()
	val SoftMax = SoftMaxFunction()

	val Zero = ZeroFunction() // test
	val Tanh = TanhFunction()
	val FastTanh = FastTanhFunction()
	val RevTanh = RevTanhFunction()
	val Sigmoid = SigmoidFunction()
	val FastSigmoid = FastSigmoidFunction()


	val TanhMax = TanhMaxFunction()
	val BinMax = BinMaxFunction()
	val ReverseReLu = ReverseReLuFunction()
	val ReLuMinMax = ReLuMinMaxFunction()
	val ReLuTanh = ReLuTanhFunction()
	val NegPosRange = NegPosRangeFunction()
	val BinaryZeroPos = BinaryZeroPosFunction()
	val Binary = BinaryStepFunction()
	val BinaryRange = BinaryRangeFunction()
	val SmallTanh = SmallTanhFunction()
	val NormPeakFunction = NormPeakFunction()
	val NPCap = NPCap()
	val Pit = PitFunction()

	fun activate(matrix: Matrix, buffer: Matrix, function: ActivationFunction) {
		for (y in 0 until matrix.height) {
			function.applyTo(matrix.values[y], buffer.values[y])
		}
	}

	fun deserialize(name: String?): ActivationFunction? {
		name ?: return null

		return when (name.lowercase()) {
			Abs.nameType() -> Abs
			ReLu.nameType() -> ReLu
			ReverseReLu.nameType() -> ReverseReLu
			LeReLu.nameType() -> LeReLu
			SoftMax.nameType() -> SoftMax

			TanhMax.nameType() -> TanhMax
			BinMax.nameType() -> BinMax
			Par.nameType() -> Par
			ReLu6.nameType() -> ReLu6
			NegPosRange.nameType() -> NegPosRange
			BinaryZeroPos.nameType() -> BinaryZeroPos
			Binary.nameType() -> Binary
			BinaryRange.nameType() -> BinaryRange
			Zero.nameType() -> Zero
			Tanh.nameType() -> Tanh
			FastTanh.nameType() -> FastTanh
			RevTanh.nameType() -> RevTanh
			Sigmoid.nameType() -> Sigmoid
			SmallTanh.nameType() -> SmallTanh
			FastSigmoid.nameType() -> FastSigmoid
			ReLuMinMax.nameType() -> ReLuMinMax
			ReLuTanh.nameType() -> ReLuTanh
			NormPeakFunction.nameType() -> NormPeakFunction
			Pit.nameType() -> Pit
			NPCap.nameType() -> NPCap
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