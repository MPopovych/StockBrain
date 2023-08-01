package brain.suppliers

object Suppliers {
	val Zero = ZeroSupplier.INSTANCE
	val Ones = OnesSupplier.INSTANCE
	val RandomZP = RandomSupplier.INSTANCE
	val RandomBinZP = RandomBinaryZP.INSTANCE
	val RandomBinNP = RandomBinaryNP.INSTANCE
	val RandomHE = HESupplier.INSTANCE
	val RandomM = MSupplier.INSTANCE
	val RandomRangeNP = RandomRangeSupplier.INSTANCE

	fun const(const: Float) = ConstSupplier(const)
}