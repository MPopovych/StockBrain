package brain.pso

import brain.ga.weights.ModelGenes
import utils.ext.average
import utils.ext.dev
import utils.ext.std
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

object ClassicVelocityPolicy {

//	private const val inertia = 0.72984f
//	private const val c1 = 2.05f
//	private const val c2 = 2.05f

	private const val inertia = 0.79f
	private const val c1 = 2.05f
	private const val c2 = 2.05f
	private const val c3 = 2.05f

	private val jRandom = java.util.Random()

	fun move(
		holder: PSOHolder,
		mod: ModelGenes, pBest: ModelGenes, gBest: ModelGenes,
		context: PolicyContext
	): ModelGenes {
		val velocity = holder.velocity
		val currentExp = exp(holder.current.score)
		val pBestExp = exp(holder.best.score)
		val gBestExp = exp(context.board.getTop()?.score ?: throw IllegalStateException())

		val c1Cor = (abs(currentExp - pBestExp) / (pBestExp + currentExp)).toFloat()
		val c1QDistance = 1f - c1Cor
		val c2Cor = (abs(currentExp - gBestExp) / (gBestExp + currentExp)).toFloat()
		val c2QDistance = 1f - c2Cor
		val sumSimilarity = (1f + c1QDistance + c2QDistance) / 3

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
					var newV = inertia * (velocityGenes[i] +
									+ (c1 * jRandom.nextFloat() * (pBestGenes[i] - currentGenes[i])) +
									+ (c2 * jRandom.nextFloat() * (gBestGenes[i] - currentGenes[i])))

					if (abs(newV) < 0.00001) {
						newV = jRandom.nextGaussian().toFloat() * sumSimilarity
					}
					velocityGenes[i] = newV
				}

				for (i in velocityGenes.indices) {
					var newV = currentGenes[i] + velocityGenes[i]
					val absNewV = abs(newV)
					if (absNewV > 1) {
						// optimise new value by sqrt(x), makes harder to evolve large values
						newV = sqrt(absNewV) * (newV / absNewV)
					}
					currentGenes[i] = newV

					for (y in 0 until w.size step w.width) {
						val floatArray = FloatArray(w.width)
						System.arraycopy(currentGenes, y, floatArray, 0, w.width)

						val avg = floatArray.average().toFloat()
//						val std = floatArray.std()
						for (f in y until y + w.width) {
							val vF = currentGenes[f]
							currentGenes[f] = vF - (c3 * avg * jRandom.nextFloat()) / w.width
						}
					}
				}
			}
		}
		return mod
	}
}