package utils.frames

import utils.CacheFileProvider
import utils.WorkingDirectoryProvider
import utils.frames.modelframe.DataFrameModel
import utils.frames.modelframe.ModelFrame
import utils.math.ScaleDataState
import utils.math.ScaleDataType

class ColumnScaleFilter : HashMap<String, ScaleMeta>() {
	companion object {
		fun load(file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class)
		}

		fun load(path: String, file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class, path)
		}

		fun <T : DataFrameModel> build(
			map: Map<String, ScaleMetaType>,
			modelFrame: ModelFrame<T>
		): ColumnScaleFilter {
			val hashMap = ColumnScaleFilter()
			for ((key, value) in map) {
				val column = modelFrame.getNumberColumn(key) ?: throw IllegalStateException("No such key $key")
				hashMap[key] = value.buildForArray(column)
			}
			return hashMap
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
