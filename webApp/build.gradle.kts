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
            implementation(project(":domain"))
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
            var content = webAppJs.readText()
            val headerPrefix = """const __aiSpeakingScriptSrc = (typeof document !== "undefined" && document.currentScript) ? document.currentScript.src : "";"""
            if (!content.contains("__aiSpeakingScriptSrc")) {
                content = "$headerPrefix\n$content"
            }
            val targetFind = """document.body.appendChild(createScript(hasSupportOfAllRequiredWasmFeatures() ? "originWasmWebApp.js" : "originJsWebApp.js"));"""
            val replacement = """
    if (window.__aiSpeakingAppBooted) return;
    window.__aiSpeakingAppBooted = true;

    const getQuery = () => {
        if (__aiSpeakingScriptSrc && __aiSpeakingScriptSrc.includes("?")) {
            return "?" + __aiSpeakingScriptSrc.split("?")[1];
        }
        return "";
    };
    const query = getQuery();

    const hasJsStringBuiltinsSupport = () => {
        try {
            if (typeof WebAssembly.Tag !== "function") return false;
            const modBytes = new Uint8Array([
                0x00,0x61,0x73,0x6d, 0x01,0x00,0x00,0x00,
                0x01,0x06,0x01,0x60,0x01,0x6f,0x01,0x7f,
                0x02,0x20,0x02,
                0x0e,0x77,0x61,0x73,0x6d,0x3a,0x6a,0x73,0x2d,0x73,0x74,0x72,0x69,0x6e,0x67,
                0x06,0x6c,0x65,0x6e,0x67,0x74,0x68,
                0x00,0x00,
                0x01,0x27,0x01,0x61,0x03,0x6f,0x00
            ]);
            const mod = new WebAssembly.Module(modBytes, { builtins: ["js-string"], importedStringConstants: "'" });
            new WebAssembly.Instance(mod, {});
            return true;
        } catch (e) {
            console.warn("[AISpeaking] Wasm js-string builtins unsupported; using JS fallback:", e);
            return false;
        }
    };

    const useWasm = hasSupportOfAllRequiredWasmFeatures() && hasJsStringBuiltinsSupport();
    if (!useWasm) {
        document.body.appendChild(createScript("originJsWebApp.js" + query));
        return;
    }

    let fallbackLoaded = false;
    let rejectionHandler = null;
    const cleanupRejectionHandler = () => {
        if (rejectionHandler) {
            window.removeEventListener("unhandledrejection", rejectionHandler);
            rejectionHandler = null;
        }
    };
    const loadJsFallback = (err) => {
        if (fallbackLoaded) return;
        fallbackLoaded = true;
        cleanupRejectionHandler();
        console.error("[AISpeaking] Wasm boot failed, falling back to JS:", err);
        document.body.appendChild(createScript("originJsWebApp.js" + query));
    };

    rejectionHandler = (ev) => {
        const reason = ev && ev.reason;
        const isWasmBootError = reason instanceof WebAssembly.LinkError ||
            reason instanceof WebAssembly.CompileError ||
            reason instanceof WebAssembly.RuntimeError;
        const hasCanvas = !!document.querySelector("canvas");
        if (isWasmBootError && !hasCanvas) {
            if (ev && typeof ev.preventDefault === "function") ev.preventDefault();
            loadJsFallback(reason);
        }
    };
    window.addEventListener("unhandledrejection", rejectionHandler);
    setTimeout(cleanupRejectionHandler, 15000);

    const wasmScript = createScript("originWasmWebApp.js" + query);
    wasmScript.onerror = (e) => loadJsFallback(e);
    wasmScript.onload = () => {
        try {
            if (window.webApp && typeof window.webApp.catch === "function") {
                window.webApp.catch((e) => loadJsFallback(e));
            }
        } catch (e) {
            loadJsFallback(e);
        }
    };
    document.body.appendChild(wasmScript);
""".trimIndent()
            if (content.contains(targetFind)) {
                webAppJs.writeText(content.replace(targetFind, replacement))
                logger.lifecycle("Patched webApp.js with js-string builtins detection and JS fallback.")
            } else {
                webAppJs.writeText(content)
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

