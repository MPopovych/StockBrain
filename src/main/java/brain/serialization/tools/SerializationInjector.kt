package brain.serialization.tools

import brain.activation.abs.ActivationFunction
import brain.activation.abs.ActivationFunctionFactory
import brain.activation.abs.Activations
import brain.layers.abs.LayerFactory
import brain.layers.abs.LayerImpl
import brain.layers.impl.*
import kotlinx.serialization.json.JsonElement

interface Injector {
	companion object {
		val default = defaultInjector()
	}

	fun copy(): Injector
	fun <T : LayerImpl> push(type: String, factory: LayerFactory<T>, checkInsert: Boolean = true)
	fun <T : ActivationFunction> push(type: String, factory: ActivationFunctionFactory<T>, checkInsert: Boolean = true)
	fun parseJsonElemAsLayer(type: String, json: JsonElement): LayerImpl
	fun parseJsonAsLayer(type: String, json: String): LayerImpl
	fun parseJsonAsActivation(type: String, json: String): ActivationFunction
	fun parseArrayAsLayer(type: String, byteArray: ByteArray): LayerImpl
	fun parseArrayAsActivation(type: String, byteArray: ByteArray): ActivationFunction

	fun push(activation: ActivationFunction, checkInsert: Boolean = true) {
		val factory = activation.factory
		push(type = factory.typeName, factory = factory, checkInsert)
	}

	fun push(factory: LayerFactory<*>, checkInsert: Boolean = true) {
		push(type = factory.typeName, factory = factory, checkInsert)
	}
}

class SerializationInjector(
	private val activations: HashMap<String, ActivationFunctionFactory<*>> = HashMap(),
	private val layers: HashMap<String, LayerFactory<*>> = HashMap(),
) : Injector {

	override fun copy() = SerializationInjector(HashMap(activations), HashMap(layers))

	override fun <T : LayerImpl> push(type: String, factory: LayerFactory<T>, checkInsert: Boolean) {
		if (checkInsert && activations.containsKey(type)) {
			throw IllegalStateException("Double insertion")
		}
		layers[type] = factory
	}

	override fun <T : ActivationFunction> push(
		type: String,
		factory: ActivationFunctionFactory<T>,
		checkInsert: Boolean,
	) {
		if (checkInsert && activations.containsKey(type)) {
			throw IllegalStateException("Double insertion")
		}
		activations[type] = factory
	}

	override fun parseJsonElemAsLayer(type: String, json: JsonElement): LayerImpl {
		return parseJsonAsLayer(type, json = json.toString())
	}

	override fun parseJsonAsLayer(type: String, json: String): LayerImpl {
		val entry = layers[type] ?: throw IllegalStateException("No $type in layer injections")
		return entry.deserialize(this, json)
	}

	override fun parseArrayAsLayer(type: String, byteArray: ByteArray): LayerImpl {
		val entry = layers[type] ?: throw IllegalStateException("No $type in layer injections")
		return entry.deserialize(this, byteArray)
	}

	override fun parseJsonAsActivation(type: String, json: String): ActivationFunction {
		val entry = activations[type] ?: throw IllegalStateException("No $type in activation injections")
		return entry.deserialize(json)
	}

	override fun parseArrayAsActivation(type: String, byteArray: ByteArray): ActivationFunction {
		val entry = activations[type] ?: throw IllegalStateException("No $type in activation injections")
		return entry.deserialize(byteArray)
	}
}

private fun defaultInjector(): Injector {
	val default = SerializationInjector()
	default.push(Activations.Abs)
	default.push(Activations.ReLu)
	default.push(Activations.Softmax)
	default.push(Activations.CapLeReLu)
	default.push(Activations.ShiftedReLu)
	default.push(Activations.Tanh)
	default.push(Activations.HardTanh)
	default.push(Activations.Zero)
	default.push(Activations.Test)
	default.push(Activations.AbsCap)
	default.push(Activations.LeakyReLu)
	default.push(InputFactory)
	default.push(DenseFactory)
	default.push(ScaleFactory)
	default.push(ConcatFactory)
	default.push(AttentionFactory)
	default.push(FlattenFactory)
	default.push(ActivationFactory)
	return default
}