package utils

const val ANSI_RESET = "\u001B[0m"
const val ANSI_BLACK = "\u001B[30m"
const val ANSI_RED = "\u001B[31m"
const val ANSI_GREEN = "\u001B[32m"
const val ANSI_YELLOW = "\u001B[33m"
const val ANSI_BLUE = "\u001B[34m"
const val ANSI_PURPLE = "\u001B[35m"
const val ANSI_CYAN = "\u001B[36m"
const val ANSI_WHITE = "\u001B[37m"

fun getRed(msg: Any): String {
	return color(ANSI_RED, msg)
}

fun getGreen(msg: Any): String {
	return color(ANSI_GREEN, msg)
}

fun getYellow(msg: Any): String {
	return color(ANSI_YELLOW, msg)
}

fun getBlue(msg: Any): String {
	return color(ANSI_BLUE, msg)
}

fun getPurple(msg: Any): String {
	return color(ANSI_PURPLE, msg)
}

fun getCyan(msg: Any): String {
	return color(ANSI_CYAN, msg)
}

fun getGray(msg: Any): String {
	return color(ANSI_WHITE, msg)
}

fun color(color: String, msg: Any): String {
	return "$color$msg$ANSI_RESET".replace("\n", "$ANSI_RESET\n$color")
}

fun printRed(msg: Any) { // will be used for errors, warnings
	println(getRed(msg))
}

fun printRed(head: String, msg: Any) { // for positive info
	println(getRed("${head}: $msg"))
}

fun printGreen(msg: Any) { // for positive info
	println(getGreen(msg))
}

fun printGreen(head: String, msg: Any) { // for positive info
	println(getGreen("${head}: $msg"))
}

fun printYellow(msg: Any) { // will be used for bot reporting or warnings
	println(getYellow(msg))
}

fun printBlue(msg: Any) { // will be used for api requests
	println(getBlue(msg))
}

fun printBlue(head: String, msg: Any) { // for positive info
	println(getBlue("${head}: $msg"))
}

fun printPurple(msg: Any) { // will be used for benchmarks
	println(getPurple(msg))
}

fun printCyan(msg: Any) { // will be used for debug
	println(getCyan(msg))
}

fun printGray(msg: Any) {
	println(getGray(msg))
}

fun getBenchmark(msg: Any): String {
	return getPurple(msg)
}

fun printBenchmark(msg: Any) {
	println(getBenchmark(msg))
}


inline fun <T> logBenchmarkResult(name: String, block: () -> T): T {
	val start = System.currentTimeMillis()
	val result = block()
	val time = System.currentTimeMillis() - start
	printBenchmark("[$name] took ${time}ms")
	return result
}
