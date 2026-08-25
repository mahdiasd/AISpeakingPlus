import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

// --- Plugins ---
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.koin.compiler)
}

koinCompiler{
    compileSafety = false
}

kotlin {
    // --- Platform Targets ---
    android {
        namespace = "ir.aispeaking.sharedui"
        compileSdk = 37
        minSdk = 24
        androidResources.enable = true
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
            // --- Local Modules ---
            api(project(":domain"))
            api(project(":utils"))

            // --- Compose Multiplatform ---
            api(libs.compose.runtime)
            api(libs.compose.ui)
            api(libs.compose.foundation)
            api(libs.compose.resources)
            api(libs.compose.ui.tooling.preview)
            api(libs.compose.material3)

            // --- Coroutines ---
            api(libs.kotlinx.coroutines.core)

            // --- Ktor (Networking) ---
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.serialization)
            api(libs.ktor.serialization.json)
            api(libs.ktor.client.logging)

            // --- Lifecycle & Navigation ---
            api(libs.androidx.lifecycle.viewmodel)
            api(libs.androidx.lifecycle.runtime)
            api(libs.androidx.lifecycle.viewmodel.navigation3)
            api(libs.compose.nav3)
//            api(libs.nav3.browser)

            // --- Serialization & Data Structures ---
            api(libs.kotlinx.serialization.json)
            api(libs.kotlinx.collections.immutable)

            // --- Image Loading (Coil) ---
            api(libs.coil)
            api(libs.coil.network.ktor)

            // --- Dependency Injection (Koin) ---
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            api(libs.koin.annotations)

            // --- Logging ---
            api(libs.kermit.log)

            // --- Permissions (Calf) ---
            api("com.mohamedrejeb.calf:calf-permissions-core:0.11.0")
            api("com.mohamedrejeb.calf:calf-permissions-microphone:0.11.0")
            api("com.mohamedrejeb.calf:calf-permissions-notifications:0.11.0")

            api(libs.cmp.clipboard)
            api(libs.compottie)

            api("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.8.0")
            api("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.8.0-shaded-core")
            api("tech.annexflow.compose:constraintlayout-compose-multiplatform:0.8.0-shaded")

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.compose.ui.test)
            implementation(libs.kotlinx.coroutines.test)
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.okhttp)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.ktor.client.okhttp)
        }

        webMain.dependencies {
            implementation(libs.nav3.browser)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }

    // --- iOS Framework Configuration ---
    targets
        .withType<KotlinNativeTarget>()
        .matching { it.konanTarget.family.isAppleFamily }
        .configureEach {
            binaries {
                framework {
                    baseName = "SharedUI"
                    isStatic = true
                }
            }
        }

}

// --- Compose Resources Configuration ---
compose.resources {
    publicResClass = true
    packageOfResClass = "ir.aispeaking.sharedui"
}

// --- Android Specific Dependencies ---
dependencies {
    androidRuntimeClasspath(libs.compose.ui.tooling)
}