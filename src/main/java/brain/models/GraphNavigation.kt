package brain.models

import brain.layers.InputLayer
import brain.layers.LB
import brain.layers.Layer
import brain.layers.LayerBuilder
import brain.utils.ifAlsoBr
import brain.utils.printCyanBr
import brain.utils.printYellowBr

internal fun buildLayerNodes(
	nodes: Map<LayerBuilder<*>, GraphBuilderNode>,
	debug: Boolean,
): LinkedHashMap<LayerBuilder<*>, GraphLayerNode> {
	val queue = LinkedHashMap<LayerBuilder<*>, GraphLayerNode>()

	for (node in nodes) {
		iterateLayerNodes(node.key, nodes, queue, debug)
	}
	return queue
}

internal fun iterateLayerNodes(
	curLB: LayerBuilder<*>,
	nodeMap: Map<LayerBuilder<*>, GraphBuilderNode>,
	queue: HashMap<LayerBuilder<*>, GraphLayerNode>,
	debug: Boolean,
): GraphLayerNode {
	val existing = queue[curLB]
	if (existing != null) {
		return existing.ifAlsoBr(debug) {
			printCyanBr("already created $it")
		}
	}
	val currentBuilderNode = nodeMap[curLB] ?: throw IllegalStateException()

	when (curLB) {
		is LayerBuilder.MultiInput -> {
			val parents: List<GraphLayerNode> = curLB.parentLayers.map { builder ->
				iterateLayerNodes(builder, nodeMap, queue, debug)
			}
			val currentLayerImpl = curLB.create() as Layer.MultiInputLayer
			val currentNode = GraphLayerNode.MultiParent(
				currentLayerImpl,
				curLB,
				parents.map { it.layer.name },
				currentBuilderNode.depth
			)
			return currentNode
				.also { queue[curLB] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}

		is LayerBuilder.SingleInput -> {
			// at the stage of implementation this is a single parent
			val p = curLB.parentLayer
			val parentNode = iterateLayerNodes(p, nodeMap, queue, debug)
			val currentLayerImpl = curLB.create() as Layer.SingleInputLayer
			return GraphLayerNode.SingleParent(currentLayerImpl, curLB, parentNode.layer.name, currentBuilderNode.depth)
				.also { queue[curLB] = it }
				.ifAlsoBr(debug) { printYellowBr(it) }
		}

		is InputLayer -> {
			val layer = curLB.create()
			return GraphLayerNode.Input(layer, curLB, currentBuilderNode.depth)
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
	open val builder: LB,
	open val depth: Int,
) {
	class Input(override val layer: Layer.SingleInputLayer, builder: LB, depth: Int) :
		GraphLayerNode(layer, builder, depth)

	class SingleParent(override val layer: Layer.SingleInputLayer, builder: LB, val parentId: String, depth: Int) :
		GraphLayerNode(layer, builder, depth)

	class MultiParent(override val layer: Layer.MultiInputLayer, builder: LB, val parentIds: List<String>, depth: Int) :
		GraphLayerNode(layer, builder, depth)
}

sealed class GraphBuilderNode(open val builder: LayerBuilder<*>, open val depth: Int) {
	class DeadEnd(override val builder: LayerBuilder.DeadEnd<*>, depth: Int) : GraphBuilderNode(builder, depth)
	class SingleParent(override val builder: LayerBuilder.SingleInput<*>, depth: Int) : GraphBuilderNode(builder, depth)

	class MultiParent(override val builder: LayerBuilder.MultiInput<*>, depth: Int) : GraphBuilderNode(builder, depth)
}