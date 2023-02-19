package utils.frames.dataframe

import utils.CacheFileProvider
import utils.WorkingDirectoryProvider
import utils.math.*

class DFProcessor : HashMap<String, ScaleDataState>() {
	companion object {
		fun load(file: String): DFProcessor {
			return CacheFileProvider.loadJson(file, DFProcessor::class)
		}

		fun load(path: String, file: String): DFProcessor {
			return CacheFileProvider.loadJson(file, DFProcessor::class, path)
		}

		fun buildScaleOnDF(dataFrame: DataFrame): DFProcessor {
			val processor = DFProcessor()
			for (column in dataFrame.getNumericColumns()) {
				val array = column.data.map { it.toDouble() }
				val scale = column.scale.buildScale(array) { it }
				processor[column.name] = scale
			}
			return processor
		}

		fun buildNPScaleOnDF(dataFrame: DataFrame): DFProcessor {
			val processor = DFProcessor()
			for (column in dataFrame.getNumericColumns()) {
				if (column.scale == ScaleDataType.None) continue

				val array = column.data.map { it.toDouble() }
				val scale = createNPScale(array) { it }
				processor[column.name] = scale
			}
			return processor
		}

		fun buildZPScaleOnDF(dataFrame: DataFrame): DFProcessor {
			val processor = DFProcessor()
			for (column in dataFrame.getNumericColumns()) {
				if (column.scale == ScaleDataType.None) continue

				val array = column.data.map { it.toDouble() }
				val scale = createZPScale(array) { it }
				processor[column.name] = scale
			}
			return processor
		}

		fun buildScaleOnDFList(dataFrames: List<DataFrame>): DFProcessor {
			val first = dataFrames[0] // crash
			val processor = DFProcessor()
			for (column in first.getNumericColumns()) {
				if (column.scale == ScaleDataType.None) continue

				val array = dataFrames.map { df ->
					df.getNumericColumn(column.name)?.data?.map { it.toDouble() } ?: throw IllegalStateException()
				}.flatten()


				val scale = column.scale.buildScale(array) { it }
				processor[column.name] = scale
			}
			return processor
		}
	}

	fun applyToDf(dataFrame: DataFrame) {
		for (column in dataFrame.getNumericColumns()) {
			val scale = this[column.name] ?: continue
			if (column.scale == ScaleDataType.None) continue

			val array = column.data.map { it.toDouble() }
			val modified = scale.scale(array) { it }

			dataFrame.addColumn(column.name, modified.toList().toTypedArray(), scale = column.scale)
		}
	}

	fun save(file: String) {
		CacheFileProvider.save(this, file)
	}

	fun save(file: String, wd: String) {
		CacheFileProvider.save(this, file, path = wd)
	}

	fun save(file: String, wd: WorkingDirectoryProvider) {
		wd.saveJson(this, file)
	}
}