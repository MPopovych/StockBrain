package brain.layers.abs

import brain.abs.DimShape

interface LayerRef {

	val typeName: String
		get() = factory.typeName

	val outputShape: DimShape
	val nodeType: LayerNodeType

	/**
	 * Class for manipulating impl instances (clone, deserialize, serialize)
	 */
	val factory: LayerTypedFactory<*, *>

	/**
	 * Creates a default instance
	 */
	fun createInstance(name: String): LayerPropagationEnum
}

interface InputLayerRef: LayerRef {
	override val nodeType: LayerNodeType.InputIO
}