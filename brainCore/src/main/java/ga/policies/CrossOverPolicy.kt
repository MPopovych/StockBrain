package ga.policies

import ga.weights.LayerGenes
import ga.weights.WeightGenes
import kotlin.random.Random

interface CrossOverPolicy {
	fun cross(a: LayerGenes, b: LayerGenes, d: LayerGenes) {
		for (weight in a.map) {
			val aW = a.map[weight.key] ?: throw IllegalStateException()
			val bW = b.map[weight.key] ?: throw IllegalStateException()
			val dW = d.map[weight.key] ?: throw IllegalStateException()
			crossWeight(aW, bW, dW)
		}
	}

	fun crossWeight(a: WeightGenes, b: WeightGenes, d: WeightGenes)
}

class UniformCrossOver: CrossOverPolicy {
	override fun crossWeight(a: WeightGenes, b: WeightGenes, d: WeightGenes) {
		for (i in d.genes.indices) {
			if (Random.nextBoolean()) {
				d.genes[i] = a.genes[i]
			} else {
				d.genes[i] = b.genes[i]
			}
		}
	}
}

class SinglePointCrossOver: CrossOverPolicy {
	override fun crossWeight(a: WeightGenes, b: WeightGenes, d: WeightGenes) {
		val point = d.genes.indices.random()
		a.genes.copyInto(d.genes, 0, 0, point)
		b.genes.copyInto(d.genes, point, point, d.genes.size)
	}
}

