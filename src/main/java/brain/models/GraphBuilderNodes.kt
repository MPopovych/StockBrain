package brain.models

import brain.layers.abs.LayerNodeType

sealed class NamedLayerNodeType(
	open val name: String,
	open val nodeType: LayerNodeType,
) {
	data class InputIO(val ioKey: String, override val name: String, override val nodeType: LayerNodeType) :
		NamedLayerNodeType(name, nodeType)

	data class SingleParent(val parent: String, override val name: String, override val nodeType: LayerNodeType) :
		NamedLayerNodeType(name, nodeType)

	data class MultiParent(val parents: List<String>, override val name: String, override val nodeType: LayerNodeType) :
		NamedLayerNodeType(name, nodeType)
}
