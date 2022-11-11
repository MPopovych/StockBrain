package brain.utils

import com.google.gson.Gson
import com.google.gson.JsonElement

inline fun <reified T> Gson.fromJson(json: String): T {
	return fromJson(json, T::class.java)
}

inline fun <reified T> Gson.fromJson(json: JsonElement): T {
	return fromJson(json, T::class.java)
}