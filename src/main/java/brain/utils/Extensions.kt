package brain.utils

import java.nio.ByteBuffer
import java.util.*


fun FloatArray.encodeToBase64(): String {
	val buff = ByteBuffer.allocate(this.size * 4)
	for (f in this) {
		buff.putFloat(f)
	}
	return Base64.getEncoder().encodeToString(buff.array())
}

inline fun <T> T.ifAlsoBr(enabled: Boolean, block: (T) -> Unit): T {
	if (!enabled) return this
	block(this)
	return this
}
