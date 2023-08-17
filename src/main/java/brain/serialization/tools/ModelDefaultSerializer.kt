package brain.serialization.tools

import kotlinx.serialization.json.Json

object ModelDefaultSerializer {
	val defaultPretty = Json {
		prettyPrint = true
		ignoreUnknownKeys = false
	}
	val defaultCompact = Json {
		prettyPrint = false
		ignoreUnknownKeys = false
	}
}