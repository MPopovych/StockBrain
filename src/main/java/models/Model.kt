package models

import layers.InputLayer
import layers.Layer
import layers.LayerBuilder
import matrix.Matrix
import utils.printYellowBr


class Model(
	internal val originInputs: Map<String, InputLayer>,
	internal val originOutputs: Map<String, LayerBuilder<*>>,
	internal val debug: Boolean = false,
) {
	companion object {
		const val SINGLE_IO = "Default"
	}

	var input: Map<String, GraphBuffer.DeadEnd>
	var output: Map<String, GraphBuffer>

	val nodeGraph = buildBufferNodes(originOutputs.values, debug)
	val layersMap = nodeGraph.values.map { it.layer }.associateBy { it.name }

	init {
		input = originInputs.mapValues { nodeGraph[it.value] as? GraphBuffer.DeadEnd ?: throw IllegalStateException() }
		output = originOutputs.mapValues { nodeGraph[it.value] ?: throw IllegalStateException() }
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
	fun getOutputMap(inputMatrix: Map<String, Matrix>): Map<String, Matrix> {
		val buffer = LinkedHashMap<Layer, Matrix>()

		for (matrix in inputMatrix) {
			val inputLayer = input[matrix.key] ?: throw IllegalStateException()
			buffer[inputLayer.layer] = inputLayer.layer.call(matrix.value)
		}

		return originOutputs.mapValues {
			val outputGraph = output[it.key] ?: throw IllegalStateException()
			iterateOutput(outputGraph, buffer)
			return@mapValues buffer[outputGraph.layer] ?: throw IllegalStateException()
		}
	}

	private fun iterateOutput(bufferNode: GraphBuffer, queue: HashMap<Layer, Matrix>): GraphBuffer {
		if (queue.containsKey(bufferNode.layer)) {
			if (debug) printYellowBr("already found ${bufferNode.layer}")
			return bufferNode
		}

		when (bufferNode) {
			is GraphBuffer.MultiParent -> {
				val matrices = bufferNode.parents.map { parent ->
					queue[parent.layer]
						?: queue[iterateOutput(parent, queue).layer]
						?: throw IllegalStateException()
				}

				val currentOutput = bufferNode.layer.call(matrices)
				return bufferNode.also { queue[bufferNode.layer] = currentOutput }
			}
			is GraphBuffer.SingleParent -> {
				// at the stage of implementation this is a single parent
				val parentMatrix = queue[bufferNode.parent.layer]
					?: queue[iterateOutput(bufferNode.parent, queue).layer]
					?: throw IllegalStateException()

				val currentOutput = bufferNode.layer.call(parentMatrix)
				return bufferNode.also { queue[bufferNode.layer] = currentOutput }
			}
			is GraphBuffer.DeadEnd -> {
				if (!queue.contains(bufferNode.layer)) throw IllegalStateException()
				return bufferNode
			}
			else -> {
				throw IllegalStateException("Bad graph structure")
			}
		}
	}
}

fun Model.revertToBuilder() = ModelBuilder(originInputs, originOutputs, debug)
