package brain.utils

import brain.layers.LayerShape
import brain.matrix.Matrix
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.ln


infix fun Double.lnDiv(other: Double): Double {
	return ln(this / other) + 1.0
}

fun Matrix.getShape(): LayerShape {
	return LayerShape(this.width, this.height)
}

fun Matrix.print() {
	printBlueBr(describe())
}

fun Matrix.printRedBr() {
	printRedBr(describe())
}
fun Matrix.printGreenBr() {
	printGreenBr(describe())
}

fun Matrix.describe(): String {
	val sb = StringBuilder()
	for (y in 0 until height) {
		val line = "[${values[y].joinToString {it.roundDisplay() }}]"
		sb.append(line).appendLine()
	}
	return sb.toString().trimIndent()
}


fun FloatArray.encodeGenes(): String {
	val buff = ByteBuffer.allocate(this.size * 4)
	for (f in this) {
		buff.putFloat(f)
	}
	return Base64.getEncoder().encodeToString(buff.array())
}

fun FloatArray.reshapeToMatrix(w: Int, h: Int): Matrix {
	val m = Matrix(w, h)
	m.writeFloatData(this)
	return m
}

inline fun <T> T.ifAlsoBr(enabled: Boolean, block: (T) -> Unit): T {
	if (!enabled) return this
	block(this)
	return this
}
