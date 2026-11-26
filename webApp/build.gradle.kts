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

    const dbg = (msg) => {
        try {
            if (typeof window.__aiDebugLog === "function") window.__aiDebugLog(msg);
        } catch (_) {}
    };
    const setMode = (mode) => {
        try {
            if (typeof window.__aiDebugSetMode === "function") window.__aiDebugSetMode(mode);
        } catch (_) {}
    };

    const getQuery = () => {
        if (__aiSpeakingScriptSrc && __aiSpeakingScriptSrc.includes("?")) {
            return "?" + __aiSpeakingScriptSrc.split("?")[1];
        }
        return "";
    };
    const query = getQuery();

    const forceJs = typeof window !== "undefined" && window.location && (window.location.search || "").indexOf("force_js=1") !== -1;

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

    const hasReqWasm = hasSupportOfAllRequiredWasmFeatures();
    const hasJsStr = hasReqWasm ? hasJsStringBuiltinsSupport() : false;
    dbg("boot: reqWasm=" + hasReqWasm + " jsStr=" + hasJsStr + " forceJs=" + forceJs);

    if (forceJs || !hasReqWasm || !hasJsStr) {
        setMode(forceJs ? "js (forced)" : "js (compat)");
        dbg("loading originJsWebApp.js" + query);
        const jsScript = createScript("originJsWebApp.js" + query);
        jsScript.onload = () => dbg("originJsWebApp.js loaded");
        jsScript.onerror = (e) => dbg("originJsWebApp.js error: " + (e && e.message ? e.message : "load failed"));
        document.body.appendChild(jsScript);
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
        setMode("js (fallback)");
        dbg("fallback to JS: " + (err && (err.message || err) ? (err.message || String(err)) : "unknown"));
        console.error("[AISpeaking] Wasm boot failed, falling back to JS:", err);
        const jsScript = createScript("originJsWebApp.js" + query);
        jsScript.onload = () => dbg("originJsWebApp.js (fallback) loaded");
        jsScript.onerror = (e) => dbg("originJsWebApp.js (fallback) error");
        document.body.appendChild(jsScript);
    };

    const hasAnyCanvas = () => {
        if (document.querySelector("canvas")) return true;
        const els = document.querySelectorAll("*");
        for (let i = 0; i < els.length; i++) {
            if (els[i].shadowRoot && els[i].shadowRoot.querySelector("canvas")) return true;
        }
        return false;
    };

    rejectionHandler = (ev) => {
        const reason = ev && ev.reason;
        dbg("unhandledrejection: " + (reason && (reason.message || reason) ? (reason.message || String(reason)) : "unknown"));
        const isWasmBootError = reason instanceof WebAssembly.LinkError ||
            reason instanceof WebAssembly.CompileError ||
            reason instanceof WebAssembly.RuntimeError;
        if (isWasmBootError && !hasAnyCanvas()) {
            if (ev && typeof ev.preventDefault === "function") ev.preventDefault();
            loadJsFallback(reason);
        }
    };
    window.addEventListener("unhandledrejection", rejectionHandler);
    setTimeout(cleanupRejectionHandler, 15000);

    setMode("wasm");
    dbg("loading originWasmWebApp.js" + query);
    const wasmScript = createScript("originWasmWebApp.js" + query);
    wasmScript.onerror = (e) => {
        dbg("originWasmWebApp.js script error");
        loadJsFallback(e);
    };
    wasmScript.onload = () => {
        dbg("originWasmWebApp.js loaded");
        try {
            if (window.webApp && typeof window.webApp.then === "function") {
                window.webApp.then(() => dbg("wasm promise resolved"));
            }
            if (window.webApp && typeof window.webApp.catch === "function") {
                window.webApp.catch((e) => {
                    dbg("wasm promise rejected: " + (e && (e.message || e) ? (e.message || String(e)) : "unknown"));
                    loadJsFallback(e);
                });
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

