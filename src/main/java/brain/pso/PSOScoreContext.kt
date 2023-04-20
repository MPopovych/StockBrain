package brain.pso

import brain.ga.weights.ModelGenes
import brain.models.Model
import java.util.concurrent.CopyOnWriteArrayList

class PSOScoreContext(val generation: Int, val action: PSOAction, val model: Model, val genes: ModelGenes)