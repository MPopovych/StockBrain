package brain.ga

import brain.ga.policies.FutureMatch
import brain.ga.weights.ModelGenes
import brain.models.Model
import brain.utils.printCyanBr
import brain.utils.printYellowBr
import kotlin.random.Random


class GA(
	private val settings: GASettings,
	val initialModel: Model,
	private val onGeneration: (Int, GA) -> Unit = { _, _ -> },
	private var earlyStopCallback: (Int, GA) -> Boolean = { _, _ -> false },
) {

	private val originalBuilder = initialModel.revertToBuilder()
	private val originalGenes = ModelGenes(0, initialModel, "I", "I")
	private val modelBuffer = (0 until settings.rooms).map {
		allocBuffer()
	}
	val scoreBoardWithRooms = GAScoreBoardWrapRooms(settings)

	fun runFor(generations: Int, silent: Boolean = false, action: ((GAScoreContext) -> Double)): ModelGenes {
		var genCount = 0
		for (i in 1..generations) {
			genCount = i

			val time = System.currentTimeMillis()
			val commands = scoreBoardWithRooms.rooms
				.map { board ->
					runGeneration(board, genCount, action).take(settings.totalPopulationCount)
				}
			val elapsed = (System.currentTimeMillis() - time) / 1000
			val newGenesByRooms = commands.map { roomCommand ->
				roomCommand.map { command -> handleCommand(i, command) }
			}

			newGenesByRooms.forEachIndexed { indexRoom, room ->
				room.forEachIndexed { indexGene, modelGenes ->
					val model = modelBuffer[indexRoom][indexGene].first
					modelGenes.applyToModel(model)
					modelBuffer[indexRoom][indexGene] = Pair(model, modelGenes)
				}
			}

			if (scoreBoardWithRooms.rooms.size > 1 && genCount % settings.leakRoomEvery == 0) {
				repeat(settings.rooms) { repeatIndex ->
					val toI = (repeatIndex + 1) % settings.rooms // next room
					if (repeatIndex != toI) {
						val randomToI = modelBuffer[toI].indices.random()
						val randomModel = modelBuffer[toI][randomToI]
						val bestFrom = scoreBoardWithRooms.rooms[repeatIndex].getTop() ?: return@repeat
						bestFrom.genes.applyToModel(randomModel.first)
						modelBuffer[toI][randomToI] = Pair(randomModel.first, bestFrom.genes.copy().also {
							it.bornOnEpoch = genCount
						})
						printYellowBr("Leak from $repeatIndex to $toI with score: ${bestFrom.score}: ${bestFrom.id.hashCode()}")
					}
				}
			}

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

	private fun handleCommand(generation: Int, command: FutureMatch): ModelGenes {
		when (command) {
			is FutureMatch.CrossMatch -> {
				val destination = command.parentA.copyWithParent(command.parentA.id, command.parentB.id)
				destination.applyCrossOverPolicy(settings.crossOverPolicy, command.parentA.genes, command.parentB.genes)
				val chromosome = destination.chromosome.hashCode()
				if (command.mutate
					|| chromosome == command.parentA.genes.chromosome.hashCode()
					|| chromosome == command.parentB.genes.chromosome.hashCode()
				) {

					destination.applyMutationPolicy(settings.mutationPolicy, source = destination)
				}
				destination.bornOnEpoch = generation
				settings.weightOptPolicy.optimise(destination)
				return destination
			}

			is FutureMatch.MutateMatch -> {
				val destination = command.source.copyWithParent(command.source.id, command.source.id)
				destination.bornOnEpoch = generation
				destination.applyMutationPolicy(settings.mutationPolicy, source = command.source.genes)
				settings.weightOptPolicy.optimise(destination)
				return destination
			}

			is FutureMatch.Repeat -> {
				// keep bornOnEpoch the same
				return command.source.copyGene().also {
					it.bornOnEpoch = command.source.bornOnEpoch
				}
			}

			is FutureMatch.New -> {
				val destination = originalGenes.copyWithParents(
					Random.nextInt(-900, 900).toString(),
					Random.nextInt(-900, 900).toString()
				)
				destination.bornOnEpoch = generation
				destination.applyMutationPolicy(settings.initialMutationPolicy, source = destination)
				return destination
			}
		}
	}

	private fun runGeneration(
		room: GAScoreBoard,
		generation: Int,
		action: ((GAScoreContext) -> Double)
	): List<FutureMatch> {
		val contexts = modelBuffer[room.order]
			.map { model ->
				GAScoreContext(
					generation = generation,
					model = model.first,
					genes = model.second
				)
			}
		val scores = contexts.map { context ->
			val score = action(context)
			val appliedScore = settings.scoringPolicy.applyScore(settings, room, score, generation)
			return@map GAScoreHolder(id = context.genes.chromosome, score = appliedScore, genes = context.genes)
		}

		room.pushBatch(scores)

		return settings.matchMakingPolicy.select(settings, room, generation)
	}

	private fun allocBuffer(): ArrayList<Pair<Model, ModelGenes>> {
		return (0..settings.totalPopulationCount).mapTo(ArrayList()) { index ->
			if (index == 0) { // keep origin
				val model = originalBuilder.build()
				originalGenes.applyToModel(model)
				return@mapTo Pair(model, originalGenes.copyWithParents(
					Random.nextInt(-900, 900).toString(),
					Random.nextInt(-900, 900).toString()
				))
			}
			val model = originalBuilder.build()
			val genes = originalGenes.copyWithParents(
				Random.nextInt(-900, 900).toString(),
				Random.nextInt(-900, 900).toString()
			).applyMutationPolicy(settings.initialMutationPolicy, originalGenes)
			genes.applyToModel(model)
			genes.bornOnEpoch = 0
			return@mapTo Pair(model, genes)
		}
	}

	fun changeEarlyStop(earlyStopCallback: (Int, GA) -> Boolean = { _, _ -> false }) {
		this.earlyStopCallback = earlyStopCallback
	}

}