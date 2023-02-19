package utils.frames.dataframe

import utils.frames.ColumnFilter
import utils.frames.DFColumnIndexes
import utils.math.ScaleDataType
import java.io.File
import java.io.PrintWriter



class DataFrame(
	private var data: HashMap<String, DataColumn<*>> = LinkedHashMap()
) {

	private var length: Int

	init {
		val check = data.map { it.value.size }
		length = if (check.isNotEmpty()) {
			val first = check[0]
			for (size in check) {
				if (first != size) {
					throw SymmetryException(first, size)
				}
			}
			first
		} else {
			0
		}
	}

	fun length() = length
	fun columns() = data.keys

	fun getNumericColumns(): List<DataColumn.Numeric> = data.values.mapNotNull { it as? DataColumn.Numeric }
	fun getNumericColumn(key: String): DataColumn.Numeric? = data[key] as? DataColumn.Numeric

	fun columnsWithFilter(filter: ColumnFilter): List<String> {
		return columns().filter { filter.keyFilter(it) }
	}

	fun copy(): DataFrame {
		val dataCopy = data.mapValuesTo(LinkedHashMap()) { it.value.copy() }
		return DataFrame(dataCopy)
	}

	fun columnIndexes(filter: ColumnFilter) = DFColumnIndexes(this, filter)

	fun getProcessorActive(name: String): ScaleDataType? = data[name]?.scale
	fun setProcessorActive(name: String, type: ScaleDataType) {
		val column = data[name] ?: throw BadDataKeyException(name)
		data[name] = column.shallowCopy(scale = type)
	}

	fun addColumn(name: String, values: Array<Any>, scale: ScaleDataType = ScaleDataType.None) {
		if (data.isEmpty()) {
			length = values.size
		}
		if (length != values.size) {
			throw SymmetryException(length, values.size)
		}
		val column: DataColumn<*> = if (values.isArrayOf<Number>()) {
			val cast = values.filterIsInstance<Number>().toTypedArray()
			if (cast.size != length) throw SymmetryException(cast.size, length)
			DataColumn.Numeric(name, scale, cast)
		} else {
			DataColumn.Generic(name, scale, values)
		}
		data[name] = column
	}

	fun removeColumn(name: String) {
		data.remove(name)
	}

	fun addRow(row: Map<String, Any>) {
		if (data.isNotEmpty() && row.size != data.size) {
			throw SymmetryException(row.size, data.size)
		}
		for (entry in row) {
			if (!data.containsKey(entry.key)) {
				data[entry.key] = DataColumn.ofSingleValue(entry.key, ScaleDataType.None, entry.value)
			}
			data[entry.key] = data[entry.key]?.copyWithNew(entry.value) ?: throw BadDataException(entry.value, entry.key)
		}
		length++
	}

	fun takeLast(last: Int): DataFrame {
		return copyDf { list -> list.takeLast(last).toTypedArray() }
	}

	fun takeFirst(first: Int): DataFrame {
		return copyDf { list -> list.take(first).toTypedArray() }
	}

	fun dropLast(last: Int): DataFrame {
		return copyDf { list -> list.dropLast(last).toTypedArray() }
	}

	fun dropFirst(last: Int): DataFrame {
		return copyDf { list -> list.drop(last).toTypedArray() }
	}

	private fun copyDf(block: ((Array<*>) -> Array<*>)): DataFrame {
		val newData = LinkedHashMap<String, DataColumn<*>>()
		data.forEach { (t, u) ->
			newData[t] = u.lambdaData(block)
		}

		return DataFrame(newData)
	}

	fun iterateRows(from: Int = 0, to: Int = length - 1, block: (Int, List<Any?>) -> Unit) {
		for (i in from..to) {
			block(i, data.values.map { it.data[i] })
		}
	}

	fun iterateRows(
		from: Int = 0,
		to: Int = length - 1,
		columnFilter: ColumnFilter,
		block: (Int, List<Any?>) -> Unit,
	) {
		for (i in from..to) {
			block(i, data.filterKeys { columnFilter.keyFilter(it) }.values.map { it.data[i] })
		}
	}

	fun findNaN() {
		data.mapNotNull { it.value as? DataColumn.Numeric }.forEach { t ->
			t.data.forEachIndexed { i, it ->
				if (it is Double && it.isNaN()) {
					throw Exception("key: ${t.name}, index: $i is NaN")
				} else if (it is Float && it.isNaN()) {
					throw Exception("key: ${t.name}, index: $i is NaN")
				}
			}
		}
	}

	fun hasNaN(): Boolean {
		data.mapNotNull { it.value as? DataColumn.Numeric }.forEach { t ->
			t.data.forEachIndexed { _, it ->
				if (it is Double && it.isNaN()) {
					return true
				} else if (it is Float && it.isNaN()) {
					return true
				}
			}
		}
		return false
	}

	fun getHeadString(): String {
		var buffer = columns().joinToString(",") + "\n"
		this.takeFirst(1).iterateRows { _, data ->
			buffer += data.joinToString(",") + "\n"
		}
		return buffer
	}

	fun saveCSV(path: String, fileName: String) {
		val pathEnd = if (path.endsWith("/")) path else "$path/"

		//creating dir if not exist
		val dirPath = File(if (pathEnd.contains("/")) pathEnd.substring(0, pathEnd.lastIndexOf("/")) else "")
		if (!dirPath.exists())
			dirPath.mkdirs()
		val file = File("$pathEnd$fileName.csv")
		if (file.exists()) {
			file.delete()
		}
		val writer = PrintWriter(File("$pathEnd$fileName.csv"))
		writer.append(columns().joinToString(",") + "\n")
		iterateRows { _, items ->
			writer.append(items.joinToString(",") + "\n")
		}
		writer.close()
	}

	fun to2dArray(): Array<DoubleArray> {
		val rows = ArrayList<DoubleArray>()
		iterateRows { _, anyList ->
			rows.add(anyList.map { it as Double }.toDoubleArray())
		}
		return rows.toTypedArray()
	}

	fun to2fArray(): Array<FloatArray> {
		val rows = ArrayList<FloatArray>()
		iterateRows { _, anyList ->
			rows.add(anyList.map { it as Float }.toFloatArray())
		}
		return rows.toTypedArray()
	}

	// does not allow "padded" windows, only full ones
	// window size -> length of final array
	// gap size N -> skips N-1 elements of original data
	fun window(size: Int, gapSize: Int = 1): List<WindowScope> {
		if (size < 2 && gapSize > 1) throw Exception("window size can't be less then 2 when gapsize is greater than 1")
		val fullWindowSize = (size - 1) * gapSize + 1
		if (length < fullWindowSize) {
			throw IllegalStateException("Dataframe is smaller then the window")
		}

		return (0 .. length - fullWindowSize).mapIndexed { index, i ->
			WindowScope(parent = this, startIndex = index, endIndex = index + fullWindowSize - 1, gapSize = gapSize)
		}
	}

	class WindowScope(
		private val parent: DataFrame,
		private val startIndex: Int,
		private val endIndex: Int,
		private val gapSize: Int
	) {

		init {
			if (startIndex > endIndex) throw IllegalStateException("Start is greater than end")
		}

		val size = endIndex - startIndex

		fun getHeadString(): String {
			var buffer = parent.columns().joinToString(",") + "\n"
			buffer += parent.data.values.map { it.data[startIndex] }.joinToString(",") + "\n"
			return buffer
		}


		fun to2fArray(): Array<FloatArray> {
			val rows = ArrayList<FloatArray>()
			parent.iterateRows(startIndex, endIndex) { i, anyList ->
				val relativeIndex = startIndex - i
				if (relativeIndex % gapSize != 0) return@iterateRows
				rows.add(anyList.mapNotNull { (it as? Double)?.toFloat() }.toFloatArray())
			}
			return rows.toTypedArray()
		}

		fun to2fArray(filter: ColumnFilter): Array<FloatArray> {
			val rows = ArrayList<FloatArray>()
			parent.iterateRows(startIndex, endIndex, filter) { i, anyList ->
				val relativeIndex = startIndex - i
				if (relativeIndex % gapSize != 0) return@iterateRows
				rows.add(anyList.mapNotNull { (it as? Double)?.toFloat() }.toFloatArray())
			}
			return rows.toTypedArray()
		}

		fun getFromLast(key: String): Any? {
			return parent.data[key]?.data?.getOrNull(endIndex)
		}

		fun getFromNext(key: String): Any? {
			return parent.data[key]?.data?.getOrNull(endIndex + 1)
		}

		fun describe(): String {
			return "<DF window: ${startIndex}:${endIndex}>"
		}
	}

}