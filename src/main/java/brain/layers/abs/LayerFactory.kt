package brain.layers.abs

import brain.serialization.tools.Injector
import brain.serialization.tools.ModelDefaultSerializer
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.reflect.KClass

interface LayerFactory<T : LayerImpl> {
	val typeName: String

	fun deserialize(injector: Injector, json: String): T
	fun deserialize(injector: Injector, byteArray: ByteArray): T
	fun serializeByteArray(input: LayerImpl): ByteArray
	fun serializeJson(input: LayerImpl): JsonElement
}

interface LayerTypedFactory<T : LayerImpl, S : Any> : LayerFactory<T> {
	val inputType: KClass<T>

	val outputSerializer: SerializationStrategy<S>
	val outputDeserializer: DeserializationStrategy<S>

	fun deserialize(injector: Injector, value: S): T
	fun serialize(value: T): S
	fun copy(value: T): T

	@OptIn(ExperimentalSerializationApi::class)
	override fun deserialize(injector: Injector, byteArray: ByteArray): T {
		val ser = ProtoBuf.decodeFromByteArray(outputDeserializer, byteArray)
		return deserialize(injector, ser)
	}

	override fun deserialize(injector: Injector, json: String): T {
		val ser = ModelDefaultSerializer.defaultCompact.decodeFromString(outputDeserializer, json)
		return deserialize(injector, ser)
	}

	@OptIn(ExperimentalSerializationApi::class)
	override fun serializeByteArray(input: LayerImpl): ByteArray {
		val inputCasted = cast(input, inputType)
		val ser = serialize(inputCasted)
		return ProtoBuf.encodeToByteArray(outputSerializer, ser)
	}

	override fun serializeJson(input: LayerImpl): JsonElement {
		val inputCasted = cast(input, inputType)
		val ser = serialize(inputCasted)
		return ModelDefaultSerializer.defaultCompact.encodeToJsonElement(outputSerializer, ser)
	}

	private fun <T : Any> cast(any: Any, clazz: KClass<out T>): T = clazz.javaObjectType.cast(any)

	@Suppress("UNCHECKED_CAST")
	fun asGeneric(): LayerTypedFactory<LayerImpl, *> {
		return this as? LayerTypedFactory<LayerImpl, *> ?: throw IllegalStateException()
	}
}
