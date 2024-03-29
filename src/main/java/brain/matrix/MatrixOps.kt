package brain.matrix

import brain.utils.roundDisplay
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.operations.all
import org.jetbrains.kotlinx.multik.ndarray.operations.any
import org.jetbrains.kotlinx.multik.ndarray.operations.map
import org.jetbrains.kotlinx.multik.ndarray.operations.toList
import utils.wrap

operator fun Matrix.get(x: Int, y: Int): Float {
	return this.array.get(ind1 = y, ind2 = x)
}

fun Matrix.transpose(): Matrix {
	return Matrix(this.array.transpose())
}

fun Matrix.sumHorizontal(): Matrix {
	val output = Array(this.height) { FloatArray(1) }
	this.iteratorWithPos().forEach { (x, y, v) ->
		output[y][0] += v
	}
	return Matrix.wrapMD(1, this.height, output)
}

fun Matrix.flatten(): Matrix {
	return Matrix.wrap(this.width * this.height, 1, this.readFloatData())
}

fun Matrix.map(block: (Float) -> Float): Matrix {
	return Matrix(this.array.map(block))
}

fun Matrix.any(block: (Float) -> Boolean): Boolean {
	return this.array.any(block)
}

fun Matrix.all(block: (Float) -> Boolean): Boolean {
	return this.array.all(block)
}

fun Matrix.iterRows() = (0 until height).iterator().wrap { this.row(it) }

fun Matrix.withPos() = this.array.data.mapIndexed { index, fl ->
	val x = index % width
	val y = (index - x) / height
	Triple(x, y, fl)
}

fun Matrix.iteratorWithPos() = this.array.data.iterator().withIndex().wrap {
	val x = it.index % width
	val y = (it.index - x) / height
	Triple(x, y, it.value)
}

fun Matrix.mapWithPos(block: (Triple<Int, Int, Float>) -> Float): Matrix {
	val array = this.array.data.mapIndexed { index, fl ->
		val x = index % width
		val y = (index - x) / height
		return@mapIndexed block(Triple(x, y, fl))
	}.toFloatArray()
	return Matrix.wrap(width, height, array)
}

fun List<Matrix>.concat(axis: Int): Matrix {
	return Matrix(this.first().array.cat(this.drop(1).map { it.array }, axis))
}

fun Matrix.describe(): String {
	val sb = StringBuilder()
	array.toList().chunked(this.width).map {
		"[${it.joinToString { v -> v.roundDisplay() }}]"
	}.forEach { line -> sb.appendLine(line) }
	return sb.toString().trimIndent()
}
