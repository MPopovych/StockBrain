package brain.activation

import brain.matrix.Matrix

object Activations {
	val Abs = AbsFunction()
	val ReLu = ReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val Par = ParFunction()
	val ReLu6 = ReLu6Function()

	val Zero = ZeroFunction() // test
	val Tanh = TanhFunction()
	val FastTanh = FastTanhFunction()
	val RevTanh = RevTanhFunction()
	val Sigmoid = SigmoidFunction()
	val HardSigmoid = HardSigmoidFunction()

	val ReverseReLu = ReverseReLuFunction()
	val ReLuMinMax = ReLuMinMaxFunction()
	val ReLuTanh = ReLuTanhFunction()
	val NegPosRange = NegPosRangeFunction()
	val BinaryNegPos = BinaryNegPosFunction()
	val Binary = BinaryStepFunction()
	val BinaryRange = BinaryRangeFunction()
	val SmallTanh = SmallTanhFunction()
	val NormPeakFunction = NormPeakFunction()
	val NPCap = NPCap()
	val Pit = PitFunction()

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
			Par.nameType() -> Par
			ReLu6.nameType() -> ReLu6
			NegPosRange.nameType() -> NegPosRange
			BinaryNegPos.nameType() -> BinaryNegPos
			Binary.nameType() -> Binary
			BinaryRange.nameType() -> BinaryRange
			Zero.nameType() -> Zero
			Tanh.nameType() -> Tanh
			FastTanh.nameType() -> FastTanh
			RevTanh.nameType() -> RevTanh
			Sigmoid.nameType() -> Sigmoid
			SmallTanh.nameType() -> SmallTanh
			HardSigmoid.nameType() -> HardSigmoid
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

fun ActivationFunction?.applyFromMatrixTo(matrix: Matrix, buffer: Matrix) {
	this ?: return
	Activations.activate(matrix, buffer, this)
}

fun ActivationFunction.nameType() = this.javaClass.name.lowercase()