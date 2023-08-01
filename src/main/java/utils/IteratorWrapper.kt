package utils

class IteratorWrapper<out O, T>(val wrap: Iterator<T>, val block: (T) -> O) : Iterator<O> {
	override fun hasNext(): Boolean {
		return wrap.hasNext()
	}

	override fun next(): O {
		return block(wrap.next())
	}
}

fun <T, O> Iterator<T>.wrap(block: (T) -> O) = IteratorWrapper(this, block)