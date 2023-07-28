package utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import kotlin.reflect.KClass

class WorkingDirectoryProvider(
	private val wdFile: File,
	private val gson: Gson = GsonBuilder().disableHtmlEscaping().create(),
	private val checkExists: Boolean = false,
) {

	constructor(folder: String) : this(folder, GsonBuilder().disableHtmlEscaping().create(), false)

	constructor(
		folder: String,
		gson: Gson = GsonBuilder().disableHtmlEscaping().create(),
		checkExists: Boolean = false,
	) : this(File(folder.trimStart('/').trimEnd('.')).absoluteFile, gson, checkExists)

	private val wd = wdFile.absolutePath

	init {
		if (wdFile.exists() && !wdFile.isDirectory) throw IllegalStateException("Not a directory")
		if (checkExists && !wdFile.exists()) {
			throw IllegalStateException("No such path $wd")
		}
	}

	fun branchSubFolder(sub: String, check: Boolean = checkExists): WorkingDirectoryProvider {
		val corrected = sub.trimStart('/').trimEnd('/')
		val child = File(wdFile, "/${corrected}/")
		return WorkingDirectoryProvider(child, gson, check)
	}

	fun branchParentFolder(): WorkingDirectoryProvider {
		return WorkingDirectoryProvider(wd.substringBeforeLast('/'), gson = gson, checkExists = checkExists)
	}

	fun getAbsolutePath(): String {
		return wd
	}

	fun checkFileExistsAndReadable(name: String): Boolean {
		if (!wdFile.exists()) return false
		val file = File("${getAbsolutePath()}/$name")
		return file.exists() && file.canRead()
	}

	// java class
	fun <T : Any> loadJson(name: String, cls: Class<T>): T {
		val corrected = name
			.substringBefore(".JSON") // may return different depending on system
			.substringBefore(".json") // make sure extension is removed
		val file = getFile("$corrected.json")
		return gson.fromJson(file.readText(), cls)
	}

	// Kotlin class
	fun <T : Any> loadJson(name: String, cls: KClass<T>): T {
		return loadJson(name, cls.java)
	}

	// alt way to pass class
	inline fun <reified T : Any> loadJson(name: String): T {
		return loadJson(name, T::class.java)
	}

	// java class
	fun loadTextContents(name: String): String {
		return getFile(name).readText()
	}

	// java class
	fun loadByteContents(name: String): ByteArray {
		return getFile(name).readBytes()
	}

	fun saveText(value: String, name: String) {
		if (name.endsWith('/')) throw IllegalStateException("Illegal file name: $name. Provided directory")
		val file = File("$wd/$name")
		if (!wdFile.exists()) wdFile.mkdirs()
		file.writeText(value)
	}

	fun <T> saveJson(obj: T, name: String) {
		if (name.endsWith('/')) throw IllegalStateException("Illegal file name: $name. Provided directory")

		val correction = name.substringBefore(".json")

		val json = gson.toJson(obj)
		saveText(json, "$correction.json")
	}

	fun saveJson(json: String, name: String) {
		if (name.endsWith('/')) throw IllegalStateException("Illegal file name: $name. Provided directory")

		val correction = name.substringBefore(".json")

		saveText(json, "$correction.json")
	}

	fun getFileNamesList(extension: String, depth: Int? = 1): List<String> {
		val correction = extension.trimStart('.')
		return wdFile
			.walk()
			.let { if (depth != null) it.maxDepth(depth) else it }
			.filter { it.name.endsWith(correction) }
			.map {
				it.name
			}
			.toList()
	}


	private fun getFile(name: String): File {
		if (!wdFile.exists()) throw IllegalStateException("Directory does not exist")
		val file = File("${getAbsolutePath()}/$name")
		if (!file.exists()) throw IllegalStateException("File does not exist, file: " + file.path)
		if (!file.canRead()) throw IllegalStateException("Cannot open file. Is file: " + file.isFile)
		return file
	}


}