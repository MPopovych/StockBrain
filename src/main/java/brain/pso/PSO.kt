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
						runGenerationTest(board, genCount, block)
//						runGeneration(board, genCount, block)
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
			model.geneBuffer.applyToModel(model.modelBuffer)
			val score = action(context)
			val moveHolder = PSOHolder(
				model.ordinal,
				current = PSOScore(score, model.geneBuffer),
				best = PSOScore(score, model.geneBuffer),
				model.modelBuffer
			)
			room.push(moveHolder, generation)
		}
	}

	private fun runGenerationTest(
		room: PSOScoreBoard,
		generation: Int,
		action: ((PSOScoreContext) -> Double)
	) {
		room.getAscendingFitnessList().shuffled().chunked(3).forEach { models ->
			if (models.size < 3) return@forEach

			val sorted = when (settings.order) {
				PSOScoreBoardOrder.Ascending -> models.sortedBy { it.current.score }
				PSOScoreBoardOrder.Descending -> models.sortedByDescending { it.current.score }
			}
			val best = when (settings.order) {
				PSOScoreBoardOrder.Ascending -> models.maxBy { it.best.score }
				PSOScoreBoardOrder.Descending -> models.minBy { it.best.score }
			}

			val choreographK = settings.choreographyPolicy.getKForContext(settings, generation, room)
			val psoContext = PolicyContext(generation, room, settings, choreographK)

			val top = sorted[2]
			val middle = sorted[1]
			val worst = sorted[0]

			val triple = CustomVelocityPolicy.move(
				best.best,
				top.current, middle.current, worst.current,
				psoContext,
			)

//			OptPolicy.opt(triple.first, psoContext)
//			OptPolicy.opt(triple.second, psoContext)
//			OptPolicy.opt(triple.third, psoContext)

			triple.first.applyToModel(top.modelBuffer)
			triple.second.applyToModel(middle.modelBuffer)
			triple.third.applyToModel(worst.modelBuffer)
			val moveContextT = PSOScoreContext(generation, PSOAction.MOVE, top.modelBuffer, triple.first)
			val moveContextM = PSOScoreContext(generation, PSOAction.MOVE, middle.modelBuffer, triple.second)
			val moveContextW = PSOScoreContext(generation, PSOAction.MOVE, worst.modelBuffer, triple.third)

			val moveScoreT = action(moveContextT)
			val moveHolderT = PSOHolder(
				top.ordinal,
				current = PSOScore(moveScoreT, triple.first),
				best = top.best,
				top.modelBuffer,
			)
			room.push(moveHolderT, generation)

			val moveScoreM = action(moveContextM)
			val moveHolderM = PSOHolder(
				middle.ordinal,
				current = PSOScore(moveScoreM, triple.second),
				best = middle.best,
				middle.modelBuffer,
			)
			room.push(moveHolderM, generation)

			val moveScoreW = action(moveContextW)
			val moveHolderW = PSOHolder(
				worst.ordinal,
				current = PSOScore(moveScoreW, triple.third),
				best = worst.best,
				worst.modelBuffer,
			)
			room.push(moveHolderW, generation)
		}
	}


	private fun allocBuffer(): ArrayList<PSOModel> {
		var ordinal = 0
		return (0 until settings.population).mapTo(ArrayList()) { index ->
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