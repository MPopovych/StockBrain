package brain.gat.encoding

import brain.gat.context.GATSettings
import brain.gat.policies.DNAMutationPolicy
import brain.genes.ModelGenes

class GATModel(
	val bornOnEpoch: Int,
	val pheno: ModelGenes,
	val cell: GATCell,
) {

	val phenoId = pheno.chromosome()

	fun produceZygote(settings: GATSettings): ModelGenes {
		val zygote = cell.produceZygote()
		return DNAMutationPolicy.mutate(zygote, settings, false)
	}
}