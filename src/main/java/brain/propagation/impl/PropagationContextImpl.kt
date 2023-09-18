package brain.propagation.impl

import brain.matrix.Matrix
import brain.propagation.BufferPropagation
import brain.propagation.PropagationContext

class PropagationTracker(
	vararg contexts: PropagationContext
): PropagationContext {
	private val bufferProbe: BufferPropagation? = contexts.firstNotNullOfOrNull { it as? BufferPropagation }

	override fun acceptBuffer(map: Map<String, Matrix>) {
		bufferProbe?.acceptBuffer(map)
	}


}