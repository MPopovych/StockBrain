package brain.matrix

import brain.abs.Dim
import brain.abs.Shape
import brain.suppliers.ValueSupplier
import brain.utils.encodeToBase64
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.ndarray.data.D1
import org.jetbrains.kotlinx.multik.ndarray.data.D2
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array
import org.jetbrains.kotlinx.multik.ndarray.data.writableView
import java.nio.ByteBuffer
import java.util.*

typealias ND = D2Array<Float>

class Matrix internal constructor(
	internal val array: ND,
) {
	val width: Int
		get() = array.shape[1] // dim 2
	val height: Int
		get() = array.shape[0] // dim 1

	fun shape() = Shape(width, height)

	fun copy() = Matrix(array.copy())

	fun accessMutableArray() = array.data.getFloatArray()

	fun row(n: Int): MatrixRow {
		return MatrixRow(n, width, array.writableView<Float, D2, D1>(n, 0))
	}

	companion object {
		fun fromEncoded(width: Int, height: Int, data: String): Matrix {
			val bytes = Base64.getDecoder().decode(data)
			val buff = ByteBuffer.wrap(bytes)
			val expectedCount = width * height
			val expectedBytes = expectedCount * 4
			check(bytes.size == expectedBytes) {
				"Mismatch of ${bytes.size} vs ${expectedBytes}, w:${width}, h:${height}"
			}
			val floatArray = FloatArray(expectedCount)
			var i = 0
			while (buff.hasRemaining()) {
				floatArray[i++] = buff.getFloat()
			}
			check(i == floatArray.size) { "Not a full write, i ${i}, size: ${floatArray.size}" }
			return wrap(width, height, floatArray)
		}

		fun wrap(width: Int, height: Int, array: FloatArray): Matrix {
			require(height > 0 && width > 0 && height * width == array.size)
			return Matrix(mk.ndarray(array, dim1 = height, dim2 = width))
		}

		fun wrapMD(width: Int, height: Int, array: Array<FloatArray>): Matrix {
			require(height > 0 && height == array.size)
			require(width == array[0].size)
			return Matrix(mk.ndarray(array))
		}

		fun zeroes(width: Int, height: Int): Matrix {
			return Matrix(mk.zeros<Float>(dim1 = height, dim2 = width))
		}

		fun ofSupply(width: Int, height: Int, supply: ValueSupplier): Matrix {
			return wrap(width, height, supply.create(width * height))
		}

		fun ofSupply(width: Dim.Const, height: Dim.Const, supply: ValueSupplier): Matrix {
			return wrap(width.x, height.x, supply.create(width.x * height.x))
		}

		fun ofLambda(width: Int, height: Int, block: (x: Int, y: Int, c: Int) -> Float): Matrix {
			val size = width * height
			val array = FloatArray(size) {
				val x = it % width
				val y = (it - x) / width
				block(x, y, it)
			}
			return wrap(width, height, array)
		}
	}

	fun readStringData(): String {
		return accessMutableArray().encodeToBase64()
	}

	fun readFloatData(): FloatArray {
		return this.array.data.getFloatArray().copyOf()
	}

	fun writeFloatData(data: FloatArray) {
		val innerArray = this.accessMutableArray()
		System.arraycopy(data, 0, innerArray, 0, data.size)
	}
}