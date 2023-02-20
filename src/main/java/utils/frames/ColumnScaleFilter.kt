package utils.frames

import utils.CacheFileProvider
import utils.WorkingDirectoryProvider
import utils.frames.modelframe.FrameAsset
import utils.frames.modelframe.ModelFrame

class ColumnScaleFilter : LinkedHashMap<String, ScaleMeta>() {
	companion object {
		fun load(file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class)
		}

		fun load(path: String, file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class, path)
		}

		/** all fields are valid, all of them are None-scaled */
		fun <T : FrameAsset> blankOf(modelFrame: ModelFrame<T>): ColumnScaleFilter {
			if (modelFrame.isEmpty()) throw IllegalStateException("Can't build from an empty model")
			val noneScaleMap = modelFrame.first().describeHeader.associateWith {
				ScaleMetaType.None
			}
			return byTypeMap(modelFrame, noneScaleMap)
		}

		fun <T : FrameAsset> byTypeMap(
			modelFrame: ModelFrame<T>,
			map: Map<String, ScaleMetaType>
		): ColumnScaleFilter {
			val scales = HashMap<String, ScaleMeta>()
			for ((key, type) in map) {
				val column = modelFrame.getNumberColumn(key)
					?: throw IllegalStateException("No $key in ${modelFrame.getHeadString()}")
				scales[key] = type.buildForArray(column)
			}

			val firstItem = modelFrame.firstOrNull()
				?: throw IllegalStateException("Can't build from an empty model")

			val sortedByDeclaration = firstItem.describeHeader
				.mapNotNull { key ->
					val meta = scales[key] ?: return@mapNotNull null
					return@mapNotNull Pair(key, meta)
				}.toMap()

			return ColumnScaleFilter().also {
				it.putAll(sortedByDeclaration)
			}
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
