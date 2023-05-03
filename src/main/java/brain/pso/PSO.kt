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
//						runGenerationFast(board, genCount, block)
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
			val top = room.getTop() ?: throw IllegalStateException()
			val choreographK = settings.choreographyPolicy.getKForContext(settings, generation, room)
			val psoContext = PolicyContext(generation, room, settings, choreographK)
			val movedBuffer = model.current.genes.copy()

			ClassicVelocityPolicy.move(
				model,
				mod = movedBuffer,
				pBest = model.best.genes,
				gBest = top.genes,
				psoContext,
			)
			movedBuffer.applyToModel(model.modelBuffer)

			val moveContext = PSOScoreContext(generation, PSOAction.MOVE, model.modelBuffer, movedBuffer)
			val moveScore = action(moveContext)
			val moveHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(moveScore, movedBuffer),
				best = model.best,
				model.modelBuffer,
			)
			room.push(moveHolder)
		}
	}

	private fun runGenerationFast(
		room: PSOScoreBoard,
		generation: Int,
		action: ((PSOScoreContext) -> Double)
	) {
		room.getAscendingFitnessList().forEach { model ->
			val top = room.getTop() ?: throw IllegalStateException()
			val choreographK = settings.choreographyPolicy.getKForContext(settings, generation, room)
			val psoContext = PolicyContext(generation, room, settings, choreographK)
			val movedBuffer = model.current.genes.copy().applyVelocityPolicy(settings.velocityPolicy, psoContext)

			movedBuffer.applyApproachPolicy(
				settings.approachPersonalPolicy,
				destination = model.best.genes,
			)
			movedBuffer.applyApproachPolicy(
				settings.approachTopPolicy,
				destination = top.genes,
			)
			movedBuffer.applyToModel(model.modelBuffer)
			val moveContext = PSOScoreContext(generation, PSOAction.MOVE, model.modelBuffer, movedBuffer)
			val moveScore = action(moveContext)
			val moveHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(moveScore, movedBuffer),
				best = model.best,
				model.modelBuffer,
			)
			room.push(moveHolder)
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