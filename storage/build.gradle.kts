import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.koin.compiler)
}

koinCompiler {
    compileSafety = false
}

kotlin {
    android {
        namespace = "ir.aispeaking.storage"
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

    sourceSets {
        commonMain.dependencies {
            implementation(libs.multiplatform.settings)


            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

            implementation(project(":utils"))

            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.3.0")


        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }
    }
}
