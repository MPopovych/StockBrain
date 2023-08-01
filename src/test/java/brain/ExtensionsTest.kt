package brain

import brain.utils.printBlueBr
import brain.utils.printGrayBr
import brain.utils.upscale
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtensionsTest {
	@Test
	fun testUpscale() {
		val f1 = 0.2144f
		val f2 = f1.upscale()
		printGrayBr(f1)
		printBlueBr(f2)
		assertEquals(0.214f, f2)
	}
}