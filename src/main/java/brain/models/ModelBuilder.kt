package brain.models

import brain.layers.abs.InputLayerRef
import brain.layers.abs.LayerNodeType
import brain.layers.abs.LayerPropagationEnum
import brain.layers.abs.LayerRef

class ModelBuilder(
	internal val inputs: Map<String, InputLayerRef>,
	internal val outputs: Map<String, LayerRef>,
	private val debug: Boolean = false,
) {

	constructor(input: InputLayerRef, output: LayerRef, debug: Boolean = false)
			: this(mapOf(Model.DEFAULT_INPUT to input), mapOf(Model.DEFAULT_OUTPUT to output), debug)

	constructor(inputs: Map<String, InputLayerRef>, output: LayerRef, debug: Boolean = false)
			: this(inputs, mapOf(Model.DEFAULT_OUTPUT to output), debug)

	constructor(input: InputLayerRef, outputs: Map<String, LayerRef>, debug: Boolean = false)
			: this(mapOf(Model.DEFAULT_INPUT to input), outputs, debug)

	internal val graph = LinkedHashMap<LayerRef, NamedLayerNodeType>()

	init {
		initBuildNodes(graph, outputs)

		for (output in outputs.values) {
			graph[output] ?: throw IllegalStateException("output layer disconnected '${output}'")
		}
	}

	fun build(debug: Boolean = this.debug): Model {
		val nodes = graph.map { entry ->
			when (val node = entry.value) {
				is NamedLayerNodeType.InputIO -> {
					val safe = when (val instance = entry.key.createInstance(entry.value.name)) {
						is LayerPropagationEnum.MultiInput -> throw IllegalStateException("Unsupported multi input")
						is LayerPropagationEnum.SingleInput -> instance
					}
					GraphNode(type = GraphNodeType.InputIO(node.ioKey, safe.input))
				}

				is NamedLayerNodeType.MultiParent -> {
					val safe = when (val instance = entry.key.createInstance(entry.value.name)) {
						is LayerPropagationEnum.MultiInput -> instance
						is LayerPropagationEnum.SingleInput -> throw IllegalStateException("Single input in place of multi")
					}
					GraphNode(type = GraphNodeType.MultiParent(node.parents, safe.input))
				}

				is NamedLayerNodeType.SingleParent -> {
					val safe = when (val instance = entry.key.createInstance(entry.value.name)) {
						is LayerPropagationEnum.MultiInput -> throw IllegalStateException("Multi input in place of single")
						is LayerPropagationEnum.SingleInput -> instance
					}
					GraphNode(type = GraphNodeType.SingleParent(node.parent, safe.input))
				}
			}
		}

		val outputMapByKey = outputs.mapValues {
			val ref = graph[it.value] ?: throw IllegalStateException("Disconnected output for: ${it.key}")
			ref.name
		}

		return Model(outputMapByKey, nodes, debug = debug)
	}

	private fun initBuildNodes(
		graph: LinkedHashMap<LayerRef, NamedLayerNodeType>,
		outputs: Map<String, LayerRef>,
	) {
		for (currentLayer in outputs.values) {
			initIterateNodes(graph, currentLayer)
		}
	}

	private fun initIterateNodes(
		graph: LinkedHashMap<LayerRef, NamedLayerNodeType>,
		currentLayer: LayerRef,
	): NamedLayerNodeType {
		val existing = graph[currentLayer]
		if (existing != null) {
			return existing
		}

		val namedNode = when (val node = currentLayer.nodeType) {
			LayerNodeType.InputIO -> {
				val name = "${currentLayer.typeName}_${graph.size}"
				val ioKey = inputs.entries.find {
					it.value == currentLayer
				}?.key ?: throw IllegalStateException("Missing ref for input IO $name")
				NamedLayerNodeType.InputIO(ioKey = ioKey, name, node)
			}

			is LayerNodeType.SingleParent -> {
				val matchedParent = initIterateNodes(graph, node.parent)
				val name = "${currentLayer.typeName}_${graph.size}"
				NamedLayerNodeType.SingleParent(parent = matchedParent.name, name, node)
			}

			is LayerNodeType.MultiParent -> {
				val matchedParents = node.parents.map { parent ->
					initIterateNodes(graph, parent)
				}.map { it.name }
				val name = "${currentLayer.typeName}_${graph.size}"
				NamedLayerNodeType.MultiParent(parents = matchedParents, name, node)
			}

		}
		graph[currentLayer] = namedNode
		return namedNode
	}

}

fun ModelBuilder.summary(): String {
	val layerDescription = graph
		.map {
			"[${it.value.name}] - ${it.key.typeName} ${it.key.outputShape.format()}"
		}
		.joinToString("\n")
	return "Total layers: ${graph.size} : inputs: ${inputs.keys}, outputs: ${outputs.keys}\n" +
			layerDescription
}

