package brain.gat

import brain.ga.weights.ModelGenes
import brain.gat.context.GATEvalContext
import brain.gat.context.GATScored
import brain.gat.context.GATSettings
import brain.gat.encoding.GATCell
import brain.gat.encoding.GATModel
import brain.gat.policies.DNAMutationPolicy
import brain.models.Model
import brain.utils.printYellowBr

class GAT(
	private val settings: GATSettings,
	val initialModel: Model,
	private val onGeneration: (Int, GAT) -> Unit = { _, _ -> },
	private var earlyStopCallback: (Int, GAT) -> Boolean = { _, _ -> false },
) {

	private val originalZygote = ModelGenes(initialModel)
	val scoreboard = GATScoreBoard(0, settings)

	fun runFor(generations: Int, silent: Boolean = false, block: ((GATEvalContext) -> Double)): ModelGenes {
		var genCount = 0

		for (i in 0..generations) {
			genCount = i

			val time = System.currentTimeMillis()
			if (i == 0) {
				runInitGeneration(scoreboard, genCount, block)
			} else {
				runGeneration(scoreboard, genCount, block)
			}
			val elapsed = (System.currentTimeMillis() - time) / 1000

			val topScore = scoreboard.getTop()?.score ?: throw IllegalStateException("No top score")
			if (!silent) printYellowBr(
				"Generation: ${i}, " +
						"topScore: $topScore, " +
						"time: ${elapsed}s, " +
						"time challenge: ${elapsed / settings.population}s"
			)
			onGeneration(i, this)
			if (earlyStopCallback(i, this)) {
				break
			}
		}

		printYellowBr("Ran $genCount generations in total")
		return scoreboard.getTop()?.model?.pheno ?: throw IllegalStateException("No top score")
	}

	private fun runInitGeneration(
		room: GATScoreBoard,
		generation: Int,
		action: ((GATEvalContext) -> Double),
	) {
		val scores = (0 until settings.population).map { ord ->
			if (ord == 0) {
				val context = GATEvalContext(generation, originalZygote)
				val score = action(context)
				val cell = GATCell(originalZygote, originalZygote)
				val model = GATModel(-1, originalZygote, cell)
				return@map GATScored(model.phenoId, score, model, "0A", "0B")
			}
			val mutationA = DNAMutationPolicy.mutate(originalZygote, settings, true)
			val mutationB = DNAMutationPolicy.mutate(originalZygote, settings, true)
			val cell = GATCell(mutationA, mutationB)
			val pheno = cell.produceActivation()
			val model = GATModel(0, pheno, cell)
			val context = GATEvalContext(generation, pheno)
			val score = action(context)
			return@map GATScored(model.phenoId, score, model, "${ord}A", "${ord}B")
		}
		room.pushBatch(scores)
	}

	private fun runGeneration(
		room: GATScoreBoard,
		generation: Int,
		action: ((GATEvalContext) -> Double),
	) {
		val list = room.getAscendingFitnessList().takeLast(settings.topParentCount).asReversed()

		val scores = (0 until settings.population).map {
			val aM = list[it % list.size] // use at least once the top specimens
			val bM = list.random()
			val a = aM.model.produceZygote(settings)
			val b = bM.model.produceZygote(settings)
			val cell = GATCell(a, b)
			val phenoModel = cell.produceActivation()
			val model = GATModel(generation, phenoModel, cell)
			val context = GATEvalContext(generation, phenoModel)
			val score = action(context)
			return@map GATScored(model.phenoId, score, model, aM.id, bM.id)
		}
		room.pushBatch(scores)
	}

	fun changeEarlyStop(earlyStopCallback: (Int, GAT) -> Boolean = { _, _ -> false }) {
		this.earlyStopCallback = earlyStopCallback
	}
}