package utils.frames.modelframe

import utils.frames.ColumnFilter
import utils.frames.ColumnScaleFilter

interface DataFrameModel {
	/** description of data count point, equal to array size
	 * should point to a static object to avoid memory alloc
	 */
	val describeHeader: Array<String>

	fun getValueByKey(key: String): Float?

	/** Uses an existing allocation */
	fun fill2FArray(destination: FloatArray)

	/** description of data count, equal to array size produced */
	val describeDataCount: Int get() = describeHeader.size

	/** Returns a new allocation */
	fun to2FArray(): FloatArray {
		return FloatArray(describeDataCount).also { fill2FArray(it) }
	}

	fun fill2FArray(filter: ColumnScaleFilter, buffer: FloatArray, destination: FloatArray) {
		fill2FArray(buffer)
		var i = 0
		buffer.forEachIndexed { index, fl ->
			if (filter.containsKey(describeHeader[index])) {
				destination[i++] = fl
			}
		}
	}

	fun to2FArray(filter: ColumnScaleFilter): FloatArray {
		val result = FloatArray(filter.values.size)
		val buffer = to2FArray()

		var i = 0
		buffer.forEachIndexed { index, fl ->
			if (filter.containsKey(describeHeader[index])) {
				result[i++] = fl
			}
		}
		return result
	}

	fun validateMatching(destination: FloatArray) {
		if (this.describeDataCount != destination.size) {
			throw IllegalStateException("Mismatch of data count and destination")
		}
		if (this.describeHeader.size != this.describeDataCount) {
			throw IllegalStateException("Mismatch of data count and headers")
		}
	}

}