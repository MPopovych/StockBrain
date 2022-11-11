package utils

private const val ANSI_RESET = "\u001B[0m"
private const val ANSI_BLACK = "\u001B[30m"
private const val ANSI_RED = "\u001B[31m"
private const val ANSI_GREEN = "\u001B[32m"
private const val ANSI_YELLOW = "\u001B[33m"
private const val ANSI_BLUE = "\u001B[34m"
private const val ANSI_PURPLE = "\u001B[35m"
private const val ANSI_CYAN = "\u001B[36m"
private const val ANSI_WHITE = "\u001B[37m"

private fun getRed(msg: Any): String {
	return color(ANSI_RED, msg)
}

private fun getGreen(msg: Any): String {
	return color(ANSI_GREEN, msg)
}

private fun getYellow(msg: Any): String {
	return color(ANSI_YELLOW, msg)
}

private fun getBlue(msg: Any): String {
	return color(ANSI_BLUE, msg)
}

private fun getPurple(msg: Any): String {
	return color(ANSI_PURPLE, msg)
}

private fun getCyan(msg: Any): String {
	return color(ANSI_CYAN, msg)
}

private fun getGray(msg: Any): String {
	return color(ANSI_WHITE, msg)
}

private fun color(color: String, msg: Any): String {
	return "$color$msg$ANSI_RESET".replace("\n", "$ANSI_RESET\n$color")
}

fun printRedBr(msg: Any) { // will be used for errors, warnings
	println(getRed(msg))
}

fun printRedBr(head: String, msg: Any) { // for positive info
	println(getRed("${head}: $msg"))
}

fun printGreenBr(msg: Any) { // for positive info
	println(getGreen(msg))
}

fun printGreenBr(head: String, msg: Any) { // for positive info
	println(getGreen("${head}: $msg"))
}

fun printYellowBr(msg: Any) { // will be used for bot reporting or warnings
	println(getYellow(msg))
}

fun printBlueBr(msg: Any) { // will be used for api requests
	println(getBlue(msg))
}

fun printBlueBr(head: String, msg: Any) { // for positive info
	println(getBlue("${head}: $msg"))
}

fun printPurpleBr(msg: Any) { // will be used for benchmarks
	println(getPurple(msg))
}

fun printCyanBr(msg: Any) { // will be used for debug
	println(getCyan(msg))
}

fun printGrayBr(msg: Any) {
	println(getGray(msg))
}

private fun getBenchmark(msg: Any): String {
	return getPurple(msg)
}

fun printBenchmarkBr(msg: Any) {
	println(getBenchmark(msg))
}

inline fun <T> brBenchmark(name: String, block: () -> T): T {
	val start = System.currentTimeMillis()
	val result = block()
	val time = System.currentTimeMillis() - start
	printBenchmarkBr("[$name] took ${time}ms")
	return result
}
