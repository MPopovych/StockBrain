package utils.frames

import utils.ext.average
import utils.ext.iqrAndMedian
import utils.ext.median
import utils.ext.std
import utils.math.NPStandardize
import utils.math.NegativePositiveNorm
import utils.math.RobustNorm
import utils.math.ZeroPositiveNorm

enum class ScaleMetaType {
	NormalizeZP,
	NormalizeNP,
	Standardize,
	Robust,

	None;

	fun buildForArray(array: FloatArray): ScaleMeta {
		val mean = array.average().toFloat()
		val max = array.max()
		val min = array.min()
		val std = array.std()
		val (iqr, median) = array.iqrAndMedian()

		return ScaleMeta(this, mean, max, min, std, median, iqr)
	}

	fun buildForArray(array: Iterable<Number>): ScaleMeta {
		val mean = array.average()
		val max = array.maxOf { it.toFloat() }
		val min = array.minOf { it.toFloat() }
		val std = array.std()
		val (iqr, median) = array.iqrAndMedian()

		return ScaleMeta(this, mean, max, min, std, median, iqr)
	}
}

data class ScaleMeta(
	val type: ScaleMetaType,
	val mean: Float,
	val max: Float,
	val min: Float,
	val std: Float,
	val median: Float,
	val iqr: Float
) {

	companion object {
		val None = ScaleMeta(type = ScaleMetaType.None, 0f, 0f, 0f, 0f, 0f, 0f)
		private val ZPNorm = ZeroPositiveNorm()
		private val NPNorm = NegativePositiveNorm()
		private val NPStandardize = NPStandardize()
		private val RobustNorm = RobustNorm()
	}

	fun getMeanAndStdString(): String {
		return "mean=${mean}, std=${std}"
	}

	fun applyToValue(value: Float): Float {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, value)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, value)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, value)
			ScaleMetaType.Robust -> RobustNorm.performScale(this, value)
			ScaleMetaType.None -> value
		}
	}

	fun applyToArray(array: FloatArray): FloatArray {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, array)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, array)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, array)
			ScaleMetaType.Robust -> RobustNorm.performScale(this, array)
			ScaleMetaType.None -> array.copyOf()
		}
	}
	fun applyToArray(nArray: Iterable<Number>): FloatArray {
		return applyToArray(array = nArray.map { it.toFloat() }.toFloatArray())
	}
}
