package utils.frames

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

	fun applyToArray(array: FloatArray): FloatArray {
		return when (this.type) {
			ScaleMetaType.NormalizeZP -> ZPNorm.performScale(this, array)
			ScaleMetaType.NormalizeNP -> NPNorm.performScale(this, array)
			ScaleMetaType.Standardize -> NPStandardize.performScale(this, array)
			ScaleMetaType.None -> array.copyOf()
		}
	}
}
