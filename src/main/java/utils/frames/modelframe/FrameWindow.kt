package utils.frames.modelframe

import utils.frames.ColumnScaleFilter

interface WindowProvider<G> {
	fun getBackWindow(index: Int, windowSize: Int, gapSize: Int = 1): FrameWindow<G>?
}

interface FrameWindow<G> {
	fun to2fArray(): Array<FloatArray>
	fun to2fArray(filter: ColumnScaleFilter): Array<FloatArray>
	fun to2fArray(mapper: ColumnScaleFilter.OrdMapper<G>): Array<FloatArray>
	fun fill2fArray(destination: Array<FloatArray>): Array<FloatArray>
	fun fill2fArray(destination: Array<FloatArray>, filter: ColumnScaleFilter): Array<FloatArray>
	fun fill2fArray(destination: Array<FloatArray>, mapper: ColumnScaleFilter.OrdMapper<G>): Array<FloatArray>
}