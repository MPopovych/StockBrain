package brain.matrix

import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.MultiArray
import org.jetbrains.kotlinx.multik.ndarray.data.get

class MatrixRow(
	val row: Int,
	val width: Int,
	val array: MultiArray<Float, D1>,
) {
	fun get(index: Int): Float {
		require(index < width)
		return array[index]
	}
}