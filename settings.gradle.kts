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

include(":feature:auth")
include(":feature:splash")
include(":feature:onboarding")
include(":feature:register")
include(":feature:english_level")
include(":feature:main")
include(":feature:scenarios")
include(":feature:search")
include(":feature:scenario_detail")
include(":feature:chat")
include(":feature:competition")
include(":feature:profile")
include(":feature:edit_profile")
//include(":feature:payment")
include(":feature:purchases")
include(":feature:lightener")
include(":feature:roadmap")


