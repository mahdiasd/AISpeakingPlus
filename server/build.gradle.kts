plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
    application
}



group = "ir.speaking"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")

    tasks.withType<Test> {
        useJUnitPlatform()
        maxParallelForks = 1  // Ensure sequential execution
    }
}

ktor {
    docker {
        localImageName.set("ai-speaking-image")
        imageTag.set("latest")
    }
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
    implementation(libs.ktor.server.config.yaml)

    implementation(libs.exposed.core)
    implementation(libs.exposed.crypt)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.postgresql)

    // Koin for Kotlin apps
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)
    implementation("io.insert-koin:koin-annotations:2.3.1")
    ksp("io.insert-koin:koin-ksp-compiler:2.3.1")


    implementation(libs.redisson)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.logging)

    // For encrypt password
    implementation(libs.bcrypt)
    implementation(libs.google.genai)
    implementation(libs.openai.client)
    implementation(libs.firebase.admin)

    testImplementation(libs.ktor.server.test.host)
    testImplementation(kotlin("test-junit5"))
}

