package brain.models

import brain.layers.InputLayer
import brain.layers.LB
import brain.layers.Layer
import brain.layers.LayerBuilder
import brain.utils.ifAlsoBr
import brain.utils.printCyanBr
import brain.utils.printYellowBr

class ModelBuilder(
	internal val inputs: Map<String, InputLayer>,
	internal val outputs: Map<String, LayerBuilder<*>>,
	private val debug: Boolean = false,
) {

	constructor(input: InputLayer, output: LayerBuilder<*>, debug: Boolean = false)
			: this(mapOf(Model.SINGLE_IO to input), mapOf(Model.SINGLE_IO to output), debug)

	constructor(inputs: Map<String, InputLayer>, output: LayerBuilder<*>, debug: Boolean = false)
			: this(inputs, mapOf(Model.SINGLE_IO to output), debug)

	constructor(input: InputLayer, outputs: Map<String, LayerBuilder<*>>, debug: Boolean = false)
			: this(mapOf(Model.SINGLE_IO to input), outputs, debug)

	private val graph = LinkedHashMap<LayerBuilder<*>, GraphBuilderNode>()
	internal val reverseQueue = LinkedHashMap<LayerBuilder<*>, Connection>()
	internal val sortedConnections = LinkedHashSet<Connection>()

	init {
		buildNodes()
		processDownGraph()
		// used on init, as a check for structure
		for (input in inputs.values) {
			val root = graph[input] ?: throw IllegalStateException("input layer disconnected ${input.name}")
			if (root !is GraphBuilderNode.DeadEnd) {
				throw IllegalStateException("input layer should be dead end")
			}
		}
		for (output in outputs.values) {
			graph[output] ?: throw IllegalStateException("output layer disconnected")
		}

		if (debug) {
			printCyanBr("Size: ${graph.size}")
		}
	}

	fun build(debug: Boolean = this.debug): Model {
		val graphMap = buildLayerNodes(graph, debug).mapKeys { it.value.layer.name }

		val inputMapByKey = inputs
			.mapValues {
				val node = graphMap[it.value.name]
				node as? GraphLayerNode.Input
					?: throw IllegalStateException("Expected ${GraphLayerNode.Input::class.simpleName} at key ${it.value.name}, got: $node")
			}
		val outputMapByKey = outputs
			.mapValues {
				val node = graphMap[it.value.name]
				node
					?: throw IllegalStateException("Expected ${GraphLayerNode::class.simpleName} at key ${it.value.name}, got: null")
			}

		return Model(this, inputMapByKey, outputMapByKey, graphMap, debug = debug)
	}

	private fun buildNodes() {
		for (output in outputs.values) {
			iterateNodes(output, 0)
		}
	}

	private fun iterateNodes(
		currentLayer: LayerBuilder<*>,
		depth: Int,
	): GraphBuilderNode {
		val existing = graph[currentLayer]
		if (existing != null) {
			return existing.ifAlsoBr(debug) {
				printCyanBr("already created $it")
			}
		}
		val connection = reverseQueue.getOrPut(currentLayer) { Connection(currentLayer) }

		when (currentLayer) {
			is LayerBuilder.MultiInput -> {
				currentLayer.parentLayers.forEach { builder ->
					val parentCon = reverseQueue.getOrPut(builder) { Connection(builder) }
					parentCon.children.add(connection)
					iterateNodes(builder, depth + 1)
				}
				val currentNode = GraphBuilderNode.MultiParent(currentLayer, depth)
				return currentNode
					.also {
						graph[currentLayer] = it
						sortedConnections.add(connection)
					}
					.ifAlsoBr(debug) { printYellowBr(it) }
			}

			is LayerBuilder.SingleInput -> {
				// at the stage of implementation this is a single parent
				iterateNodes(currentLayer.parentLayer, depth + 1)
				val parentCon = reverseQueue.getOrPut(currentLayer.parentLayer) { Connection(currentLayer.parentLayer) }
				parentCon.children.add(connection)
				return GraphBuilderNode.SingleParent(currentLayer, depth).also {
					graph[currentLayer] = it
					sortedConnections.add(connection)
				}.ifAlsoBr(debug) { printYellowBr(it) }
			}

			is InputLayer -> {
				return GraphBuilderNode.DeadEnd(currentLayer, depth).also {
					graph[currentLayer] = it
					sortedConnections.add(connection)
				}.ifAlsoBr(debug) { printYellowBr(it) }
			}

			else -> {
				throw IllegalStateException("Bad graph structure")
			}
		}
	}

	private fun processDownGraph() {
		val nameSet = HashSet<String>()
		sortedConnections.forEachIndexed { i, con ->
			if (con.parent.name == Layer.DEFAULT_NAME) {
				con.parent.name = "${con.parent.nameType}_${i}"
			}
			if (nameSet.contains(con.parent.name)) {
				throw IllegalStateException("Duplicate name: ${con.parent.name}")
			} else {
				nameSet.add(con.parent.name)
			}
		}
	}

}

internal class Connection(val parent: LB, val children: HashSet<Connection> = HashSet()) {
	fun describe(): String {
		return "${parent.name} : " +
				"${parent.getShape()} : " +
				"children: ${children.size}"
	}
}

fun ModelBuilder.summary(): String {
	val layerDescription = sortedConnections
		.joinToString("\n") { con ->
			con.describe()
		}
	return "Total layers: ${reverseQueue.size} : inputs: ${inputs.keys}, outputs: ${outputs.keys}\n" +
			layerDescription
}

