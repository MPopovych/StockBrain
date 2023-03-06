package brain.ga.policies

import brain.ga.weights.LayerGenes
import brain.ga.weights.WeightGenes
import kotlin.math.max
import kotlin.math.min
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
		if (Random.nextBoolean()) {
			a.genes.copyInto(destination.genes, 0, 0, destinationPoint)
			b.genes.copyInto(destination.genes, destinationPoint, destinationPoint, destination.genes.size)
		} else {
			b.genes.copyInto(destination.genes, 0, 0, destinationPoint)
			a.genes.copyInto(destination.genes, destinationPoint, destinationPoint, destination.genes.size)
		}
	}
}

class TwoPointCrossOver : CrossOverPolicy {
	private val fallback = SinglePointCrossOver()

	override fun crossWeight(
		a: WeightGenes,
		b: WeightGenes,
		destination: WeightGenes,
	) {
		val destinationPoint1 = destination.genes.indices.random()
		val destinationPoint2 = destination.genes.indices.random()
		val first = min(destinationPoint1, destinationPoint2)
		val second = max(destinationPoint1, destinationPoint2)
		if (first == second) return fallback.crossWeight(a, b, destination)

		a.genes.copyInto(destination.genes, 0, 0, first)
		b.genes.copyInto(destination.genes, first, first, second)
		a.genes.copyInto(destination.genes, second, second, destination.genes.size)
	}
}


