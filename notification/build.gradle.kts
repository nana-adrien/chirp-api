plugins {
    id("java-library")
    id("chirp.kotlin-common")
    id("chirp.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "empire.digiprem"
version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.common)
    implementation(libs.spring.boot.starter.amqp)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}