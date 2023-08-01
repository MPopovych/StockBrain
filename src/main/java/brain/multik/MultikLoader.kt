package brain.multik

import brain.utils.printBlueBr
import org.jetbrains.kotlinx.multik.api.mk

object MultikLoader {
	@Synchronized
	fun loadSync() {
		val mkLinAlg = mk.linalg
		printBlueBr("MK engine init: ${mk.engine}, ${mkLinAlg::class.simpleName}")
	}

	@Synchronized
	fun setOpenBlasThreads(n: Int) {
		require(n in 1..11)

	}
}