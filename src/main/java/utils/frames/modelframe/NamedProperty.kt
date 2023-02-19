package utils.frames.modelframe

import java.io.Serializable

interface NamedPropModel<T: NamedPropModel<T>>  {
	fun propGetter(): NamedPropGetter<T>
}
fun <T : NamedPropModel<T>> T.propSelf(): NamedPropGetter<T> {
	return this.propGetter()
}

abstract class NamedPropGetter<Owner> {
	val properties: LinkedHashMap<String, NamedProperty<Owner>> = LinkedHashMap()

	fun addProp(prop: NamedProperty<Owner>) {
		if (properties.contains(prop.name)) throw IllegalStateException("Model already contains a property ${prop.name}")
		properties[prop.name] = prop
	}
}

interface NamedProperty<Owner>{
	val name: String
	val get: (Owner) -> Number
}

internal class NamedPropertyImpl<Owner>(
	parent: NamedPropGetter<Owner>,
	override val name: String,
	override val get: (Owner) -> Number,
) : NamedProperty<Owner>, Serializable {

	init {
		parent.addProp(this)
	}

	override fun toString(): String = "[$name=$get]"
}

fun <T> NamedPropGetter<T>.nameProp(name: String, initializer: (T) -> Number): NamedProperty<T> =
	NamedPropertyImpl(this, name, initializer)