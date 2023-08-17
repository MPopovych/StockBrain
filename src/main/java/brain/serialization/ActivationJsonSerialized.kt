package brain.serialization

import brain.activation.abs.ActivationFunction
import brain.serialization.tools.Injector
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@OptIn(ExperimentalSerializationApi::class)
@Serializable
class ActivationJsonSerialized(
	@ProtoNumber(1) val nameType: String,
	@ProtoNumber(2) val obj: String,
) {
	companion object {
		fun <T : ActivationFunction> wrap(act: T): ActivationJsonSerialized {
			return ActivationJsonSerialized(
				nameType = act.factory.typeName,
				obj = act.factory.serializeJson(act)
			)
		}
	}

	fun read(injector: Injector): ActivationFunction {
		return injector.parseJsonAsActivation(nameType, obj)
	}
}