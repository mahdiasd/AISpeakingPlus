import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.koin.compiler)

}

android {
    namespace = "ir.aispeaking.androidApp"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 36

        applicationId = "ir.aispeaking.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":sharedUI"))
    implementation(project(":utils"))
    implementation(project(":navigation"))
    implementation(project(":network"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)


}
