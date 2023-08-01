package brain.gat.encoding

import brain.gat.policies.DNAActivationPolicy
import brain.gat.policies.ZygotePolicy
import brain.genes.ModelGenes

class GATCell(
	val parentAGenes: ModelGenes,
	val parentBGenes: ModelGenes,
) {

	fun produceZygote(): ModelGenes {
		return ZygotePolicy.produceZygote(parentAGenes, parentBGenes)
	}

	fun produceActivation(): ModelGenes {
//		return DNAActivationByMinPolicy.activation(parentAGenes, parentBGenes)
		return DNAActivationPolicy.activation(parentAGenes, parentBGenes)
	}

}