package utils.frames

import utils.ext.average
import utils.ext.iqrAndMedian
import utils.ext.std
import utils.math.*

enum class ScaleMetaType {
	NormalizeZP,
	NormalizeNP,
	Standardize,
	Robust,
	StandardizeOnZero,
	TanhNorm,
	Scale20,

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
		private val TanhHorm = TanhNorm()
		private val Scale20 = Scale20()
		private val StandardizeZeroNorm = NPStandardizeZeroNorm()
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
			ScaleMetaType.StandardizeOnZero -> StandardizeZeroNorm.performScale(this, value)
			ScaleMetaType.TanhNorm -> TanhHorm.performScale(this, value)
			ScaleMetaType.Scale20 -> Scale20.performScale(this, value)
			ScaleMetaType.None -> value
		}
	}

	fun applyToArray(array: FloatArray): FloatArray {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, array)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, array)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, array)
			ScaleMetaType.Robust -> RobustNorm.performScale(this, array)
			ScaleMetaType.StandardizeOnZero -> StandardizeZeroNorm.performScale(this, array)
			ScaleMetaType.TanhNorm -> TanhHorm.performScale(this, array)
			ScaleMetaType.Scale20 -> Scale20.performScale(this, array)
			ScaleMetaType.None -> array.copyOf()
		}
	}
	fun applyToArray(nArray: Iterable<Number>): FloatArray {
		return applyToArray(array = nArray.map { it.toFloat() }.toFloatArray())
	}
}
