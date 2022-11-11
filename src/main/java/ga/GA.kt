package ga

import ga.policies.FutureMatch
import ga.weights.ModelGenes
import models.Model
import models.revertToBuilder
import utils.printGreen


class GA(
	private val settings: GASettings,
	initialModel: Model,
	private val onGeneration: (GA) -> Unit = {},
	private val earlyStopCallback: (Int, GA) -> Boolean = { _, _ -> false },
) {

	private val originalBuilder = initialModel.revertToBuilder()
	private val originalGenes = ModelGenes(initialModel)
	private val modelBuffer = (0..settings.totalPopulationCount).mapTo(ArrayList()) {
		val model = originalBuilder.build()
		val genes = originalGenes.copy().applyMutationPolicy(settings.initialMutationPolicy, originalGenes)
		genes.applyToModel(model)
		return@mapTo Pair(model, genes)
	}
	val scoreBoard = GAScoreBoard(settings.topParentCount, settings.scoreBoardOrder)

	fun runFor(generations: Int, silent: Boolean = false, action: ((GAScoreContext) -> Double)): ModelGenes {
		var genCount = 0
		for (i in 1..generations) {
			genCount = i

			val commands = runGeneration(action)
			val newGenes = commands.map { command ->
				handleCommand(command)
			}

			newGenes.forEachIndexed { index, modelGenes ->
				val model = modelBuffer[index].first
				modelGenes.applyToModel(model)
				modelBuffer[index] = Pair(model, modelGenes)
			}

			val topScore = scoreBoard.getTop()?.score ?: throw IllegalStateException("No top score")
			if (!silent) printGreen("Generation: ${i}, topScore: $topScore")
			onGeneration(this)
			if (earlyStopCallback(i, this)) {
				break
			}
		}

		printGreen("Ran $genCount generations in total")
		return scoreBoard.getTop()?.genes ?: throw IllegalStateException("No top score")
	}

	private fun handleCommand(command: FutureMatch): ModelGenes {
		return when (command) {
			is FutureMatch.CrossMatch -> {
				val destination = command.parentA.copyGene()
				destination.applyCrossOverPolicy(settings.crossOverPolicy, command.parentA.genes, command.parentB.genes)
				if (command.mutate) {
					destination.applyMutationPolicy(settings.mutationPolicy, source = destination)
				}
				destination
			}
			is FutureMatch.MutateMatch -> {
				val destination = command.source.copyGene()
				destination.applyMutationPolicy(settings.mutationPolicy, source = command.source.genes)
			}
		}
	}

	private fun runGeneration(action: ((GAScoreContext) -> Double)): List<FutureMatch> {
		val contexts = modelBuffer.map { model -> GAScoreContext(model = model.first, genes = model.second) }
		val scores = contexts.map { context ->
			val score = action(context)
			return@map GAScoreHolder(id = context.genes.chromosome, score = score, genes = context.genes)
		}

		scoreBoard.pushBatch(scores)

		return settings.matchMakingPolicy.select(settings, scoreBoard)
	}

}