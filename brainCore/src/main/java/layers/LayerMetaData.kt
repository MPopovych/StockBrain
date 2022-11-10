package layers

sealed interface LayerMetaData {
	data class DenseMeta(val useBias: Boolean): LayerMetaData
	data class DirectMeta(val useBias: Boolean): LayerMetaData
}