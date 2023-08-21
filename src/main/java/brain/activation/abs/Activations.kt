package brain.activation.abs

import brain.activation.impl.*

object Activations {
	val ReLu = ReLuActivationImpl
	val CapLeReLu = CapLeReLuActivationImpl(cap = 6f, e = 0.1f)
	val ShiftedReLu = ShiftedReLuActivationImpl
	val Tanh = TanhActivationImpl
	val Sigmoid = SigmoidActivationImpl
	val FT = FTActivationImpl
	val HardTanh = HardTanhActivationImpl
	val Softmax = SoftMaxActivationImpl
	val LeakyReLu = LeakyReLu(e = 0.1f)
	val AbsCap = AbsCap(cap = 2f)
	val Abs = AbsActivationImpl
	val Zero = ZeroActivationImpl
	val Test = TestActivationImpl
	val BinZP = BinZeroPosActivationImpl

	fun CapLeReLu(cap: Float, e: Float) = CapLeReLuActivationImpl(cap = cap, e = e)
	fun LeakyReLu(e: Float) = LeakyReLuActivationImpl(e = e)
	fun AbsCap(cap: Float) = AbsCapActivationImpl(cap = cap)
}