import org.gradle.kotlin.dsl.kotlin

plugins{
    id("chirp.kotlin-common")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}


dependencies{
    "implementation"(libraries.findLibrary("kotlin-reflect").get())
    "implementation"(libraries.findLibrary("kotlin-stdlib").get())
    "implementation"(libraries.findLibrary("spring-boot-starter-webmvc").get())

    "testImplementation"(libraries.findLibrary("spring-boot-start-test").get())
    "testImplementation"(libraries.findLibrary("kotlin-test-junit5").get())
    "testRuntimeOnly"(libraries.findLibrary("junit-platform-launcher").get())
}
