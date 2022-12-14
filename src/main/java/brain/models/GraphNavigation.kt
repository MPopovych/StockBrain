package brain.models

import brain.layers.InputLayer
import brain.layers.Layer
import brain.layers.LayerBuilder
import brain.utils.ifAlsoBr
import brain.utils.printCyanBr
import brain.utils.printYellowBr

internal fun buildBufferNodes(
	nodes: Collection<LayerBuilder<*>>,
	debug: Boolean,
): HashMap<LayerBuilder<*>, GraphBuffer> {
	val queue = LinkedHashMap<LayerBuilder<*>, GraphBuffer>()

	for (node in nodes) {
		iterateBufferNodes(node, queue, debug)
	}
	return queue
}

internal fun iterateBufferNodes(
	currentLayer: LayerBuilder<*>,
	queue: HashMap<LayerBuilder<*>, GraphBuffer>,
	debug: Boolean,
): GraphBuffer {
	val existing = queue[currentLayer]
	if (existing != null) {
		return existing.ifAlsoBr(debug) {
			printCyanBr("already created $it")
		}
	}

	when (currentLayer) {
		is LayerBuilder.MultiInput -> {
			val parents: List<GraphBuffer> = currentLayer.parentLayers.map { builder ->
				iterateBufferNodes(builder, queue, debug)
			}
			val currentLayerImpl = currentLayer.create()
					as Layer.MultiInputLayer
			val currentNode = GraphBuffer.MultiParent(currentLayerImpl, parents)
			return currentNode
				.also { queue[currentLayer] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}
		is LayerBuilder.SingleInput -> {
			// at the stage of implementation this is a single parent
			val p = currentLayer.parentLayer
			val parentNode = iterateBufferNodes(p, queue, debug)
			val currentLayerImpl = currentLayer.create() as Layer.SingleInputLayer
			return GraphBuffer.SingleParent(currentLayerImpl, parentNode)
				.also { queue[currentLayer] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}
		is InputLayer -> {
			val layer = currentLayer.create()
			return GraphBuffer.DeadEnd(layer)
				.also { queue[currentLayer] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}
		else -> {
			throw IllegalStateException("Bad graph structure")
		}
	}
}

sealed class GraphBuffer(
	open val layer: Layer,
) {
	class DeadEnd(override val layer: Layer.SingleInputLayer) : GraphBuffer(layer)
	class SingleParent(override val layer: Layer.SingleInputLayer, val parent: GraphBuffer) : GraphBuffer(layer)
	class MultiParent(override val layer: Layer.MultiInputLayer, val parents: List<GraphBuffer>) : GraphBuffer(layer)
}

sealed class EmptyGraphNode(open val layer: LayerBuilder<*>) {
	class DeadEnd(override val layer: LayerBuilder.DeadEnd<*>) : EmptyGraphNode(layer)
	class SingleParent(override val layer: LayerBuilder.SingleInput<*>) : EmptyGraphNode(layer)

	class MultiParent(override val layer: LayerBuilder.MultiInput<*>) : EmptyGraphNode(layer)
}