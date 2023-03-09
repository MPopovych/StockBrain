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

	fun createMatrix(shape: LayerShape, supplier: ValueFiller): Matrix {
		return Matrix(shape.width, shape.height).also {
			for (y in 0 until it.height) {
				supplier.fill(it.values[y])
			}
		}
	}

	fun fillFull(matrix: Matrix, supplier: ValueFiller) {
		for (y in 0 until matrix.height) {
			for (x in 0 until matrix.width) {
				supplier.fill(matrix.values[y])
			}
		}
	}
}