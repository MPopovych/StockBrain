package brain.propagation

import brain.matrix.Matrix

interface PropagationContext {
	fun acceptBuffer(map: Map<String, Matrix>)
}

interface PropagationModule

interface TrainingPropagation: PropagationModule

interface BufferPropagation: PropagationModule {
	fun acceptBuffer(map: Map<String, Matrix>)
}


