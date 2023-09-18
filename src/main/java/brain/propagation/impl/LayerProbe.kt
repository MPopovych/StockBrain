package brain.propagation.impl

import brain.matrix.Matrix
import brain.propagation.BufferPropagation

class LayerProbe(
	private val layerNames: Collection<String>,
) : BufferPropagation {

	override fun acceptBuffer(map: Map<String, Matrix>) {
		val filteredMatricies = layerNames.mapNotNull { map[it] }
	}
}