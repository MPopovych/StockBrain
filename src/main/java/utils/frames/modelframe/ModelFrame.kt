package utils.frames.modelframe

import utils.frames.ColumnScaleFilter

class ModelFrame<T : DataFrameModel> : ArrayList<T>() {
	companion object {
		fun <A : DataFrameModel> from(source: Iterable<A>): ModelFrame<A> {
			return ModelFrame<A>().also { it.addAll(source) }
		}
	}

	private fun ensureDataSize() = firstOrNull()?.describeDataCount

	fun throwIfHasNan() {
		val first = this.firstOrNull() ?: return
		val buffer = FloatArray(first.describeDataCount)
		this.forEachIndexed { rowIndex, item ->
			item.fill2FArray(buffer)
			buffer.forEachIndexed { i, f ->
				if (f.isNaN()) {
					val header = first.describeHeader[i]
					throw Exception("key: ${header}, index: $rowIndex is NaN")
				}
			}
		}
	}

	fun getNumberColumn(key: String): FloatArray? {
		val array = FloatArray(size)
		forEachIndexed { i, m ->
			array[i] = m.getValueByKey(key) ?: return null
		}
		return array
	}

	fun getHeadString(): String {
		val first = this.firstOrNull() ?: return "[EmptyModelFrame]"

		var buffer = first.describeHeader.joinToString(",") + "\n"
		buffer += first.to2FArray().joinToString(",") + "\n"
		return buffer
	}

	// does not allow "padded" windows, only full ones
	// window size -> length of final array
	// gap size N -> skips N-1 elements of original data
	fun window(windowSize: Int, gapSize: Int = 1): List<WindowScope<T>> {
		if (windowSize < 2 && gapSize > 1) throw Exception("Window size can't be less then 2 when gapsize is greater than 1")
		val fullWindowSize = (windowSize - 1) * gapSize + 1
		if (size < fullWindowSize) {
			throw IllegalStateException("ModelFrame is smaller then the window")
		}
		val dataCount = first().describeDataCount

		return (0..size - fullWindowSize).mapIndexed { index, i ->
			WindowScope(
				parent = this,
				windowSize = windowSize,
				dataCount = dataCount,
				startIndex = index,
				endIndex = index + fullWindowSize - 1,
				gapSize = gapSize
			)
		}
	}

	class WindowScope<G : DataFrameModel>(
		private val parent: ModelFrame<G>,
		private val windowSize: Int, // vertical
		private val dataCount: Int, // horizontal
		private val startIndex: Int,
		private val endIndex: Int, // inclusive
		private val gapSize: Int
	) {

		init {
			if (startIndex > endIndex) throw IllegalStateException("Start is greater than end")
		}

		val size = endIndex - startIndex

		fun iterate(block: (relative: Int, data: G) -> Unit) {
			return parent.subList(startIndex, endIndex).forEachIndexed { index, g ->
				if (index % gapSize != 0) return@forEachIndexed
				block(index, g)
			}
		}

		fun getHeadString(): String {
			val first = parent.getOrNull(startIndex) ?: return "[EmptyWindowScope]"
			var buffer = first.describeHeader.joinToString(",") + "\n"
			buffer += first.to2FArray().joinToString(",") + "\n"
			return buffer
		}

		fun to2fArray(): Array<FloatArray> {
			val rows = Array(windowSize) { FloatArray(dataCount) }
			iterate { i, modelFrameEntry ->
				rows[i] = modelFrameEntry.to2FArray()
			}
			return rows
		}

		fun to2fArray(destination: Array<FloatArray>): Array<FloatArray> {
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(destination[i])
			}
			return destination
		}

		fun to2fArray(filter: ColumnScaleFilter): Array<FloatArray> {
			val rows = Array(windowSize) { FloatArray(filter.values.size) }
			val buffer = FloatArray(dataCount)
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(filter = filter, destination = rows[i], buffer = buffer)
			}
			return rows
		}

		fun fill2fArray(filter: ColumnScaleFilter, destination: Array<FloatArray>) {
			val buffer = FloatArray(dataCount)
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(filter = filter, destination = destination[i], buffer = buffer)
			}
		}

		fun describe(): String {
			return "<DF window: ${startIndex}:${endIndex}>"
		}
	}

}