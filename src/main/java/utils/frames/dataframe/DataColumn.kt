package utils.frames.dataframe

import utils.math.ScaleDataType

/** Immutable **/
sealed class DataColumn<T>(
	val name: String,
	val scale: ScaleDataType,
	val data: Array<T>
) {

	companion object {
		fun ofSingleValue(name: String, scale: ScaleDataType, value: Any): DataColumn<*> {
			return if (value is Number) {
				val array: Array<Number> = arrayOf(value)
				Numeric(name, scale, array)
			} else {
				Generic(name, scale, arrayOf(value))
			}
		}
	}

	class Generic<T>(
		name: String, scale: ScaleDataType, data: Array<T>
	) : DataColumn<T>(name, scale, data) {
		override fun copy(name: String, scale: ScaleDataType): Generic<T> {
			return Generic(name, scale, data.copyOf())
		}

		override fun shallowCopy(name: String, scale: ScaleDataType): Generic<T> {
			return Generic(name, scale, data)
		}

		override fun lambdaData(block: (Array<*>) -> Array<*>): Generic<*> {
			return Generic(name, scale, block(data.copyOf()))
		}

		override fun copyWithNewInternal(new: T): Generic<T> {
			return Generic<T>(name, scale, data.plus(new))
		}
	}

	class Numeric(
		name: String, scale: ScaleDataType, data: Array<Number>
	) : DataColumn<Number>(name, scale, data) {
		override fun copy(name: String, scale: ScaleDataType): Numeric {
			return Numeric(name, scale, data.copyOf())
		}

		override fun shallowCopy(name: String, scale: ScaleDataType): Numeric {
			return Numeric(name, scale, data)
		}

		override fun lambdaData(block: (Array<*>) -> Array<*>): Numeric {
			val cast = block(data.copyOf()).filterIsInstance<Number>().toTypedArray()
			return Numeric(name, scale, cast)
		}

		override fun copyWithNewInternal(new: Number): Numeric {
			return Numeric(name, scale, data.plus(new))
		}
	}

	val size = data.size

	fun isEmpty() = data.isEmpty()
	abstract fun copy(name: String = this.name, scale: ScaleDataType = this.scale): DataColumn<T>
	abstract fun shallowCopy(name: String = this.name, scale: ScaleDataType = this.scale): DataColumn<T>

	abstract fun lambdaData(block: ((Array<*>) -> Array<*>)): DataColumn<*>
	abstract fun copyWithNewInternal(new: T): DataColumn<T>
	fun copyWithNew(new: Any?): DataColumn<T> {
		if (new == null) throw IllegalStateException("null")
		val sameType = new as? T ?:  throw IllegalStateException("Not same type")
		return copyWithNewInternal(sameType)
	}

}