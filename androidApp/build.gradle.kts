import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.application)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        FileInputStream(keystorePropertiesFile).use { load(it) }
    }
}

android {
    namespace = "ir.aispeaking.androidApp"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        targetSdk = 36

        applicationId = "ir.aispeaking"
        versionCode = 10
        versionName = "1.2.0"
    }

    signingConfigs {
        if (keystorePropertiesFile.exists()) {
            create("release") {
                keyAlias = keystoreProperties.getProperty("keyAlias", "key0")
                keyPassword = keystoreProperties.getProperty("keyPassword", "")
                val rawStoreFile = keystoreProperties.getProperty("storeFile", "keystore.jks")
                storeFile = if (rootProject.file(rawStoreFile).exists()) {
                    rootProject.file(rawStoreFile)
                } else if (file(rawStoreFile).exists()) {
                    file(rawStoreFile)
                } else {
                    rootProject.file("keystore.jks")
                }
                storePassword = keystoreProperties.getProperty("storePassword", "")
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (keystorePropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
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
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":feature:subscription"))
    implementation(libs.androidx.activityCompose)
    implementation(libs.koin.core)
    implementation(libs.koin.annotations)
    implementation(libs.koin.compose)
    implementation(libs.koin.compose.viewmodel)
    implementation(libs.poolakey)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
}
