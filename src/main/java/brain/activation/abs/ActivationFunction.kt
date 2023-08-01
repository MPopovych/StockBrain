package brain.activation.abs

import brain.matrix.Matrix
import brain.serialization.tools.ModelDefaultSerializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.reflect.KClass


interface ActivationFunction {
	val typeName: String
	val factory: ActivationFunctionFactory<*>
	fun call(matrix: Matrix): Matrix
}

@OptIn(ExperimentalSerializationApi::class)
interface ActivationFunctionFactory<T : ActivationFunction> {
	val typeName: String
	fun deserialize(json: String): T
	fun deserialize(byteArray: ByteArray): T
	fun serializeByteArray(activation: ActivationFunction): ByteArray
	fun serializeJson(activation: ActivationFunction): String
}

@OptIn(ExperimentalSerializationApi::class)
interface ActivationFunctionTypedFactory<T : ActivationFunction, S : Any> : ActivationFunctionFactory<T> {
	val inputType: KClass<T>
	val outputSerializer: KSerializer<S>

	fun serialize(value: T): S
	fun deserialize(value: S): T

	override fun serializeByteArray(activation: ActivationFunction): ByteArray {
		val actCasted = cast(activation, inputType)
		val ser = serialize(actCasted)
		return ProtoBuf.encodeToByteArray(outputSerializer, ser)
	}

	override fun serializeJson(activation: ActivationFunction): String {
		val actCasted = cast(activation, inputType)
		val ser = serialize(actCasted)
		return ModelDefaultSerializer.defaultCompact.encodeToString(outputSerializer, ser)
	}

	override fun deserialize(byteArray: ByteArray): T {
		val ser = ProtoBuf.decodeFromByteArray(outputSerializer, byteArray)
		return deserialize(ser)
	}

	override fun deserialize(json: String): T {
		val ser = ModelDefaultSerializer.defaultCompact.decodeFromString(outputSerializer, json)
		return deserialize(ser)
	}

	private fun <T : Any> cast(any: Any, clazz: KClass<out T>): T = clazz.javaObjectType.cast(any)
}

// generated
inline fun <reified T : ActivationFunction> T.objectFactory(): ActivationFunctionFactory<T> {
	val klass = T::class // save class
	val instance = this::class.objectInstance ?: throw IllegalStateException() // singleton instance required

	return object : ActivationFunctionTypedFactory<T, Boolean> {
		override val typeName: String = instance.typeName
		override val inputType: KClass<T> = klass
		override val outputSerializer: KSerializer<Boolean> = Boolean.serializer()

		override fun serialize(value: T): Boolean {
			return false
		}

		override fun deserialize(value: Boolean): T {
			return instance
		}
	}
}
