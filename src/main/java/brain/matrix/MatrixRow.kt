package brain.matrix

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MutableMultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.toFloatArray

class MatrixRow(
	val row: Int,
	val width: Int,
	val array: MutableMultiArray<Float, D1>,
) {
	fun get(index: Int): Float {
		require(index < width)
		return array[index]
	}

	fun mapToMatrix(block: (FloatArray) -> FloatArray): Matrix {
		val data = block(array.toFloatArray())
		return Matrix.wrap(data.size, 1, data)
	}
}