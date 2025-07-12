plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.intuit.identity.manage"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // For most Kotlin projects (JVM, Native)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}