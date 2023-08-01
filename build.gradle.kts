import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm")
	kotlin("plugin.serialization")
	`java-library`
}

group = "com.makki.stockBrain"
version = "1.0-SNAPSHOT"

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	testImplementation(kotlin("test"))

	implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
	implementation("com.google.code.gson:gson:2.8.9")

	implementation("org.apache.commons:commons-math3:3.0")

	implementation("org.jetbrains.kotlinx:multik-core:0.2.2")
	implementation("org.jetbrains.kotlinx:multik-openblas:0.2.2")

	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.5.1")

	testImplementation("org.ejml:ejml-all:0.43")
	testImplementation("org.ujmp:ujmp-core:0.3.0")

	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.6.1")

}

tasks.test {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions.jvmTarget = "16"
}
java {
	sourceCompatibility = JavaVersion.VERSION_16
	targetCompatibility = JavaVersion.VERSION_16
}