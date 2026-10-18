rootProject.name = "AISpeakingPlus"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        google()
        maven {
            url = uri("https://maven.myket.ir")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        google()
        maven {
            url = uri("https://maven.myket.ir")
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        maven { url = uri("https://jitpack.io") }
    }
}
include(":server")
include(":sharedUI")
include(":androidApp")
include(":desktopApp")
include(":webApp")
include(":domain")
include(":data")
include(":utils")
include(":navigation")
include(":network")
include(":storage")

include(":feature:splash")
include(":feature:auth")
include(":feature:main")
include(":feature:stages_list")
include(":feature:chat")
include(":feature:profile")
include(":feature:subscription")
