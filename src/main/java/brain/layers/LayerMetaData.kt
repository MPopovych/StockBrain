package brain.layers

sealed interface LayerMetaData {
	data class DenseMeta(val useBias: Boolean) : LayerMetaData
	data class DirectMeta(val useBias: Boolean) : LayerMetaData
	data class DropoutMeta(val rate: Float) : LayerMetaData
	data class FeatureDenseMeta(val useBias: Boolean) : LayerMetaData
	data class TimeMaskMeta(val fromStart: Int, val fromEnd: Int) : LayerMetaData
	data class FeatureMaskMeta(val allowIndexes: Set<Int>) : LayerMetaData
}