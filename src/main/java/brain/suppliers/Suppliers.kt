package brain.suppliers

object Suppliers {
	val Zero = ZeroSupplier
	val Ones = OnesSupplier
	val UniformHE = UniformHeSupplier
	val UniformNegPos = UniformNegPosSupplier
	val UniformZeroPos = UniformZeroPosSupplier
	val BinaryZeroPos = BinaryZeroPosSupplier
	val BinaryNegPos = BinaryNegPosSupplier

	fun const(const: Float) = ConstSupplier(const)
}

object OnesSupplier: ConstSupplier(1f)
object ZeroSupplier: ConstSupplier(0f)