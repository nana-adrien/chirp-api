plugins {
    id("chirp.spring-boot-app")
    /*alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jpa)*/
}

group = "empire.digiprem"
version = "0.0.1-SNAPSHOT"
description = "chirp-server"

/*
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
*/

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.user)
    implementation(projects.notification)
    implementation(projects.chat)
    implementation(projects.common)
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.mail)
    implementation(libs.spring.boot.starter.data.redis)
    runtimeOnly(libs.postgresql)
}
/*
kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}*/
/*

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
*/
