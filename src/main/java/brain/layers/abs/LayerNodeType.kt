package brain.layers.abs

sealed interface LayerNodeType {
	object InputIO: LayerNodeType
	class SingleParent(val parent: LayerRef): LayerNodeType
	class MultiParent(val parents: List<LayerRef>): LayerNodeType
}
