package activation

import com.google.gson.GsonBuilder
import matrix.Matrix
import utils.fromJson

object Activations {
	val ReLu = ReLuFunction()
	val LeReLu = LeakyReLuFunction()
	val NegZeroPos = NegZeroPosFunction()
	val BinaryNegPos = BinaryNegPosFunction()
	val Binary = BinaryStepFunction()
	val Zero = ZeroFunction() // test

	private val gson by lazy { GsonBuilder().create() }

	fun activate(matrix: Matrix, buffer: Matrix, function: ActivationFunction) {
		for (x in 0 until matrix.width) {
			for (y in 0 until matrix.height) {
				buffer.values[x][y] = function.apply(matrix.values[x][y])
			}
		}
	}

	fun deserialize(json: String?): ActivationFunction? {
		json ?: return null

		val parsed: BasicSerializedFunction = gson.fromJson(json)

		return when (parsed.type.lowercase()) {
			ReLu.nameType() -> ReLu
			LeReLu.nameType() -> LeReLu
			NegZeroPos.nameType() -> NegZeroPos
			BinaryNegPos.nameType() -> BinaryNegPos
			Binary.nameType() -> Binary
			Zero.nameType() -> Zero
			else -> throw IllegalArgumentException("unsupported type or json: $json")
		}
	}

	fun serialize(function: ActivationFunction?): String? {
		function ?: return null

		val type = function.nameType()
		return gson.toJson(BasicSerializedFunction(type))
	}
}

data class BasicSerializedFunction(
	val type: String,
)

fun ActivationFunction.applyFromMatrixTo(matrix: Matrix, buffer: Matrix) {
	Activations.activate(matrix, buffer, this)
}

fun ActivationFunction.nameType() = this.javaClass.name.lowercase()