package brain.abs

import kotlinx.serialization.Serializable

@Serializable
sealed interface Dim {
	@Serializable
	@JvmInline
	value class Const(val x: Int): Dim {
		fun toValue() = x
	}
	@Serializable
	object Variable: Dim

	fun requireConst(): Const {
		when(this) {
			is Const -> return this
			Variable -> throw IllegalStateException("Variable in place of const")
		}
	}

	fun isConst(): Boolean = this is Const
	fun isVariable(): Boolean = this is Variable
}

fun Int.toDim(): Dim.Const = Dim.Const(this)

@Serializable
data class DimShape(
	val width: Dim,
	val height: Dim
) {
	fun format() = "[${width}, ${height}]"
}