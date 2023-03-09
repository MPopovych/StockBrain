import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "com.makki.stockBrain"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.6.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
    implementation("com.google.code.gson:gson:2.8.9")

    implementation("org.apache.commons:commons-math3:3.0")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-rx2:1.6.1")

}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "18"
}