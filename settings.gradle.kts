rootProject.name = "chirp"

pluginManagement {
    includeBuild("build-logic")
    repositories{
        gradlePluginPortal()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
//include("app")

include("user")
include("chat")
include("notification")
include("common")
include("app")