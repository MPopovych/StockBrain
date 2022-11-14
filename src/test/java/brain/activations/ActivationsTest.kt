package brain.activations

import brain.activation.Activations
import brain.utils.printRedBr
import kotlin.test.Test

class ActivationsTest {

	private val testArrayHalf = listOf(10f, 6f, 5f, 4f, 3f, 2.5f, 2f, 1.5f, 1f, 0.9f, 0.75f, 0.66f, 0.5f, 0.4f, 0.33f, 0.2f, 0.1f, 0f)
	private val testArray = testArrayHalf.map { -it } + testArrayHalf.asReversed().drop(1)

	@Test
	fun testSigmoid() {
		testArray.forEach { printRedBr(Activations.Sigmoid.apply(it)) }
	}

}