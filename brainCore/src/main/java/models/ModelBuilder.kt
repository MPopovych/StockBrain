package models

import layers.*
import utils.*

class ModelBuilder(
	internal val inputs: Map<String, InputLayer>,
	internal val outputs: Map<String, LayerBuilder<*>>,
	internal val debug: Boolean = false,
) {

	constructor(inputLayer: InputLayer, output: LayerBuilder<*>, debug: Boolean = true)
			: this(mapOf(Model.SINGLE_IO to inputLayer), mapOf(Model.SINGLE_IO to output), debug)

	constructor(inputLayer: Map<String, InputLayer>, output: LayerBuilder<*>, debug: Boolean = true)
			: this(inputLayer, mapOf(Model.SINGLE_IO to output), debug)

	constructor(inputLayer: InputLayer, output: Map<String, LayerBuilder<*>>, debug: Boolean = true)
			: this(mapOf(Model.SINGLE_IO to inputLayer), output, debug)

	private val graph = HashMap<LayerBuilder<*>, EmptyGraphNode>()
	internal val reverseQueue = LinkedHashMap<LayerBuilder<*>, Connection>()
	internal val sortedConnections = LinkedHashSet<Connection>()

	init {
		buildNodes()
		buildDownGraph()
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
		currentLayer.ifAlso(debug) {
			printGreen(it)
		}

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
					.also { graph[currentLayer] = it }
					.ifAlso(debug) {
						printYellow(it)
					}
			}
			is LayerBuilder.SingleInput -> {
				// at the stage of implementation this is a single parent
				iterateNodes(currentLayer.parentLayer)
				val parentCon = reverseQueue.getOrPut(currentLayer.parentLayer) { Connection(currentLayer.parentLayer) }
				parentCon.children.add(connection)
				return EmptyGraphNode.SingleParent(currentLayer).also { graph[currentLayer] = it }
			}
			is InputLayer -> {
				return EmptyGraphNode.DeadEnd(currentLayer).also { graph[currentLayer] = it }
			}
			else -> {
				throw IllegalStateException("Bad graph structure")
			}
		}
	}

	private fun buildDownGraph() {
		val queue = ArrayDeque<Connection>()
		this.inputs.values.mapNotNull { reverseQueue[it] }.forEach { root ->
			queue.add(root)
		}
		dequeueNodes(queue)

		val nameSet = HashSet<String>()
		sortedConnections.forEachIndexed { i, con ->
			if (con.parent.name == Layer.DEFAULT_NAME) {
				con.parent.name = "${con.parent.javaClass.simpleName}_${i}"
			}
			if (nameSet.contains(con.parent.name)) {
				throw IllegalStateException("Duplicate name: ${con.parent.name}")
			} else {
				nameSet.add(con.parent.name)
			}
		}
	}

	private fun dequeueNodes(queue: ArrayDeque<Connection>) {
		while (queue.isNotEmpty()) {
			val entry = queue.removeFirst()
			queueNodes(entry, queue)
		}
	}

	private fun queueNodes(con: Connection, queue: ArrayDeque<Connection>) {
		sortedConnections.add(con)
		con.children.forEach { queue.add(it) }
		dequeueNodes(queue)
	}

}

internal class Connection(val parent: LB, val children: HashSet<Connection> = HashSet<Connection>()) {
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
	return "Total layers: ${reverseQueue.size}\n" +
			layerDescription
}

