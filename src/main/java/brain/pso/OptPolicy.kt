package brain.pso

import brain.ga.weights.ModelGenes
import kotlin.math.abs

object OptPolicy {

	private const val alpha = 0.3f

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
				if (w.width > 2 && w.callOrder < 4) {
					for (featH in 0 until w.height) {
						var sum = 0f
						for (featW in featH until w.size step w.width) {
							sum += currentGenes[featW]
						}
						val avg = sum / w.width
						if (abs(avg) > 0.4f) {
							for (featW in featH until w.size step w.width) {
								val weight = velocityGenes[featW]
								if (avg > 0 && weight > 0) {
									velocityGenes[featW] -= avg * abs(jRandom.nextGaussian().toFloat() / 3) * alpha
								} else if (avg < 0 && weight < 0) {
									velocityGenes[featW] -= avg * abs(jRandom.nextGaussian().toFloat() / 3) * alpha
								}
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