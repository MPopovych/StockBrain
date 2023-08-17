@file:OptIn(ExperimentalSerializationApi::class)

package brain.serialization

import brain.matrix.Matrix
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
class MatrixSerialized(
	@ProtoNumber(1) val width: Int,
	@ProtoNumber(2) val height: Int,
	@ProtoNumber(3) val data: String,
) {
	companion object {
		fun fromMatrix(matrix: Matrix): MatrixSerialized {
			val data = matrix.readStringData()
			return MatrixSerialized(matrix.width, matrix.height, data)
		}

		fun fromByteArray(byteArray: ByteArray): MatrixSerialized {
			return ProtoBuf.decodeFromByteArray<MatrixSerialized>(bytes = byteArray)
		}
	}

	fun toMatrix(): Matrix {
		return Matrix.fromEncoded(width, height, data)
	}
}