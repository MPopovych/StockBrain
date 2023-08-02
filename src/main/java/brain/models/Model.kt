package brain.models

import brain.matrix.Matrix
import brain.matrix.any


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
	fun getOutput(inputMatrix: Matrix): Matrix {
		return getOutputMap(mapOf(DEFAULT_INPUT to inputMatrix))[DEFAULT_OUTPUT] ?: throw IllegalStateException()
	}

	fun getOutput(inputMap: Map<String, Matrix>): Matrix {
		return getOutputMap(inputMap)[DEFAULT_OUTPUT] ?: throw IllegalStateException()
	}

	fun getOutputMap(inputMatrix: Matrix): Map<String, Matrix> {
		return getOutputMap(mapOf(DEFAULT_INPUT to inputMatrix))
	}

	// map
	fun getOutputMap(inputMatrixMap: Map<String, Matrix>): Map<String, Matrix> {
		val buffer = HashMap<String, Matrix>(inputMatrixMap)

		callOrderedGraph.forEach { node ->
			buffer[node.id] = node.invoke(buffer).also {
				if (check) check(it, node.id)
			}
		}

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
			"[${it.id}] - ${it.impl.factory.typeName}"
		}
		return "Total layers: ${callOrderedGraph.size} : outputs: ${outputKeyByLayerName.keys}\n" +
				layerDescription
	}


}


