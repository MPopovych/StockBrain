package utils

import com.google.gson.Gson
import java.io.File
import kotlin.reflect.KClass

object CacheFileProvider {
	private const val EXTENSION = "json"
	private val gson = Gson()

	const val PATH = "./build/cache/"

	// java class
	fun <T : Any> loadJson(name: String, cls: Class<T>, path: String? = null): T {
		val path = path ?: PATH
		val file = File("$path$name.$EXTENSION")
		val dirPath = File(path + if (name.contains("/")) name.substring(0, name.lastIndexOf("/")) else "")
		if (!dirPath.exists()) throw IllegalStateException("Directory does not exist, directory: " + dirPath.path).also {
			println(dirPath.absolutePath)
		}
		if (!file.exists()) throw IllegalStateException("File does not exist, file: " + file.path)
		if (!file.canRead()) throw IllegalStateException("Cannot open file. Is file: " + file.isFile)
		return gson.fromJson(file.readText(), cls)
	}

	// Kotlin class
	fun <T : Any> loadJson(name: String, cls: KClass<T>, path: String? = null): T {
		return loadJson(name, cls.java, path ?: PATH)
	}

	// alt way to pass class
	inline fun <reified T : Any> loadJson(name: String, path: String? = null): T {
		return loadJson(name, T::class.java, path ?: PATH)
	}

	fun <T> save(obj: T, name: String, extension: String = EXTENSION, path: String = PATH) {
		if (name.last() == '/') throw IllegalStateException("Illegal file name: $name . File haven't body name")

		val value = gson.toJson(obj)
		val pathWithSlash = if (!path.endsWith("/")) {
			"${path}/"
		} else {
			path
		}
		val file = File("$pathWithSlash$name.$extension")

		//creating dir if not exist
		val dirPath = File(pathWithSlash + if (name.contains("/")) name.substring(0, name.lastIndexOf("/")) else "")
		if (!dirPath.exists())
			dirPath.mkdirs()

		file.writeText(value)
		return
	}

	fun saveString(value: String, name: String, extension: String = EXTENSION, path: String = PATH) {
		if (name.last() == '/') throw IllegalStateException("Illegal file name: $name . File haven't body name")

		val pathEnd = if (path.endsWith("/")) path else "$path/"
		val file = File("$pathEnd$name.$extension")

		//creating dir if not exist
		val dirPath = File(PATH + if (name.contains("/")) name.substring(0, name.lastIndexOf("/")) else "")
		if (!dirPath.exists())
			dirPath.mkdirs()

		file.writeText(value)
		return
	}

}