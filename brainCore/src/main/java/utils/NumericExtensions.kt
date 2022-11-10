package utils

import java.math.RoundingMode

fun Double.roundUpInt(): Int {
	return this.toBigDecimal().setScale(0, RoundingMode.UP).toInt()
}

fun Double.roundDownInt(decimals: Int): Int {
	return this.toBigDecimal().setScale(0, RoundingMode.DOWN).toInt()
}

fun Double.roundUp(decimals: Int): Double {
	return this.toBigDecimal().setScale(decimals, RoundingMode.UP).toDouble()
}

fun Double.roundDown(decimals: Int): Double {
	return this.toBigDecimal().setScale(decimals, RoundingMode.DOWN).toDouble()
}

fun Float.roundUp(decimals: Int): Float {
	return this.toBigDecimal().setScale(decimals, RoundingMode.UP).toFloat()
}

fun Float.roundDown(decimals: Int): Float {
	return this.toBigDecimal().setScale(decimals, RoundingMode.DOWN).toFloat()
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