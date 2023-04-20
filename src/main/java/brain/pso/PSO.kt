package brain.pso

import brain.ga.weights.ModelGenes
import brain.models.Model
import brain.utils.printYellowBr
import kotlin.random.Random

class PSO(
	private val settings: PSOSettings,
	val initialModel: Model,
	private val onGeneration: (Int, PSO) -> Unit = { _, _ -> },
	private var earlyStopCallback: (Int, PSO) -> Boolean = { _, _ -> false },
) {

	private val originalBuilder = initialModel.revertToBuilder()
	private val originalGenes = ModelGenes(0, initialModel, "I", "I")
	private val modelBuffer = (0 until settings.swarms).map {
		allocBuffer()
	}

	val scoreBoardWithRooms = PSOScoreBoardWrapRooms(settings)

	fun runFor(generations: Int, silent: Boolean = false, block: ((PSOScoreContext) -> Double)): ModelGenes {
		var genCount = 0

		for (i in 0..generations) {
			genCount = i

			val time = System.currentTimeMillis()
			scoreBoardWithRooms.rooms
				.forEach { board ->
					if (i == 0) {
						runInitGeneration(board, genCount, block)
					} else {
						runGeneration(board, genCount, block)
					}
				}
			val elapsed = (System.currentTimeMillis() - time) / 1000

			val topScore = scoreBoardWithRooms.getTop()?.score ?: throw IllegalStateException("No top score")
			if (!silent) printYellowBr(
				"Generation: ${i}, " +
						"topScore: $topScore, " +
						"time: ${elapsed}s, " +
						"time challenge: ${elapsed / modelBuffer.sumOf { it.size }}s"
			)
			onGeneration(i, this)
			if (earlyStopCallback(i, this)) {
				break
			}
		}

		printYellowBr("Ran $genCount generations in total")
		return scoreBoardWithRooms.getTop()?.genes ?: throw IllegalStateException("No top score")
	}

	private fun runInitGeneration(
		room: PSOScoreBoard,
		generation: Int,
		action: ((PSOScoreContext) -> Double)
	) {
		modelBuffer[room.order].forEach { model ->
			val context = PSOScoreContext(generation, PSOAction.INITIAL, model.modelBuffer, model.geneBuffer)
			val score = action(context)
			val moveHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(score, model.geneBuffer),
				best = PSOScore(score, model.geneBuffer),
				model.modelBuffer
			)
			room.push(moveHolder)
		}
	}

	private fun runGeneration(
		room: PSOScoreBoard,
		generation: Int,
		action: ((PSOScoreContext) -> Double)
	) {
		room.getAscendingFitnessList().forEach { model ->
			val movedBuffer = model.current.genes.copy().applyVelocityPolicy(settings.velocityPolicy)
			movedBuffer.applyToModel(model.modelBuffer)
			val moveContext = PSOScoreContext(generation, PSOAction.MOVE, model.modelBuffer, movedBuffer)
			val moveScore = action(moveContext)
			val moveHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(moveScore, movedBuffer),
				best = model.best,
				model.modelBuffer
			)
			room.push(moveHolder)
		}

		val std = room.getStandardDeviation().toFloat()

		room.getAscendingFitnessList().forEach { model ->
			if (model.current.genes == model.best.genes) return@forEach // will produce same result

			val appToBestBuffer = model.current.genes.copy().applyApproachPolicy(
				settings.approachPersonalPolicy,
				ownScore = model.current.score.toFloat(),
				destination = model.best.genes,
				destinationScore = model.best.score.toFloat(),
				globalDeviation = std
			)
			appToBestBuffer.applyToModel(model.modelBuffer)
			val appBestContext =
				PSOScoreContext(generation, PSOAction.APPROACH_PERSONAL, model.modelBuffer, appToBestBuffer)
			val appBestScore = action(appBestContext)
			val appBestHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(appBestScore, appToBestBuffer),
				best = model.best,
				model.modelBuffer
			)
			room.push(appBestHolder)
		}

		val top = room.getTop() ?: throw IllegalStateException()
		room.getAscendingFitnessList().forEach { model ->
			if (model.current.genes == top.genes) return@forEach // will produce same result

			val appToTopBuffer = model.current.genes.copy().applyApproachPolicy(
				settings.approachTopPolicy,
				ownScore = model.current.score.toFloat(),
				destination = top.genes,
				destinationScore = top.score.toFloat(),
				globalDeviation = std
			)
			appToTopBuffer.applyToModel(model.modelBuffer)
			val appTopContext = PSOScoreContext(generation, PSOAction.APPROACH_TOP, model.modelBuffer, appToTopBuffer)
			val appTopScore = action(appTopContext)
			val appTopHolder = PSOHolder(
				model.ordinal,
				PSOScore(appTopScore, appToTopBuffer),
				model.best,
				model.modelBuffer
			)
			room.push(appTopHolder)
		}
	}

	private fun allocBuffer(): ArrayList<PSOModel> {
		var ordinal = 0
		return (0..settings.population).mapTo(ArrayList()) { index ->
			if (index == 0) { // keep origin
				val model = originalBuilder.build()
				originalGenes.applyToModel(model)
				val genes = originalGenes.copyWithParents(
					Random.nextInt(-900, 900).toString(),
					Random.nextInt(-900, 900).toString()
				)
				return@mapTo PSOModel(ordinal++, genes, model)
			}
			val model = originalBuilder.build()
			val genes = originalGenes.copyWithParents(
				Random.nextInt(-900, 900).toString(),
				Random.nextInt(-900, 900).toString()
			).applyMutationPolicy(settings.initialMutationPolicy, originalGenes)
			genes.applyToModel(model)
			genes.bornOnEpoch = 0
			return@mapTo PSOModel(ordinal++, genes, model)
		}
	}

	fun changeEarlyStop(earlyStopCallback: (Int, PSO) -> Boolean = { _, _ -> false }) {
		this.earlyStopCallback = earlyStopCallback
	}
}