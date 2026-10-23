plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.koin.compiler)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    port = 8081
                }
            }
        }
        binaries.executable()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            commonWebpackConfig {
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).apply {
                    port = 8081
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":sharedUI"))
            implementation(project(":navigation"))
            implementation(project(":utils"))
            implementation(project(":network"))
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
        }
    }
}

abstract class PatchCompatibilityWebAppJs : DefaultTask() {
    @get:OutputDirectory
    abstract val distDir: DirectoryProperty

    @TaskAction
    fun patch() {
        val webAppJs = distDir.get().asFile.resolve("webApp.js")
        if (webAppJs.exists()) {
            val content = webAppJs.readText()
            val targetFind = """document.body.appendChild(createScript(hasSupportOfAllRequiredWasmFeatures() ? "originWasmWebApp.js" : "originJsWebApp.js"));"""
            val replacement = """
    const currentScriptSrc = (typeof document !== "undefined" && document.currentScript) ? document.currentScript.src : "";
    const getQuery = () => {
        if (currentScriptSrc && currentScriptSrc.includes("?")) {
            return "?" + currentScriptSrc.split("?")[1];
        }
        return "?v=" + Date.now();
    };
    const targetScript = (hasSupportOfAllRequiredWasmFeatures() ? "originWasmWebApp.js" : "originJsWebApp.js") + getQuery();
    document.body.appendChild(createScript(targetScript));
""".trimIndent()
            if (content.contains(targetFind)) {
                webAppJs.writeText(content.replace(targetFind, replacement))
                logger.lifecycle("Patched webApp.js with cache-buster forwarding.")
            }
        }
    }
}

val patchCompatibilityWebAppJs = tasks.register<PatchCompatibilityWebAppJs>("patchCompatibilityWebAppJs") {
    distDir.set(layout.buildDirectory.dir("dist/composeWebCompatibility/productionExecutable"))
    mustRunAfter("composeCompatibilityBrowserDistribution")
}

tasks.named("composeCompatibilityBrowserDistribution") {
    finalizedBy(patchCompatibilityWebAppJs)
}

