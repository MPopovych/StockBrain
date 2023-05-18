package brain.gat.encoding

import brain.ga.weights.ModelGenes
import brain.gat.policies.DNAActivationPolicy
import brain.gat.policies.ZygotePolicy

class GATCell(
	val parentAGenes: ModelGenes,
	val parentBGenes: ModelGenes,
) {

	fun produceZygote(): ModelGenes {
		return ZygotePolicy.produceZygote(parentAGenes, parentBGenes)
	}

	fun produceActivation(): ModelGenes {
		return DNAActivationPolicy.activation(parentAGenes, parentBGenes)
	}

}