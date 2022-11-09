package models

import layers.*
import utils.*

class ModelBuilder(val inputLayer: InputLayer, val output: LayerBuilder<*>, val debug: Boolean = true) {

	val graph = buildNodes(output)
	init {
		// used on init, as a check for structure
		val root = graph[inputLayer] ?: throw IllegalStateException("input layer disconnected")
		if (root !is EmptyGraphNode.DeadEnd) {
			throw IllegalStateException("input layer should be dead end")
		}
		graph[output] ?: throw IllegalStateException("output layer disconnected")
		if (debug) {
			printCyan("Size: ${graph.size}")
		}
	}

	fun build(debug: Boolean = this.debug): Model {
		return Model(inputLayer, output, debug = debug)
	}

	private fun buildNodes(currentLayer: LayerBuilder<*>): HashMap<LayerBuilder<*>, EmptyGraphNode> {
		val queue = HashMap<LayerBuilder<*>, EmptyGraphNode>()

		iterateNodes(currentLayer, queue)
		return queue
	}

	private fun iterateNodes(currentLayer: LayerBuilder<*>, queue: HashMap<LayerBuilder<*>, EmptyGraphNode>): EmptyGraphNode {
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

