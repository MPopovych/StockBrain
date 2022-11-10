package models

import layers.*
import utils.*

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

	private val graph = LinkedHashMap<LayerBuilder<*>, EmptyGraphNode>()
	internal val reverseQueue = LinkedHashMap<LayerBuilder<*>, Connection>()
	internal val sortedConnections = LinkedHashSet<Connection>()

	init {
		buildNodes()
		processDownGraph()
		// used on init, as a check for structure
		for (input in inputs.values) {
			val root = graph[input] ?: throw IllegalStateException("input layer disconnected")
			if (root !is EmptyGraphNode.DeadEnd) {
				throw IllegalStateException("input layer should be dead end")
			}
		}
		for (output in outputs.values) {
			graph[output] ?: throw IllegalStateException("output layer disconnected")
		}

		if (debug) {
			printCyan("Size: ${graph.size}")
		}
	}

	fun build(debug: Boolean = this.debug): Model {
		return Model(inputs, outputs, debug = debug)
	}

	private fun buildNodes() {
		for (output in outputs.values) {
			iterateNodes(output)
		}
	}

	private fun iterateNodes(
		currentLayer: LayerBuilder<*>,
	): EmptyGraphNode {
		val existing = graph[currentLayer]
		if (existing != null) {
			return existing.ifAlso(debug) {
				printCyan("already created $it")
			}
		}
		val connection = reverseQueue.getOrPut(currentLayer) { Connection(currentLayer) }

		when (currentLayer) {
			is LayerBuilder.MultiInput -> {
				currentLayer.parentLayers.forEach { builder ->
					val parentCon = reverseQueue.getOrPut(builder) { Connection(builder) }
					parentCon.children.add(connection)
					iterateNodes(builder)
				}
				val currentNode = EmptyGraphNode.MultiParent(currentLayer)
				return currentNode
					.also {
						graph[currentLayer] = it
						sortedConnections.add(connection)
					}
					.ifAlso(debug) { printYellow(it) }
			}
			is LayerBuilder.SingleInput -> {
				// at the stage of implementation this is a single parent
				iterateNodes(currentLayer.parentLayer)
				val parentCon = reverseQueue.getOrPut(currentLayer.parentLayer) { Connection(currentLayer.parentLayer) }
				parentCon.children.add(connection)
				return EmptyGraphNode.SingleParent(currentLayer).also {
					graph[currentLayer] = it
					sortedConnections.add(connection)
				}.ifAlso(debug) { printYellow(it) }
			}
			is InputLayer -> {
				return EmptyGraphNode.DeadEnd(currentLayer).also {
					graph[currentLayer] = it
					sortedConnections.add(connection)
				}.ifAlso(debug) { printYellow(it) }
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

