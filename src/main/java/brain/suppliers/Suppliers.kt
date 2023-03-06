package brain.suppliers

import brain.layers.LayerShape
import brain.matrix.Matrix

object Suppliers {
	val Zero = ZeroSupplier.INSTANCE
	val Ones = OnesSupplier.INSTANCE
	val RandomZP = RandomSupplier.INSTANCE
	val RandomBinZP = RandomBinaryZP.INSTANCE
	val RandomBinNP = RandomBinaryNP.INSTANCE
	val RandomHE = HESupplier.INSTANCE
	val RandomRangeNP = RandomRangeSupplier.INSTANCE

	fun const(const: Float) = ConstSupplier(const)

	fun createMatrix(shape: LayerShape, supplier: ValueSupplier): Matrix {
		return Matrix(shape.width, shape.height).also {
			for (y in 0 until it.height) {
				for (x in 0 until it.width) {

					it.values[y][x] = supplier.supply(shape.width * shape.height, x, y)
				}
			}
		}
	}

	fun fillFull(matrix: Matrix, supplier: ValueSupplier) {
		for (y in 0 until matrix.height) {
			for (x in 0 until matrix.width) {
				matrix.values[y][x] = supplier.supply(matrix.width * matrix.width, x, y)
			}
		}
	}
}