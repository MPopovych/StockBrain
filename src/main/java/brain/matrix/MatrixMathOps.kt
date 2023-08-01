package brain.matrix

import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.writableView
import org.jetbrains.kotlinx.multik.ndarray.operations.*


infix fun Matrix.multiplyDot(other: Matrix): Matrix {
	return Matrix(mk.linalg.dot(this.array, other.array))
}

infix fun Matrix.multiplyElement(other: Matrix): Matrix {
	return Matrix(this.array.times(other.array))
}

infix fun Matrix.multiply(other: Float): Matrix {
	return Matrix(this.array.times(other))
}

infix fun Matrix.add(other: Matrix): Matrix {
	return Matrix(this.array.plus(other.array))
}

infix fun Matrix.multiply1DToEachRow(other: Matrix): Matrix {
	require(other.height == 1)

	val flat = other.array.flatten()
	val copy = this.array.copy()
	for (i in 0 until this.height) {
		val writable = copy.writableView<Float, D2, D1>(index = i, 0)
		writable.timesAssign(flat)
	}
	return Matrix(copy)
}

infix fun Matrix.add1DToEachRow(other: Matrix): Matrix {
	require(other.height == 1)

	val flat = other.array.flatten()
	val copy = this.array.copy()
	for (i in 0 until this.height) {
		val writable = copy.writableView<Float, D2, D1>(index = i, 0)
		writable += flat
	}
	return Matrix(copy)
}

infix fun Matrix.add(other: Float): Matrix {
	return Matrix(this.array.plus(other))
}

infix fun Matrix.sub(other: Matrix): Matrix {
	return Matrix(this.array.minus(other.array))
}

infix fun Matrix.sub(other: Float): Matrix {
	return Matrix(this.array.minus(other))
}