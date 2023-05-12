package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs

object OptPolicy {

	private const val alpha = 0.01f
	private const val alphaRandom = 0.03f

	private val jRandom = java.util.Random()

	fun opt(
		mod: ModelGenes,
		psoContext: PolicyContext,
	) {
		mod.layers.map { l ->
			l.value.map.values.map inner@{ w ->
				if (w.size == 0) return@inner

				val currentGenes = w.genes
				val velocityGenes = FloatArray(currentGenes.size)
				if (w.height > 1 && w.width > 2) {
					for (featH in 0 until w.height) {
						var sum = 0f
						for (featW in featH until w.size step w.width) {
							sum += currentGenes[featW]
						}
						val avg = sum / w.width
						if (abs(avg) > 0.1f) {
							for (featW in featH until w.size step w.width) {
								velocityGenes[featW] -= avg * (alpha + jRandom.nextFloat() * alphaRandom)
							}
						}
					}

					for (i in velocityGenes.indices) {
						currentGenes[i] = currentGenes[i] + velocityGenes[i]
					}
				}
			}
		}

	}

}