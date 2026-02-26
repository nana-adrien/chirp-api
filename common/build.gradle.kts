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
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.jwt.api)
    implementation(libs.jwt.impl)
    implementation(libs.jwt.jackson)
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}