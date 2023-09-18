package brain.models

import brain.matrix.Matrix
import brain.matrix.any
import brain.propagation.PropagationContext


class Model(
	internal val outputKeyByLayerName: Map<String, String>,
	internal val callOrderedGraph: List<GraphNodeType>,
	private val check: Boolean = false,
) {

	companion object {
		const val DEFAULT_INPUT = "DEF_I"
		const val DEFAULT_OUTPUT = "DEF_O"
	}

	fun copy() = Model(outputKeyByLayerName, callOrderedGraph.map { it.copy() }, check)

	fun onWeightUpdated() = callOrderedGraph.forEach { it.impl.onWeightUpdated() }

	// single
	fun getOutput(inputMatrix: Matrix, prop: PropagationContext? = null): Matrix {
		return getOutputMap(mapOf(DEFAULT_INPUT to inputMatrix), prop)[DEFAULT_OUTPUT] ?: throw IllegalStateException()
	}

	fun getOutput(inputMap: Map<String, Matrix>, prop: PropagationContext? = null): Matrix {
		return getOutputMap(inputMap, prop)[DEFAULT_OUTPUT] ?: throw IllegalStateException()
	}

	fun getOutputMap(inputMatrix: Matrix, prop: PropagationContext? = null): Map<String, Matrix> {
		return getOutputMap(mapOf(DEFAULT_INPUT to inputMatrix), prop)
	}

	// map
	fun getOutputMap(inputMatrixMap: Map<String, Matrix>, prop: PropagationContext? = null): Map<String, Matrix> {
		val buffer = HashMap<String, Matrix>(inputMatrixMap)

		callOrderedGraph.forEach { node ->
			buffer[node.id] = node.invoke(buffer, prop).also {
				if (check) check(it, node.id)
			}
		}

		prop?.acceptBuffer(buffer)

		return outputKeyByLayerName.mapValues {
			return@mapValues buffer[it.value] ?: throw IllegalStateException()
		}.also {
			buffer.clear() // for easier gc
		}
	}

	private fun check(matrix: Matrix, ioName: String) {
		if (check && matrix.any { !it.isFinite() }) {
			throw IllegalStateException("$ioName has NaN")
		}
	}

	fun summary(): String {
		val layerDescription = callOrderedGraph.joinToString("\n") {
			"[${it.id}] - ${it.impl.weightData().map { it.matrix.shape() }}"
		}
		val params = callOrderedGraph.sumOf {
			it.impl.weightData().filter { m -> m.active }.sumOf { m -> m.matrix.width * m.matrix.height }
		}
		return "Total layers: ${callOrderedGraph.size}, param: ${params}, outputs: ${outputKeyByLayerName.keys}\n" +
				layerDescription
	}


}


