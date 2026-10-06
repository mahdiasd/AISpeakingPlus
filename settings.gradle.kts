rootProject.name = "AISpeakingPlus"

pluginManagement {
    repositories {
        mavenCentral()
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        gradlePluginPortal()
        maven { url = uri("https://plugins.gradle.org/m2") }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositories {
        maven { url = uri("https://repo.maven.apache.org/maven2") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
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




