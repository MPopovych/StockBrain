package utils.frames

import utils.frames.dataframe.DataFrame

class ColumnFilter(validColumns: List<String>) {

	constructor(vararg string: String): this(string.toList())

	val values: Set<String> = validColumns.toSet()

	fun applyToDf(dataFrame: DataFrame) {
		val existing = dataFrame.columns().toSet()
		for (column in existing) {
			if (!values.contains(column)) {
				dataFrame.removeColumn(column)
			}
		}
	}

	fun keyFilter(key: String): Boolean {
		return values.contains(key)
	}
}

class DFColumnIndexes(private val indexes: Map<String, Int>) {
	constructor(df: DataFrame, filter: ColumnFilter) : this(
		df.columnsWithFilter(filter).mapIndexed { index, key -> Pair(key, index) }.toMap()
	)

	fun exclude(names: Set<String>): DFColumnIndexes {
		val new = indexes.filterKeys { !names.contains(it) }
		return DFColumnIndexes(new)
	}
	fun exclude(names: List<String>): DFColumnIndexes {
		return exclude(names.toSet())
	}

	fun unionOnly(names: List<String>): DFColumnIndexes {
		return unionOnly(names.toSet())
	}

	fun unionOnly(names: Set<String>): DFColumnIndexes {
		val new = indexes.filterKeys { names.contains(it) }
		return DFColumnIndexes(new)
	}

	fun indexes() = indexes.values.toList()
}