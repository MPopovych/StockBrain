package models

import layers.InputLayer
import layers.Layer
import layers.LayerBuilder
import matrix.Matrix
import utils.ifAlso
import utils.printYellow

class Model(
	internal val originInput: InputLayer,
	internal val originOutput: LayerBuilder<*>,
	internal val debug: Boolean = true,
) {
	var input: GraphBuffer.DeadEnd
	var output: GraphBuffer

	val nodeGraph = buildNodes(originOutput, debug)
	val layers = nodeGraph.values.map { it.layer }
	val graphBuffer = nodeGraph.values
	init {
		input = nodeGraph[originInput] as? GraphBuffer.DeadEnd ?: throw IllegalStateException()
		output = nodeGraph[originOutput] ?: throw IllegalStateException()
	}

	fun getOutput(inputMatrix: Matrix): Matrix {
		graphBuffer.forEach { it.flush() }
		val buffer = HashMap<Layer, Matrix>()
		buffer[input.layer] = inputMatrix

		return (iterateOutput(output, buffer) as GraphBuffer.SingleParent).buffer ?: throw IllegalStateException()
	}

	fun iterateOutput(bufferNode: GraphBuffer, queue: HashMap<Layer, Matrix>): GraphBuffer {
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

fun Model.revertToBuilder() = ModelBuilder(originInput, originOutput, debug)
