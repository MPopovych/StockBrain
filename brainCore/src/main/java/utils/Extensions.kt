package utils

import layers.LayerShape
import matrix.Matrix
import java.math.RoundingMode
import java.nio.ByteBuffer
import java.util.*

fun Matrix.getShape(): LayerShape {
	return LayerShape(this.width, this.height)
}

fun Matrix.print() {
	printBlue(describe())
}

fun Matrix.printRed() {
	printRed(describe())
}

fun Matrix.describe(): String {
	val sb = StringBuilder()
	for (y in 0 until height) {
		val line = "[${values.joinToString { it[y].roundDisplay() }}]"
		sb.append(line).appendLine()
	}
	return sb.toString().trimIndent()
}


fun Matrix.printTransposed() {
	val sb = StringBuilder().append("[")
	for (x in 0 until width) {
		val line = "[${values[x].joinToString { it.roundDisplay() }}]"
		sb.append(line).appendLine()
	}
	sb.append("]")
	printBlue(sb.toString().trimIndent())
}

fun FloatArray.encodeGenes(): String {
	val buff = ByteBuffer.allocate(this.size * 4)
	for (f in this) {
		buff.putFloat(f)
	}
	return Base64.getEncoder().encodeToString(buff.array())
}

public inline fun <T> T.ifAlso(enabled: Boolean, block: (T) -> Unit): T {
	if (!enabled) return this
	block(this)
	return this
}
