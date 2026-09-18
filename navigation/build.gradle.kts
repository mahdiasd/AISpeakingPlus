import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.koin.compiler)
}
koinCompiler {
    userLogs = true
    debugLogs = true
    compileSafety = false
}

kotlin {
    android {
        namespace = "ir.aispeaking.navigation"
        compileSdk = 37
        minSdk = 24
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    jvm {
        compilerOptions { jvmTarget = JvmTarget.JVM_17 }
    }

    js { browser() }
    wasmJs { browser() }

    iosArm64()
    iosSimulatorArm64()

    // --- Source Sets & Dependencies ---
    sourceSets {
        commonMain.dependencies {
            implementation(project(":sharedUI"))
            implementation(project(":utils"))
            implementation(project(":data"))
            implementation(project(":domain"))
            implementation(project(":network"))
            implementation(project(":storage"))


            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.compose)

            // --- Serialization ---
            // Required for SerializersModule, polymorphic and NavKey serialization
            implementation(libs.kotlinx.serialization.json)

            // --- Lifecycle & Navigation ---
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
            implementation(libs.compose.nav3)
//            implementation(libs.nav3.browser)

            // --- Compose Multiplatform ---
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
        }
    }
}
