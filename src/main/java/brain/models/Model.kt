package brain.models

import brain.layers.*
import brain.matrix.Matrix
import brain.utils.printYellowBr


class Model(
	private val builder: ModelBuilder,
	internal var inputByKey: Map<String, GraphLayerNode.Input>,
	internal var outputByKey: Map<String, GraphLayerNode>,
	var graphMap: Map<String, GraphLayerNode>,
	internal val debug: Boolean = false,
) {
	companion object {
		const val SINGLE_IO = "Default"
	}

	fun setTrainable(trainable: Boolean) {
		for (layer in graphMap.values) {
			if (layer is LayerTrainableMode) {
				layer.setTrainable(trainable)
			}
		}
	}

	fun warmup() {
		for (layer in graphMap.values) {
			if (layer is LayerWarmupMode) {
				layer.warmup()
			}
		}
	}

	// single
	fun getOutput(inputMatrix: Matrix): Matrix {
		return getOutputMap(mapOf(SINGLE_IO to inputMatrix))[SINGLE_IO] ?: throw IllegalStateException()
	}

	fun getOutput(inputMatrix: Map<String, Matrix>): Matrix {
		return getOutputMap(inputMatrix)[SINGLE_IO] ?: throw IllegalStateException()
	}

	fun getOutputMap(inputMatrix: Matrix): Map<String, Matrix> {
		return getOutputMap(mapOf(SINGLE_IO to inputMatrix))
	}

	// map
	fun getOutputMap(inputMatrixMap: Map<String, Matrix>): Map<String, Matrix> {
		val outputBuffer = LinkedHashMap<String, Matrix>()

		for (entry in inputMatrixMap) {
			val inputLayer = inputByKey[entry.key] ?: throw IllegalStateException()
			outputBuffer[inputLayer.layer.name] = inputLayer.layer.call(entry.value)
		}

		for (node in graphMap.values) {
			when (node) {
				is GraphLayerNode.Input -> {
					if (node.layer is InputLayerImpl) continue
					else throw IllegalStateException("Invalid node type: $node")
				}
				is GraphLayerNode.MultiParent -> {
					val mList = node.parentIds.map { id ->
						outputBuffer[id]
							?: throw IllegalStateException("missing: $id for ${node.layer.name}")
					}
					val outM = node.layer.call(mList)
					outputBuffer[node.layer.name] = outM
				}
				is GraphLayerNode.SingleParent -> {
					val m = outputBuffer[node.parentId] ?: throw IllegalStateException("missing: ${node.parentId} for ${node.layer.name}")
					val outM = node.layer.call(m)
					outputBuffer[node.layer.name] = outM
				}
			}
		}

		return outputByKey.mapValues {
			val outputGraph = outputByKey[it.key] ?: throw IllegalStateException()
			return@mapValues outputBuffer[outputGraph.layer.name] ?: throw IllegalStateException()
		}
	}

	fun revertToBuilder() = builder
}


