package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs
import kotlin.math.sqrt

object ClassicVelocityPolicy {

//	private const val inertia = 0.72984f
//	private const val c1 = 2.05f
//	private const val c2 = 2.05f

	private const val inertia = 0.72984f
	private const val c1 = 2.05f
	private const val c2 = 2.05f

	private val jRandom = java.util.Random()

	fun move(
		holder: PSOHolder,
		mod: ModelGenes, pBest: ModelGenes, gBest: ModelGenes,
		context: PolicyContext
	): ModelGenes {
		val velocity = holder.velocity

		mod.layers.map { l ->
			val velocityLayer = velocity.layerAndWeightMap[l.key] ?: throw IllegalStateException()
			val pBestLayer = pBest.layers[l.key] ?: throw IllegalStateException()
			val gBestLayer = gBest.layers[l.key] ?: throw IllegalStateException()
			l.value.map.values.map inner@{ w ->
				if (w.size == 0) return@inner

				val currentGenes = w.genes
				val velocityGenes = velocityLayer[w.weightName] ?: throw IllegalStateException()
				val pBestGenes = pBestLayer.map[w.weightName]?.genes ?: throw IllegalStateException()
				val gBestGenes = gBestLayer.map[w.weightName]?.genes ?: throw IllegalStateException()

				for (i in velocityGenes.indices) {
					val newV = inertia * (velocityGenes[i] +
									+ (c1 * jRandom.nextFloat() * (pBestGenes[i] - currentGenes[i])) +
									+ (c2 * jRandom.nextFloat() * (gBestGenes[i] - currentGenes[i])))

					velocityGenes[i] = newV
				}

				for (i in velocityGenes.indices) {
					currentGenes[i] = currentGenes[i] + velocityGenes[i]
				}
			}
		}
		return mod
	}
}