package utils.frames

import utils.ext.average
import utils.ext.std
import utils.math.NPStandardize
import utils.math.NegativePositiveNorm
import utils.math.ZeroPositiveNorm

enum class ScaleMetaType {
	NormalizeZP,
	NormalizeNP,
	Standardize,

	None;

	fun buildForArray(array: FloatArray): ScaleMeta {
		val mean = array.average().toFloat()
		val max = array.max()
		val min = array.min()
		val std = array.std()

		return ScaleMeta(this, mean, max, min, std)
	}

	fun buildForArray(array: Iterable<Number>): ScaleMeta {
		val mean = array.average()
		val max = array.maxOf { it.toFloat() }
		val min = array.minOf { it.toFloat() }
		val std = array.std()

		return ScaleMeta(this, mean, max, min, std)
	}
}

data class ScaleMeta(
	val type: ScaleMetaType,
	val mean: Float,
	val max: Float,
	val min: Float,
	val std: Float,
) {

	companion object {
		val None = ScaleMeta(type = ScaleMetaType.None, 0f, 0f, 0f, 0f)
		private val ZPNorm = ZeroPositiveNorm()
		private val NPNorm = NegativePositiveNorm()
		private val NPStandardize = NPStandardize()
	}

	fun getMeanAndStdString(): String {
		return "mean=${mean}, std=${std}"
	}

	fun applyToValue(value: Float): Float {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, value)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, value)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, value)
			ScaleMetaType.None -> value
		}
	}

	fun applyToArray(array: FloatArray): FloatArray {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, array)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, array)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, array)
			ScaleMetaType.None -> array.copyOf()
		}
	}
	fun applyToArray(nArray: Iterable<Number>): FloatArray {
		return applyToArray(array = nArray.map { it.toFloat() }.toFloatArray())
	}
}
