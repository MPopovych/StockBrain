package utils.frames.modelframe

import brain.utils.printYellowBr
import utils.frames.ColumnScaleFilter


class ModelFrame<T : FrameAsset> : ArrayList<T>(), WindowProvider<T> {
	companion object {
		fun <A : FrameAsset> from(source: Iterable<A>): ModelFrame<A> {
			return ModelFrame<A>().also { it.addAll(source) }
		}
	}

	private fun ensureDataSize() = firstOrNull()?.describeDataCount ?: throw IllegalStateException("Ensure failed")

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
	fun windowList(windowSize: Int, gapSize: Int = 1): List<WindowScope<T>> {
		if (windowSize < 2 && gapSize > 1) throw Exception("Window size can't be less then 2 when gapsize is greater than 1")
		val fullWindowSize = (windowSize - 1) * gapSize + 1
		if (size < fullWindowSize) {
			throw IllegalStateException("ModelFrame is smaller then the window")
		}
		val dataCount = ensureDataSize()

		return (0 .. size - windowSize).mapIndexed { index, i ->
			WindowScope(
				parent = this,
				windowSize = windowSize,
				dataCount = dataCount,
				startIndex = index, // inclusive
				endIndex = index + fullWindowSize - 1,  // inclusive
				gapSize = gapSize
			)
		}
	}

	override fun getBackWindow(index: Int, windowSize: Int, gapSize: Int): WindowScope<T>? {
		if (windowSize < 2 && gapSize > 1) throw Exception("Window size can't be less then 2 when gapsize is greater than 1")
		val fullWindowSize = (windowSize - 1) * gapSize + 1

		if (size < fullWindowSize) {
			throw IllegalStateException("ModelFrame is smaller then the window")
		}
		val startIndex = index - fullWindowSize + 1
		if (startIndex < 0) return null // there is no window

		val dataCount = first().describeDataCount

		return WindowScope(
			parent = this,
			windowSize = windowSize,
			dataCount = dataCount,
			startIndex = startIndex,  // inclusive
			endIndex = index,  // inclusive
			gapSize = gapSize
		)
	}

	class WindowScope<G : FrameAsset>(
		private val parent: ModelFrame<G>,
		private val windowSize: Int, // vertical
		private val dataCount: Int, // horizontal
		private val startIndex: Int, // inclusive
		private val endIndex: Int, // inclusive
		private val gapSize: Int
	) : FrameWindow<G> {

		init {
			if (startIndex > endIndex) throw IllegalStateException("Start is greater than end")
		}

		fun iterate(block: (relative: Int, data: G) -> Unit) {
			var i = 0
			for (index in startIndex..endIndex step gapSize) {
				block(i++, parent[index])
			}
		}

		fun toList(): List<G> {
			return (startIndex..endIndex step gapSize).map { index ->
				return@map parent[index]
			}
		}

		fun absoluteIndexList() = (startIndex..endIndex step gapSize).toList()

		fun getHeadString(): String {
			val first = parent.getOrNull(startIndex) ?: return "[EmptyWindowScope]"
			var buffer = first.describeHeader.joinToString(",") + "\n"
			buffer += first.to2FArray().joinToString(",") + "\n"
			return buffer
		}

		override fun to2fArray(): Array<FloatArray> {
			return Array(windowSize) { FloatArray(dataCount) }.also {
				fill2fArray(it)
			}
		}

		override fun to2fArray(filter: ColumnScaleFilter): Array<FloatArray> {
			return Array(windowSize) { FloatArray(filter.size) }.also {
				fill2fArray(it, filter)
			}
		}

		override fun to2fArray(mapper: ColumnScaleFilter.OrdMapper<G>): Array<FloatArray> {
			return Array(windowSize) { FloatArray(mapper.size) }.also {
				fill2fArray(it, mapper)
			}
		}

		override fun fill2fArray(destination: Array<FloatArray>): Array<FloatArray> {
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(destination[i])
			}
			return destination
		}

		override fun fill2fArray(destination: Array<FloatArray>, filter: ColumnScaleFilter): Array<FloatArray> {
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(destination[i], filter)
			}
			return destination
		}

		override fun fill2fArray(destination: Array<FloatArray>, mapper: ColumnScaleFilter.OrdMapper<G>): Array<FloatArray> {
			iterate { i, modelFrameEntry ->
				modelFrameEntry.fill2FArray(destination[i], mapper)
			}
			return destination
		}

		fun describe(): String {
			return "<DF window: ${startIndex}:${endIndex}>"
		}
	}

}