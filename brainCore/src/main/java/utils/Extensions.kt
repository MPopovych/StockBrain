package utils

import layers.LayerShape
import matrix.Matrix
import java.math.RoundingMode
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
		if (y == height - 1) {
			sb.append(line)
		} else {
			sb.appendLine(line)
		}
	}
	return sb.toString()
}


fun Matrix.printTransposed() {
	val sb = StringBuilder().append("[")
	for (x in 0 until width) {
		val line = "[${values[x].joinToString { it.roundDisplay() }}]"
		if (x == width - 1) {
			sb.append(line)
		} else {
			sb.appendLine(line)
		}
	}
	sb.append("]")
	printBlue(sb.toString())
}

fun Float.roundToDec(decimals: Int): Double {
	return this.toBigDecimal().setScale(decimals, RoundingMode.HALF_EVEN).toDouble()
}

fun Float.roundDisplay(): String {
	return this.toBigDecimal().setScale(3, RoundingMode.HALF_EVEN).toPlainString().removeTrailingZeroes()
}

fun Float.asPlainDouble(): String {
	return this.toBigDecimal().toPlainString().removeTrailingZeroes()
}

fun String.removeTrailingZeroes(): String {
	if (contains('.')) {
		return this.trimEnd('0').trimEnd('.')
	}
	return this
}

public inline fun <T> T.ifAlso(enabled: Boolean, block: (T) -> Unit): T {
	if (!enabled) return this
	block(this)
	return this
}
