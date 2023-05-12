package brain.models

import brain.layers.InputLayerImpl
import brain.layers.LayerTrainableMode
import brain.layers.LayerWarmupMode
import brain.matrix.Matrix


class Model(
	private val builder: ModelBuilder,
	internal var inputByKey: Map<String, GraphLayerNode.Input>,
	internal var outputByKey: Map<String, GraphLayerNode>,
	var graphMap: Map<String, GraphLayerNode>,
	internal val debug: Boolean = false,
	private val check: Boolean = true,
) {

	var propCallback: ((LinkedHashMap<String, Matrix>) -> Unit)? = null

	companion object {
		const val SINGLE_IO = "Default"
	}

	fun setTrainable(trainable: Boolean) {
		for (layer in graphMap.values.map { it.layer }) {
			if (layer is LayerTrainableMode) {
				layer.setTrainable(trainable)
			}
		}
	}

	init {
		onWeightUpdate()
	}

	fun onWeightUpdate() {
		for (layer in graphMap.values.map { it.layer }) {
			layer.onWeightUpdate()
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
			if (inputLayer.layer.getShape().width != entry.value.width
				|| inputLayer.layer.getShape().height != entry.value.height
			) {
				throw IllegalStateException(
					"shape for: ${entry.key} mismatch ${inputLayer.layer.getShape().width} vs ${entry.value.width}" +
							", ${inputLayer.layer.getShape().height} vs ${entry.value.height}"
				)
			}
			val m = check(inputLayer.layer.call(entry.value), inputLayer)
			outputBuffer[inputLayer.layer.name] = m
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
					val outM = check(node.layer.call(mList), node)
					outputBuffer[node.layer.name] = outM
				}

				is GraphLayerNode.SingleParent -> {
					val m = outputBuffer[node.parentId]
						?: throw IllegalStateException("missing: ${node.parentId} for ${node.layer.name}")
					val outM = check(node.layer.call(m), node)
					outputBuffer[node.layer.name] = outM
				}
			}
		}

		propCallback?.also {
			it.invoke(outputBuffer)
		}

		return outputByKey.mapValues {
			val outputGraph = outputByKey[it.key] ?: throw IllegalStateException()
			return@mapValues outputBuffer[outputGraph.layer.name] ?: throw IllegalStateException()
		}
	}

	fun revertToBuilder() = builder

	fun check(matrix: Matrix, layer: GraphLayerNode): Matrix {
		if (check && matrix.values.any { it.any { f -> !f.isFinite() } }) {
			throw IllegalStateException("${layer.layer.name} has NaN")
		}
		return matrix
	}
}


