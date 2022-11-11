package brain.ga

import brain.ga.policies.FutureMatch
import brain.ga.weights.ModelGenes
import brain.models.Model
import brain.models.revertToBuilder
import brain.utils.printYellowBr


class GA(
	private val settings: GASettings,
	val initialModel: Model,
	private val onGeneration: (Int, GA) -> Unit = { _, _ -> },
	private val earlyStopCallback: (Int, GA) -> Boolean = { _, _ -> false },
) {

	private val originalBuilder = initialModel.revertToBuilder()
	private val originalGenes = ModelGenes(initialModel)
	private val modelBuffer = (0..settings.totalPopulationCount).mapTo(ArrayList()) { index ->
		if (index == 0) { // keep origin
			val model = originalBuilder.build()
			originalGenes.applyToModel(model)
			return@mapTo Pair(model, originalGenes.copy())
		}
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

			val time = System.currentTimeMillis()
			val commands = runGeneration(action)
			val elapsed = (System.currentTimeMillis() - time) / 1000
			val newGenes = commands.map { command ->
				handleCommand(command)
			}

			newGenes.forEachIndexed { index, modelGenes ->
				val model = modelBuffer[index].first
				modelGenes.applyToModel(model)
				modelBuffer[index] = Pair(model, modelGenes)
			}

			val topScore = scoreBoard.getTop()?.score ?: throw IllegalStateException("No top score")
			if (!silent) printYellowBr(
				"Generation: ${i}, " +
						"topScore: $topScore, " +
						"time: ${elapsed}s, " +
						"time challenge: ${elapsed / modelBuffer.size}s"
			)
			onGeneration(i, this)
			if (earlyStopCallback(i, this)) {
				break
			}
		}

		printYellowBr("Ran $genCount generations in total")
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
			else -> throw IllegalStateException("Not implemented")
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