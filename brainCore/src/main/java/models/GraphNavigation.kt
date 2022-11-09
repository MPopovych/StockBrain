package models

import layers.InputLayer
import layers.Layer
import layers.LayerBuilder
import matrix.Matrix
import utils.ifAlso
import utils.printCyan
import utils.printGreen
import utils.printYellow

internal fun buildNodes(currentLayer: LayerBuilder<*>, debug: Boolean): HashMap<LayerBuilder<*>, GraphBuffer> {
	val queue = HashMap<LayerBuilder<*>, GraphBuffer>()

	iterateNodes(currentLayer, queue, debug)
	return queue
}

internal fun iterateNodes(
	currentLayer: LayerBuilder<*>,
	queue: HashMap<LayerBuilder<*>, GraphBuffer>,
	debug: Boolean,
): GraphBuffer {
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
			val parents: List<GraphBuffer> = currentLayer.parentLayers.map { builder ->
				iterateNodes(builder, queue, debug)
			}
			val currentLayerImpl = currentLayer.createFromList(parents.map { p -> p.layer.getShape() })
					as Layer.MultiInputLayer
			val currentNode = GraphBuffer.MultiParent(currentLayerImpl, parents)
			return currentNode
				.also { queue[currentLayer] = it }
				.ifAlso(debug) {
					printYellow(it)
				}
		}
		is LayerBuilder.SingleInput -> {
			// at the stage of implementation this is a single parent
			val p = currentLayer.parentLayer
			val parentNode = iterateNodes(p, queue, debug)
			val currentLayerImpl = currentLayer.createFrom(parentNode.layer.getShape()) as Layer.SingleInputLayer
			return GraphBuffer.SingleParent(currentLayerImpl, parentNode).also { queue[currentLayer] = it }
		}
		is InputLayer -> {
			val layer = currentLayer.create()
			return GraphBuffer.DeadEnd(layer).also { queue[currentLayer] = it }
		}
		else -> {
			throw IllegalStateException("Bad graph structure")
		}
	}
}

//sealed class GraphNode(var layer: Layer) {
//	class DeadEnd(layer: Layer) : GraphNode(layer)
//	class SingleParent(layer: Layer, val parent: GraphNode) : GraphNode(layer)
//	class MultiParent(layer: Layer, val parent: List<GraphNode>) : GraphNode(layer)
//}

sealed class GraphBuffer(open val layer: Layer, var buffer: Matrix? = null) {
	class DeadEnd(override val layer: Layer.SingleInputLayer) : GraphBuffer(layer)
	class SingleParent(override val layer: Layer.SingleInputLayer, val parent: GraphBuffer) : GraphBuffer(layer)
	class MultiParent(override val layer: Layer.MultiInputLayer, val parents: List<GraphBuffer>) : GraphBuffer(layer)

	fun flush() {
		buffer = null
	}
}

sealed class EmptyGraphNode(open val layer: LayerBuilder<*>) {
	class DeadEnd(override val layer: LayerBuilder.DeadEnd<*>) : EmptyGraphNode(layer)
	class SingleParent(override val layer: LayerBuilder.SingleInput<*>, val parent: EmptyGraphNode) : EmptyGraphNode(layer)
	class MultiParent(override val layer: LayerBuilder.MultiInput<*>, val parents: List<EmptyGraphNode>) : EmptyGraphNode(layer)
}