package layers

sealed interface LayerMetaData {
	data class Activation(val activation: String): LayerMetaData
	data class Dense(val activation: String?): LayerMetaData
	data class Direct(val activation: String?): LayerMetaData
}