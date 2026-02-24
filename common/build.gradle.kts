plugins {

    id("java-library")
    id("chirp.kotlin-common")
    id("org.springframework.boot")
    kotlin("plugin.jpa")
}

group = "empire.digiprem"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    api(libs.jackson.module.kotlin)
    api(libs.kotlin.reflect)
    implementation(libs.spring.boot.starter.amqp)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}