package brain.ga

import brain.ga.weights.ModelGenes
import brain.models.Model

class GAScoreContext(val generation: Int, val model: Model, val genes: ModelGenes)