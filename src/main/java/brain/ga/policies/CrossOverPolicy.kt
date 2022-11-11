package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import kotlin.random.Random

interface CrossOverPolicy {
	fun cross(a: LayerGenes, b: LayerGenes, destination: LayerGenes) {
		for (weight in a.map) {
			val aW = a.map[weight.key] ?: throw IllegalStateException()
			val bW = b.map[weight.key] ?: throw IllegalStateException()
			val dW = destination.map[weight.key] ?: throw IllegalStateException()
			crossWeight(aW, bW, destination = dW)
		}
	}

	fun crossWeight(
		a: WeightGenes,
		b: WeightGenes,
		destination: WeightGenes,
	)
}

class UniformCrossOver : CrossOverPolicy {
	override fun crossWeight(
		a: WeightGenes,
		b: WeightGenes,
		destination: WeightGenes,
	) {
		for (i in destination.genes.indices) {
			if (Random.nextBoolean()) {
				destination.genes[i] = a.genes[i]
			} else {
				destination.genes[i] = b.genes[i]
			}
		}
	}
}

class SinglePointCrossOver : CrossOverPolicy {
	override fun crossWeight(
		a: WeightGenes,
		b: WeightGenes,
		destination: WeightGenes,
	) {
		val destinationPoint = destination.genes.indices.random()
		a.genes.copyInto(destination.genes, 0, 0, destinationPoint)
		b.genes.copyInto(destination.genes, destinationPoint, destinationPoint, destination.genes.size)
	}
}

