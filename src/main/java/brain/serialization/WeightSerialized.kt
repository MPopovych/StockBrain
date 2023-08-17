@file:OptIn(ExperimentalSerializationApi::class)

package brain.serialization

import brain.layers.weights.WeightData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
class WeightSerialized(
	@ProtoNumber(1) val name: String,
	@ProtoNumber(2) val matrixSerialized: MatrixSerialized,
	@ProtoNumber(3) val active: Boolean,
	@ProtoNumber(4) val trainable: Boolean,
) {
	companion object {
		fun fromByteArray(byteArray: ByteArray): WeightSerialized {
			return ProtoBuf.decodeFromByteArray<WeightSerialized>(bytes = byteArray)
		}
	}

	fun toWeightData(): WeightData {
		return WeightData(name, matrixSerialized.toMatrix(), active, trainable)
	}
}