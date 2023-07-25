rootProject.name = "StockBrain"

pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
	}

	plugins {
		kotlin("jvm").version(extra["kotlin.version"] as String)
		kotlin("plugin.serialization").version(extra["kotlin.version"] as String)
	}
}