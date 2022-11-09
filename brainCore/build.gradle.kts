import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"

    application
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
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}