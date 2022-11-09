package models

import layers.InputLayer
import layers.Layer
import layers.LayerBuilder
import matrix.Matrix


class Model(
	internal val originInputs: Map<String, InputLayer>,
	internal val originOutputs: Map<String, LayerBuilder<*>>,
	internal val debug: Boolean = true,
) {

	companion object {
		const val SINGLE_IO = "S"
	}

	private var input: Map<String, GraphBuffer.DeadEnd>
	private var output: Map<String, GraphBuffer>

	private val nodeGraph = buildNodes(originOutputs.values, debug)
	private val graphBuffer = nodeGraph.values
	val layers = nodeGraph.values.map { it.layer }

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
		graphBuffer.forEach { it.flush() }
		val buffer = HashMap<Layer, Matrix>()

		for (matrix in inputMatrix) {
			val inputLayer = input[matrix.key] ?: throw IllegalStateException()
			buffer[inputLayer.layer] = matrix.value
		}

		return originOutputs.mapValues {
			val outputGraph = output[it.key] ?: throw IllegalStateException()
			return@mapValues (iterateOutput(outputGraph, buffer) as GraphBuffer.SingleParent).buffer
				?: throw IllegalStateException()
		}
	}

	private fun iterateOutput(bufferNode: GraphBuffer, queue: HashMap<Layer, Matrix>): GraphBuffer {
		val existing = queue[bufferNode.layer]
		if (existing != null) {
			bufferNode.buffer = existing
			return bufferNode
		}

		when (bufferNode) {
			is GraphBuffer.MultiParent -> {
				val parents: List<GraphBuffer> = bufferNode.parents.map { builder ->
					iterateOutput(builder, queue)
				}
				val matrices = parents.map { it.buffer ?: throw IllegalStateException() }
				val currentOutput = bufferNode.layer.call(matrices)
				return bufferNode
					.also {
						it.buffer = currentOutput
						queue[bufferNode.layer] = currentOutput
					}
			}
			is GraphBuffer.SingleParent -> {
				// at the stage of implementation this is a single parent
				val p = bufferNode.parent
				val parentNode = iterateOutput(p, queue)
				val matrix = bufferNode.layer.call(parentNode.buffer ?: throw IllegalStateException())
				bufferNode.buffer = matrix
				return bufferNode
			}
			is GraphBuffer.DeadEnd -> {
				bufferNode.buffer = queue[bufferNode.layer] ?: throw IllegalStateException()
				return bufferNode
			}
			else -> {
				throw IllegalStateException("Bad graph structure")
			}
		}
	}
}

fun Model.revertToBuilder() = ModelBuilder(originInputs, originOutputs, debug)
