package brain.pso

import brain.ga.weights.LayerGenes
import brain.ga.weights.ModelGenes
import brain.ga.weights.WeightGenes
import kotlin.math.pow
import kotlin.math.sqrt

object PSOUtils {

	fun countModelGenes(mod: ModelGenes): Int {
		return mod.layers.values.sumOf { l -> l.map.values.sumOf { w -> w.size } }
	}

	fun modelDistance(fromMod: ModelGenes,
	                  toRef: ModelGenes
	): Float {
		val totalGeneCount = countModelGenes(fromMod)
		val totalSum = fromMod.layers.map {
			val toW = toRef.layers[it.key] ?: throw IllegalStateException()
			getDistanceSquared(it.value, toW)
		}.sum()
		return sqrt(totalSum / totalGeneCount)
	}

	private fun getDistanceSquared(fromMod: LayerGenes, toRef: LayerGenes): Float {
		return fromMod.map.map {
			val toW = toRef.map[it.key] ?: throw IllegalStateException()
			getDistanceSquared(it.value, toW)
		}.sum()
	}

	private fun getDistanceSquared(fromMod: WeightGenes, toRef: WeightGenes): Float {
		return fromMod.genes.mapIndexed { index, fl ->
			(toRef.genes[index] - fl).pow(2)
		}.sum()
	}
}