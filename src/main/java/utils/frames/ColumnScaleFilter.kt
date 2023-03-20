package utils.frames

import utils.CacheFileProvider
import utils.WorkingDirectoryProvider
import utils.frames.modelframe.FrameAsset
import utils.frames.modelframe.ModelFrame
import utils.frames.modelframe.NamedPropGetter

class ColumnScaleFilter : LinkedHashMap<String, ScaleMeta>() {
	companion object {
		fun load(file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class)
		}

		fun load(path: String, file: String): ColumnScaleFilter {
			return CacheFileProvider.loadJson(file, ColumnScaleFilter::class, path)
		}

		fun load(file: String, wd: WorkingDirectoryProvider): ColumnScaleFilter? {
			if (!wd.checkFileExistsAndReadable(file)) {
				return null
			}
			return wd.loadJson(file, ColumnScaleFilter::class)
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
				if (column.isEmpty()) throw IllegalStateException("Empty array for $key key")
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

	fun <T> ordMapper(with: NamedPropGetter<T>): OrdMapper<T> {
		return OrdMapper.build(this, with)
	}

	class OrdMapper<T>(val ordinals: IntArray, val scales: Array<ScaleMeta>) {
		val size = ordinals.size

		companion object {
			fun <T> build(filter: ColumnScaleFilter, getter: NamedPropGetter<T>): OrdMapper<T> {
				val ordList = ArrayList<Int>()
				val scaleList = ArrayList<ScaleMeta>()
				filter.forEach {
					val ordinal = getter.keyOrdinal[it.key] ?: throw IllegalStateException()
					ordList.add(ordinal)
					scaleList.add(it.value)
				}
				return OrdMapper(ordList.toIntArray(), scaleList.toTypedArray())
			}
		}
	}
}
