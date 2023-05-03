package brain.pso

import brain.ga.weights.ModelGenes
import utils.ext.average
import utils.ext.dev
import utils.ext.std
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.random.Random

object ClassicVelocityPolicy {

//	private const val inertia = 0.72984f
//	private const val c1 = 2.05f
//	private const val c2 = 2.05f

	private const val inertia = 0.32984f
	private const val c1 = 1.65f
	private const val c2 = 1.65f

	fun move(
		holder: PSOHolder,
		mod: ModelGenes, pBest: ModelGenes, gBest: ModelGenes,
		context: PolicyContext
	): ModelGenes {
		val velocity = holder.velocity
		val bestExp = exp(context.board.getTop()?.score ?: throw IllegalStateException())
		val currentExp = exp(holder.current.score)
		val advance = (bestExp / (bestExp + currentExp)).toFloat() - 0.2f

		mod.layers.map { l ->
			val velocityLayer = velocity.layerAndWeightMap[l.key] ?: throw IllegalStateException()
			val pBestLayer = pBest.layers[l.key] ?: throw IllegalStateException()
			val gBestLayer = gBest.layers[l.key] ?: throw IllegalStateException()
			l.value.map.values.map { w ->
				val currentGenes = w.genes
				val velocityGenes = velocityLayer[w.weightName] ?: throw IllegalStateException()
				val pBestGenes = pBestLayer.map[w.weightName]?.genes ?: throw IllegalStateException()
				val gBestGenes = gBestLayer.map[w.weightName]?.genes ?: throw IllegalStateException()

				val deltaArray = FloatArray(currentGenes.size)

				for (i in deltaArray.indices) {
					deltaArray[i] = inertia * (
							velocityGenes[i] * advance
									+ (c1 * Random.nextFloat() * (pBestGenes[i] - currentGenes[i]))
									+ (c2 * Random.nextFloat() * (gBestGenes[i] - currentGenes[i])))
				}

				for (i in deltaArray.indices) {
					var newV = currentGenes[i] + deltaArray[i]
					val absNewV = abs(newV)
					if (absNewV > 1) {
						// optimise new value by sqrt(x), makes harder to evolve large values
						newV = sqrt(absNewV) * (newV / absNewV)
					}
					currentGenes[i] = newV
				}
			}
		}
		return mod
	}

	private fun averageBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.average()
		}
		return buf
	}

	private fun stdBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.std()
		}
		return buf
	}

	private fun devBuffer(mList: List<FloatArray>): FloatArray {
		val buf = FloatArray(mList.first().size)
		for (i in buf.indices) {
			buf[i] = mList.map { it[i] }.dev()
		}
		return buf
	}
}