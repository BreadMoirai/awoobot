plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
}

group = "com.breadmoirai"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.github.cesarferreira:kotlin-pluralizer:1.0.0")
    implementation("net.dv8tion:JDA:5.0.0-beta.21")
    implementation("club.minnced:jda-ktx:0.11.0-beta.20")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")
    implementation("dev.diceroll:dice-parser:0.3.0")
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("io.mockk:mockk:1.13.12")
    testImplementation(kotlin("test"))
}

tasks.test {
//    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("com.breadmoirai.awoobot.MainKt")
}