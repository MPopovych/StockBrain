package suppliers

import layers.LayerShape
import matrix.Matrix

object Suppliers {
	fun createMatrix(shape: LayerShape, supplier: ValueSupplier): Matrix {
		return Matrix(shape.width, shape.height).also {
			for (x in 0 until it.width) {
				for (y in 0 until it.height) {
					it.values[x][y] = supplier.supply(x, y)
				}
			}
		}
	}

	fun fillFull(matrix: Matrix, supplier: ValueSupplier) {
		for (x in 0 until matrix.width) {
			for (y in 0 until matrix.height) {
				matrix.values[x][y] = supplier.supply(x, y)
			}
		}
	}
}