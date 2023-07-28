package brain.layers

sealed interface LayerMetaData {
	data class OnlyBiasMeta(val useBias: Boolean) : LayerMetaData
	data class DropoutMeta(val rate: Float) : LayerMetaData
	data class OnlyUnitsMeta(val units: Int) : LayerMetaData

	data class SoftMemoryMeta(val useBias: Boolean, val units: Int, val options: Int) : LayerMetaData
	data class FeatureDenseMeta(val useBias: Boolean, val pivotAvg: Boolean) : LayerMetaData
	data class FeatureConvMeta(
		val useBias: Boolean,
		val units: Int,
		val kernels: Int,
		val step: Int,
		val reverse: Boolean,
	) : LayerMetaData

	data class GRUMeta(
		val units: Int,
		val useBias: Boolean,
		val reverse: Boolean,
		val updateActivation: String?,
		val resetActivation: String?,
	) : LayerMetaData

	data class RNNMeta(val useBias: Boolean, val reverse: Boolean) : LayerMetaData
	data class TimeMaskMeta(val fromStart: Int, val fromEnd: Int) : LayerMetaData
	data class FeatureMaskMeta(val allowIndexes: Set<Int>) : LayerMetaData
}