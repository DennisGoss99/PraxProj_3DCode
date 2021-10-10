import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.internal.os.OperatingSystem

plugins {
    kotlin("jvm") version "1.4.32"
    application
}

group = "me.dennis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}


dependencies {

    var lwjglNatives = "natives-windows"
    var lwjglVersion = "3.2.3"
    var jomlVersion = "1.9.24"

    implementation( platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    implementation( "org.lwjgl:lwjgl")
    implementation( "org.lwjgl:lwjgl-assimp")
    implementation( "org.lwjgl:lwjgl-glfw")
    implementation( "org.lwjgl:lwjgl-opengl")
    implementation( "org.lwjgl:lwjgl-stb")
    runtimeOnly( "org.lwjgl:lwjgl::$lwjglNatives")
    runtimeOnly( "org.lwjgl:lwjgl-assimp::$lwjglNatives")
    runtimeOnly( "org.lwjgl:lwjgl-glfw::$lwjglNatives")
    runtimeOnly( "org.lwjgl:lwjgl-opengl::$lwjglNatives")
    runtimeOnly( "org.lwjgl:lwjgl-stb::$lwjglNatives")
    implementation( "org.joml:joml:${jomlVersion}")

    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}