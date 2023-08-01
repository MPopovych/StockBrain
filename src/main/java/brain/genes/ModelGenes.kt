package brain.genes

import brain.models.Model

class ModelGenes(
	val layerByWeightMap: Map<String, Map<String, WeightGenes>>,
) {
	companion object {
		fun of(model: Model): ModelGenes {
			val map = model.callOrderedGraph.associateBy { it.id }.mapValues { (k, node) ->
				node.impl.weightData().associateBy { weight ->
					weight.name
				}.mapValues { (name, weight) ->
					WeightGenes(
						weight.matrix.width,
						weight.matrix.height,
						weight.matrix.readFloatData(),
						trainable = weight.trainable
					)
				}
			}
			return ModelGenes(map)
		}
	}

	fun chromosome() = layerByWeightMap.values
		.flatMap { it.values }
		.joinToString(" ") { it.chromosome().hashCode().toString() }

	val geneCount = layerByWeightMap.values.flatMap { it.values }.sumOf { l -> l.genes.size }

	fun applyTo(model: Model) {
		model.callOrderedGraph.forEach { node ->
			val subMap = layerByWeightMap[node.id] ?: throw IllegalStateException()
			node.impl.weightData().forEach { w ->
				val genes = subMap[w.name] ?: throw IllegalStateException()
				w.matrix.writeFloatData(genes.genes)
			}
		}
		model.onWeightUpdated()
	}
}