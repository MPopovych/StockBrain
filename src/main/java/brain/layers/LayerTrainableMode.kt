package brain.layers

interface LayerTrainableMode {

	fun setTrainable(trainable: Boolean)

}

interface LayerWarmupMode {

	fun warmup()

}