package brain.models

import brain.layers.InputLayer
import brain.layers.LB
import brain.layers.Layer
import brain.layers.LayerBuilder
import brain.utils.ifAlsoBr
import brain.utils.printCyanBr
import brain.utils.printYellowBr

internal fun buildLayerNodes(
	nodes: Collection<LayerBuilder<*>>,
	debug: Boolean,
): LinkedHashMap<LayerBuilder<*>, GraphLayerNode> {
	val queue = LinkedHashMap<LayerBuilder<*>, GraphLayerNode>()

	for (node in nodes) {
		iterateLayerNodes(node, queue, debug)
	}
	return queue
}

internal fun iterateLayerNodes(
	curLB: LayerBuilder<*>,
	queue: HashMap<LayerBuilder<*>, GraphLayerNode>,
	debug: Boolean,
): GraphLayerNode {
	val existing = queue[curLB]
	if (existing != null) {
		return existing.ifAlsoBr(debug) {
			printCyanBr("already created $it")
		}
	}

	when (curLB) {
		is LayerBuilder.MultiInput -> {
			val parents: List<GraphLayerNode> = curLB.parentLayers.map { builder ->
				iterateLayerNodes(builder, queue, debug)
			}
			val currentLayerImpl = curLB.create()
					as Layer.MultiInputLayer
			val currentNode = GraphLayerNode.MultiParent(currentLayerImpl, curLB, parents.map { it.layer.name })
			return currentNode
				.also { queue[curLB] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}

		is LayerBuilder.SingleInput -> {
			// at the stage of implementation this is a single parent
			val p = curLB.parentLayer
			val parentNode = iterateLayerNodes(p, queue, debug)
			val currentLayerImpl = curLB.create() as Layer.SingleInputLayer
			return GraphLayerNode.SingleParent(currentLayerImpl, curLB, parentNode.layer.name)
				.also { queue[curLB] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}

		is InputLayer -> {
			val layer = curLB.create()
			return GraphLayerNode.Input(layer, curLB)
				.also { queue[curLB] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}

		else -> {
			throw IllegalStateException("Bad graph structure")
		}
	}
}

sealed class GraphLayerNode(
	open val layer: Layer,
	open val builder: LB
) {
	class Input(override val layer: Layer.SingleInputLayer, builder: LB) : GraphLayerNode(layer, builder)
	class SingleParent(override val layer: Layer.SingleInputLayer, builder: LB, val parentId: String) : GraphLayerNode(layer, builder)
	class MultiParent(override val layer: Layer.MultiInputLayer, builder: LB, val parentIds: List<String>) : GraphLayerNode(layer, builder)
}

sealed class GraphBuilderNode(open val builder: LayerBuilder<*>) {
	class DeadEnd(override val builder: LayerBuilder.DeadEnd<*>) : GraphBuilderNode(builder)
	class SingleParent(override val builder: LayerBuilder.SingleInput<*>) : GraphBuilderNode(builder)

	class MultiParent(override val builder: LayerBuilder.MultiInput<*>) : GraphBuilderNode(builder)
}