package brain.activation.abs

import brain.activation.impl.*

object Activations {
	val ReLu = ReLuActivationImpl
	val Tanh = TanhActivationImpl
	val LeakyReLu = LeakyReLu(e = 0.1f)
	val AbsCap = AbsCap(cap = 2f)
	val Abs = AbsActivationImpl
	val Zero = ZeroActivationImpl
	val Test = TestActivationImpl

	fun LeakyReLu(e: Float) = LeakyReLuActivationImpl(e = e)
	fun AbsCap(cap: Float) = AbsCapActivationImpl(cap = cap)
}