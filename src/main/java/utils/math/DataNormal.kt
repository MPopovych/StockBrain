package utils.math

import utils.ext.std
import utils.frames.ScaleMeta
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.sqrt


fun <T> createLogNormal(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block).toDoubleArray()
	val mean = array.average()
	val std = array.std()
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.LogNormal, mean = mean, max = max, min = min, std = std
	)
}

fun <T> createLogZeroNormal(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block).toDoubleArray()
	val mean = 0.0
	val std = array.std()
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.LogNormal, mean = mean, max = max, min = min, std = std
	)
}

fun <T> createMZeroMeanNormal(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block).toDoubleArray()
	val mean = 0.0
	val std = array.std()
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.MNormal, mean = mean, max = max, min = min, std = std
	)
}

fun <T> createMShiftedStdNormal(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block).toDoubleArray()
	val mean = 0.0
	val std = array.std() + array.average() / 2
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.MNormal, mean = mean, max = max, min = min, std = std
	)
}

fun <T> createMNormal(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block).toDoubleArray()
	val mean = array.average()
	val std = array.std()
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.MNormal, mean = mean, max = max, min = min, std = std
	)
}

fun <T> createZPScale(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block)
	val mean = array.average()
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.ZPScale, mean = mean, max = max, min = min
	)
}

fun <T> createNPScale(entries: List<T>, block: (T) -> Double): ScaleDataState {
	val array = entries.map(block)
	val mean = 0.0
	val max = array.maxOf { it }
	val min = array.minOf { it }

	return ScaleDataState(
			type = ScaleDataType.NPZeroMeanScale, mean = mean, max = max, min = min
	)
}

fun <T> createZPScaleAndApply(entries: List<T>, block: (T) -> Double): Pair<DoubleArray, ScaleDataState> {
	val state = createZPScale(entries, block)

	return Pair(state.scale(entries, block), state)
}

fun <T> createWNormalAndApply(entries: List<T>, block: (T) -> Double): Pair<DoubleArray, ScaleDataState> {
	val state = createLogNormal(entries, block)
	return Pair(state.scale(entries, block), state)
}

fun <T> createMNormalAndApply(entries: List<T>, block: (T) -> Double): Pair<DoubleArray, ScaleDataState> {
	val state = createMNormal(entries, block)
	return Pair(state.scale(entries, block), state)
}

fun <T> createNPScaleAndApply(entries: List<T>, block: (T) -> Double): Pair<DoubleArray, ScaleDataState> {
	val state = createNPScale(entries, block)
	return Pair(state.scale(entries, block), state)
}

fun <T> ScaleDataState.scale(entries: List<T>, block: (T) -> Double): DoubleArray {
	val array = entries.map(block)

	return when (type) {
		ScaleDataType.NPZeroMeanScale -> npScale(this, array)
		ScaleDataType.ZPScale -> zpScale(this, array)
		ScaleDataType.LogNormal -> logNormal(this, array)
		ScaleDataType.LogZeroMeanNormal -> logNormal(this, array)
		ScaleDataType.MNormal -> mNormal(this, array)
		ScaleDataType.MZeroMeanNormal -> mNormal(this, array)
		ScaleDataType.MShiftedStdNormal -> mNormal(this, array)
		ScaleDataType.None -> array.toDoubleArray()
		null -> return logNormal(this, array)
	}
}

private fun logNormal(owner: ScaleDataState, array: List<Double>): DoubleArray {
	val mean = owner.mean
	val std = owner.std

	if (std == 0.0) return array.map { 0.0 }.toDoubleArray()

	return array
			.map { x ->
				(x - mean) / std
			}
			.map { x ->
				val absX = abs(x)
				if (absX == 0.0) return@map 0.0
				val sign = x / absX
				return@map log2(absX) * sign
			}
			.toDoubleArray()
}

private fun mNormal(owner: ScaleDataState, array: List<Double>): DoubleArray {
	val mean = owner.mean
	val std = owner.std

	return array
			.map { x ->
				(x - mean) / std
			}
			.map { x ->
				val absX = abs(x)
				if (absX > 5.0) {
					val sign = x / absX
					val r = (sqrt(absX - 4.0) + 4.0)
					return@map r * sign
				}
				return@map x
			}
			.toDoubleArray()
}

private fun zpScale(owner: ScaleDataState, entries: List<Double>): DoubleArray {
	val range = owner.max - owner.min

	if (range == 0.0) return DoubleArray(entries.size) { 0.0 }

	return entries.map {
		(it - owner.min) / range
	}.toDoubleArray()
}

private fun npScale(owner: ScaleDataState, entries: List<Double>): DoubleArray {
	val lowerRange = owner.mean - owner.min
	val higherRange = owner.max - owner.mean

	return entries.map {
		if (it >= owner.mean) { // m = 66, it = 70, max = 100
			(it - owner.mean) / higherRange
		} else {
			-(owner.mean - it) / lowerRange
		}
	}.toDoubleArray()
}

enum class ScaleDataType {
	NPZeroMeanScale,
	ZPScale,
	LogNormal,
	LogZeroMeanNormal,
	MNormal,
	MZeroMeanNormal,
	MShiftedStdNormal,
	None;


	fun <T> buildScale(entries: List<T>, block: (T) -> Double): ScaleDataState {
		return when (this) {
			NPZeroMeanScale -> createNPScale(entries, block)
			ZPScale -> createZPScale(entries, block)
			LogNormal -> createLogNormal(entries, block)
			LogZeroMeanNormal -> createLogZeroNormal(entries, block)
			MNormal -> createMNormal(entries, block)
			MZeroMeanNormal -> createMZeroMeanNormal(entries, block)
			MShiftedStdNormal -> createMShiftedStdNormal(entries, block)
			None -> ScaleDataState(type = None, 0.0, 0.0, 0.0)
		}
	}
}

data class ScaleDataState(
	val type: ScaleDataType?,
	val mean: Double,
	val max: Double,
	val min: Double,
	val std: Double = 0.0,
) {
	fun getMeanAndStdString(): String {
		return "mean=${mean}, std=${std}"
	}
}
