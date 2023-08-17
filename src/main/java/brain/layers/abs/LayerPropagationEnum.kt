package brain.layers.abs

sealed interface LayerPropagationEnum {
	class SingleInput(val input: LayerImpl.LayerSingleInput): LayerPropagationEnum
	class MultiInput(val input: LayerImpl.LayerMultiInput): LayerPropagationEnum
}

