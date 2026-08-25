plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.koin.compiler)

}

kotlin {
    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":sharedUI"))
            implementation(project(":navigation"))
            implementation(project(":utils"))
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)

        }
    }
}
