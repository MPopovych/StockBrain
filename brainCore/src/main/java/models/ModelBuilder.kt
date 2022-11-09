package models

import layers.*
import utils.*

class ModelBuilder(
	val inputs: Map<String, InputLayer>,
	val outputs: Map<String, LayerBuilder<*>>,
	val debug: Boolean = true,
) {

	constructor(inputLayer: InputLayer, output: LayerBuilder<*>, debug: Boolean = true)
			: this(mapOf(Model.SINGLE_IO to inputLayer), mapOf(Model.SINGLE_IO to output), debug)

	constructor(inputLayer: Map<String, InputLayer>, output: LayerBuilder<*>, debug: Boolean = true)
			: this(inputLayer, mapOf(Model.SINGLE_IO to output), debug)

	constructor(inputLayer: InputLayer, output: Map<String, LayerBuilder<*>>, debug: Boolean = true)
			: this(mapOf(Model.SINGLE_IO to inputLayer), output, debug)

	private val graph = buildNodes()

	init {
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

	private fun buildNodes(): HashMap<LayerBuilder<*>, EmptyGraphNode> {
		val queue = HashMap<LayerBuilder<*>, EmptyGraphNode>()

		for (output in outputs.values) {
			iterateNodes(output, queue)
		}
		return queue
	}

	private fun iterateNodes(
		currentLayer: LayerBuilder<*>,
		queue: HashMap<LayerBuilder<*>, EmptyGraphNode>,
	): EmptyGraphNode {
		currentLayer.ifAlso(debug) {
			printGreen(it)
		}

		val existing = queue[currentLayer]
		if (existing != null) {
			return existing.ifAlso(debug) {
				printCyan("already created $it")
			}
		}

		when (currentLayer) {
			is LayerBuilder.MultiInput -> {
				val parents: List<EmptyGraphNode> = currentLayer.parentLayers.map { builder ->
					iterateNodes(builder, queue)
				}
				val currentNode = EmptyGraphNode.MultiParent(currentLayer, parents)
				return currentNode
					.also { queue[currentLayer] = it }
					.ifAlso(debug) {
						printYellow(it)
					}
			}
			is LayerBuilder.SingleInput -> {
				// at the stage of implementation this is a single parent
				val p = currentLayer.parentLayer
				val parentNode = iterateNodes(p, queue)
				return EmptyGraphNode.SingleParent(currentLayer, parentNode).also { queue[currentLayer] = it }
			}
			is InputLayer -> {
				return EmptyGraphNode.DeadEnd(currentLayer).also { queue[currentLayer] = it }
			}
			else -> {
				throw IllegalStateException("Bad graph structure")
			}
		}
	}

}

