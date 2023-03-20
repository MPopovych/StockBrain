package utils.frames.modelframe

import utils.frames.ColumnScaleFilter

interface FrameAsset {

	// region abstract
	/** description of data count point, equal to array size
	 * should point to a static object to avoid memory alloc
	 */
	val describeHeader: Array<String>

	fun getValueByKey(key: String): Float?
	fun getValueByOrdinal(ord: Int): Float?
	fun getKeyOrdinal(key: String): Int?

	/** Uses an existing allocation */
	fun fill2FArray(destination: FloatArray)
	/** max index should be equal destination last index
	 * feature masks should be sorted as features
	 */
	fun fill2FArray(destination: FloatArray, featureMasks: ColumnScaleFilter)

	fun fill2FArray(destination: FloatArray, ordinalMapper: ColumnScaleFilter.OrdMapper<*>)

	// endregion

	// region implemented
	/** description of data count, equal to array size produced */
	val describeDataCount: Int get() = describeHeader.size

	/** Returns a new allocation */
	fun to2FArray(): FloatArray {
		return FloatArray(describeDataCount).also { fill2FArray(it) }
	}

	fun to2FArray(featureMasks: ColumnScaleFilter): FloatArray {
		return FloatArray(featureMasks.size).also { fill2FArray(it, featureMasks) }
	}

	fun to2FArray(ordinalMapper: ColumnScaleFilter.OrdMapper<*>): FloatArray {
		return FloatArray(ordinalMapper.size).also { fill2FArray(it, ordinalMapper) }
	}

	fun validateMatching(destination: FloatArray) {
		if (this.describeDataCount != destination.size) {
			throw IllegalStateException("Mismatch of data count and destination")
		}
		if (this.describeHeader.size != this.describeDataCount) {
			throw IllegalStateException("Mismatch of data count and headers")
		}
	}
	// endregion

}