package utils.ext

import kotlin.math.*

class AvgCollector(private val iterations: Int) {

	private var sum = 0.0
	private var insertPosition = 0
	private var headNumber = 0
	private val array = DoubleArray(iterations)

	fun add(new: Double) {
		if (headNumber < iterations) headNumber++
		sum -= array[insertPosition]
		sum += new
		array[insertPosition] = new
		insertPosition = (insertPosition + 1) % iterations
	}

	fun getAvg(): Double? {
		if (headNumber == 0) return null
		return sum / headNumber
	}
}

// own implementation, 5+ times faster than the simple one
fun createPrimeArray(count: Int): IntArray {
	val array = IntArray(count)
	if (count > 0) array[0] = 2
	if (count > 1) array[1] = 3
	var filled = 2
	var i = 2
	while (filled < count) {
		if (array.none { a -> a != 0 && i % a == 0 }) {
			array[filled] = i
			filled++
		}
		i++
	}
	return array
}

fun List<Int>.prod() = this.reduce { acc, i -> acc * i }
fun List<Long>.prod() = this.reduce { acc, i -> acc * i }

fun <T> bollPair(inputEntries: List<T>, count: Int, block: (T) -> Double): Pair<DoubleArray, DoubleArray> {
	if (count >= inputEntries.size) throw Exception()

	val movingAv = movingAverage(inputEntries, count, block)
	val standardDeviation = standardDeviation(inputEntries, count, block)

	val upper = DoubleArray(movingAv.size)
	val lower = DoubleArray(movingAv.size)
	for (i in movingAv.indices) {
		val middleBollPoint = movingAv[i]
		val deviation = standardDeviation[i]

		upper[i] = middleBollPoint + (deviation * 2)
		lower[i] = middleBollPoint - (deviation * 2)
	}
	return Pair(upper, lower)
}

// l = size - count
fun <T> standardDeviation(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	if (count >= inputEntries.size) throw Exception("size: ${inputEntries.size} vs count: $count")
	val stdDevArray = DoubleArray(inputEntries.size - count) { 0.0 }

	var totalAverage = 0.0
	var totalSquares = 0.0

	for (i in 0 until inputEntries.size - 1) {
		val value = block(inputEntries[i])
		totalAverage += value
		totalSquares += value.pow(2)

		if (i >= count - 1) {
			val temp: Double = (totalSquares - totalAverage.pow(2) / count) / count
			stdDevArray[i - count + 1] = sqrt(abs(temp))

			val valueSub = block(inputEntries[i - count + 1])
			totalAverage -= valueSub
			totalSquares -= valueSub.pow(2)
		}
	}
	return stdDevArray
}

fun DoubleArray.std(): Double {
	val squared = this.map { it.pow(2) }
	val sumSquared = squared.sum()
	val sum = this.sum()
	val main = (sumSquared - (sum.pow(2) / size)) / size
	return sqrt(abs(main))
}

fun FloatArray.std(): Float {
	val squared = this.map { it.pow(2) }
	val sumSquared = squared.sum()
	val sum = this.sum()
	val main = (sumSquared - (sum.pow(2) / size)) / size
	return sqrt(abs(main))
}

fun FloatArray.median(): Float {
	val sorted = this.sorted()
	return if (sorted.size % 2 == 0)
		(sorted[sorted.size / 2] + sorted[(sorted.size - 1) / 2]) / 2
	else
		sorted[sorted.size / 2]
}

fun FloatArray.iqrAndMedian(): Pair<Float, Float> {
	val sorted = this.sorted()
	val median = if (sorted.size % 2 == 0)
		(sorted[sorted.size / 2] + sorted[(sorted.size - 1) / 2]) / 2
	else
		sorted[sorted.size / 2]

	val q1: Float = sorted[floor(sorted.size * 0.25).toInt()]
	val q3: Float = sorted[ceil(sorted.size * 0.75).toInt()]
	val iqr = q3 - q1
	return Pair(iqr, median)
}

fun Iterable<Number>.average(): Float {
	val mapped = this.map { it.toDouble() }
	val sum = mapped.sum()
	return (sum / mapped.size).toFloat()
}

fun Iterable<Number>.std(): Float {
	val squared = this.map { it.toDouble().pow(2) }
	val sumSquared = squared.sum()
	val sum = this.sumOf { it.toDouble() }
	val main = (sumSquared - (sum.pow(2) / squared.size)) / squared.size
	return sqrt(abs(main)).toFloat()
}

fun Iterable<Number>.median(): Float {
	val sorted = this.map { it.toFloat() }.sorted()
	return if (sorted.size % 2 == 0)
		(sorted[sorted.size / 2] + sorted[(sorted.size - 1) / 2]) / 2
	else
		sorted[sorted.size / 2]
}

fun Iterable<Number>.iqrAndMedian(): Pair<Float, Float> {
	val sorted = this.map { it.toFloat() }.sorted()
	val median = if (sorted.size % 2 == 0)
		(sorted[sorted.size / 2] + sorted[(sorted.size - 1) / 2]) / 2
	else
		sorted[sorted.size / 2]

	val q1: Float = sorted[floor(sorted.size * 0.25).toInt()]
	val q3: Float = sorted[ceil(sorted.size * 0.75).toInt()]
	val iqr = q3 - q1
	return Pair(iqr, median)
}


// l = size
fun <T> movingAverageFull(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	if (count > inputEntries.size) return DoubleArray(0)

	val meanArray = DoubleArray(inputEntries.size) { 0.0 }
	var meanSum = 0.0
	inputEntries.forEachIndexed { index, entry ->
		if (index >= count) {
			meanSum -= block(inputEntries[index - count])
			meanSum += block(entry)
			meanArray[index] = meanSum / count
		} else {
			meanSum += block(entry)
			meanArray[index] = meanSum / (index + 1)
		}
	}
	return meanArray
}

// l = size
fun <T> exponentialMovingAverage(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	val a = 2.0 / (count + 1)

	val meanArray = DoubleArray(inputEntries.size) { 0.0 }
	var oldValue = block(inputEntries[0])
	inputEntries.forEachIndexed { index, entry ->
		val value = block(inputEntries[index]) * a + oldValue * (1.0 - a)
		meanArray[index] = value
		oldValue = value
	}
	return meanArray
}

// l = size
fun <T> movingSmoothing(inputEntries: List<T>, sideCount: Int, block: (T) -> Double): DoubleArray {
	val meanArray = DoubleArray(inputEntries.size) { 0.0 }
	val mapped = inputEntries.map { block(it) }
	mapped.forEachIndexed { index, _ ->
		val window = mapped.subList(max(0, index - sideCount), min(mapped.size - 1, index + sideCount))
		meanArray[index] = window.average().toDouble()
	}
	return meanArray
}


fun <T> reverseDeviation(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	val array = DoubleArray(inputEntries.size - count) { 0.0 }
	val mapped = inputEntries.map { block(it) }
	mapped.drop(count).forEachIndexed { index, d ->
		val window = mapped.subList(max(index, 0), index + count)
		val mean = window.average()
		val std = window.map { sqrt(abs(it - mean)) }.sum() / (window.size - 1)
		array[index] = std.pow(2)
	}
	return array
}

// l = size
fun <T> dynamicMovingAverage(inputEntries: List<T>, countArray: IntArray, block: (T) -> Double): DoubleArray {
	val meanArray = DoubleArray(inputEntries.size) { 0.0 }
	var oldValue = block(inputEntries[0])
	inputEntries.forEachIndexed { index, entry ->
		val count = countArray[index]
		val a = 2.0 / (count + 1)

		val value = block(inputEntries[index]) * a + oldValue * (1.0 - a)
		meanArray[index] = value
		oldValue = value
	}
	return meanArray
}

// l = size - count
fun <T> dynamicStandardDeviation(inputEntries: List<T>, countArray: IntArray, block: (T) -> Double): DoubleArray {
	val stdDevArray = DoubleArray(inputEntries.size) { 0.0 }
	val mapped = inputEntries.map { block(it) }
	mapped.forEachIndexed { index, d ->
		val count = countArray[index]
		val window = mapped.subList(max(index - count, 0), index)
		val mean = window.average()
		val std = window.map { (it - mean).pow(2) }.sum() / (window.size - 1)
		stdDevArray[index] = sqrt(std)
	}
	return stdDevArray
}


// l = size - count
fun <T> movingAverage(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	if (count >= inputEntries.size) return DoubleArray(0)

	val meanArray = DoubleArray(inputEntries.size - count) { 0.0 }
	var meanSum = 0.0
	inputEntries.forEachIndexed { index, entry ->
		if (index >= count) {
			meanSum -= block(inputEntries[index - count])
			meanSum += block(entry)
			meanArray[index - count] = meanSum / count
		} else {
			meanSum += block(entry)
		}
	}
	return meanArray
}


// l = size
fun <T> movingMinimum(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	if (count > inputEntries.size) return DoubleArray(0)

	val queue = ArrayDeque<Pair<Double, Int>>()
	val returnArray = ArrayList<Double>()
	inputEntries.forEachIndexed { index, entry ->
		if (queue.isNotEmpty() && queue.first().second <= index - count) {
			queue.removeFirst()
		}
		while (queue.isNotEmpty() && queue.last().first > block(entry)) {
			queue.removeLast()
		}

		queue.addLast(Pair(block(entry), index))

		returnArray.add(queue.first().first)
	}
	return returnArray.toDoubleArray()
}

// l = size
fun <T> movingMaximum(inputEntries: List<T>, count: Int, block: (T) -> Double): DoubleArray {
	if (count > inputEntries.size) return DoubleArray(0)

	val queue = ArrayDeque<Pair<Double, Int>>()
	val returnArray = ArrayList<Double>()
	inputEntries.forEachIndexed { index, entry ->
		if (queue.isNotEmpty() && queue.first().second <= index - count) {
			queue.removeFirst()
		}
		while (queue.isNotEmpty() && queue.last().first < block(entry)) {
			queue.removeLast()
		}

		queue.addLast(Pair(block(entry), index))

		returnArray.add(queue.first().first)
	}
	return returnArray.toDoubleArray()
}

// l = size
fun mapNullGapLength(inputEntries: Array<Double?>): IntArray {
	var localGapLength = 0
	var lastFullGapLength = 0
	val gapArray = IntArray(inputEntries.size)
	inputEntries.forEachIndexed { index, d ->
		if (d == null) {
			localGapLength++
		} else {
			lastFullGapLength = localGapLength
			localGapLength = 0
		}
		gapArray[index] = lastFullGapLength
	}
	return gapArray
}

// l = size
fun <T> localMinimumSpots(inputEntries: List<T>, count: Int, block: (T) -> Double): Array<Double?> {
	val returnArray = Array<Double?>(inputEntries.size) { null }

	var pendingPosition = 0
	var pendingMin: Double = block(inputEntries[0])
	inputEntries.forEachIndexed { index, d ->
		val value = block(d)

		val next = inputEntries.getOrNull(index + 1)
		val nextValue = next.let { block(it ?: return@let Double.MIN_VALUE) }

		if (pendingPosition + count < index && nextValue < value) {
			returnArray[pendingPosition] = pendingMin
			pendingPosition = index
			pendingMin = value
		}

		if (value < pendingMin) {
			pendingPosition = index
			pendingMin = value
		}
	}

	returnArray[pendingPosition] = pendingMin

	return returnArray
}

// l = size - window
fun <T> localAngle(
	inputEntries: List<T>,
	window: Int,
	countSmall: Int,
	countBig: Int,
	block: (T) -> Double,
): Array<Double> {
	val returnArray = Array<Double>(inputEntries.size) { 0.0 }

	val short = exponentialMovingAverage(inputEntries, countSmall, block)
	val long = exponentialMovingAverage(inputEntries, countBig, block)

	for (i in inputEntries.indices) {
		val startLong = long[max(i - window, 0)] // plane - 0.0
		val endLong = long[i]
		val growthLong = endLong / startLong // range - 1.0

		val startShort = short[max(i - window, 0)]
		val endShort = short[i]
		val growthShort = endShort / startShort

		val rad = atan2(growthShort - growthLong, 1.0)
		returnArray[i] = rad
	}

	return returnArray
}

// l = size
fun <T> localMaximumSpots(inputEntries: List<T>, count: Int, block: (T) -> Double): Array<Double?> {
	val returnArray = Array<Double?>(inputEntries.size) { null }

	var pendingPosition = 0
	var pendingMax: Double = block(inputEntries[0])
	inputEntries.forEachIndexed { index, d ->
		val value = block(d)

		val next = inputEntries.getOrNull(index + 1)
		val nextValue = next.let { block(it ?: return@let Double.MIN_VALUE) }

		if (pendingPosition + count < index && nextValue > value) {
			returnArray[pendingPosition] = pendingMax
			pendingPosition = index
			pendingMax = value
		}

		if (value > pendingMax) {
			pendingPosition = index
			pendingMax = value
		}
	}

	returnArray[pendingPosition] = pendingMax

	return returnArray
}


// l = size
fun <T> localMaximumSpotsFiltered(
	inputEntries: List<T>,
	count: Int,
	block: (T) -> Double,
	filter: (Double, Int) -> Double?,
): Array<Double?> {
	val returnArray = Array<Double?>(inputEntries.size) { null }

	var pendingPosition = 0
	var pendingMax: Double = block(inputEntries[0])
	inputEntries.forEachIndexed { index, d ->
		val value = block(d)

		val next = inputEntries.getOrNull(index + 1)
		val nextValue = next.let {
			val temp = block(it ?: return@let Double.MIN_VALUE)
			return@let filter(temp, index)
		}

		if (nextValue != null && pendingPosition + count < index && nextValue > value) {
			returnArray[pendingPosition] = pendingMax
			pendingPosition = index
			pendingMax = value
		}

		if (value > pendingMax) {
			pendingPosition = index
			pendingMax = value
		}
	}

	returnArray[pendingPosition] = pendingMax

	return returnArray
}