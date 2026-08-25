package ir.aispeaking.chat.stt

import ir.aispeaking.utils.dLog
import kotlin.js.JsArray
import kotlin.js.JsNumber
import kotlin.js.Promise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive

/**
 * WebAssembly mic capture using `navigator.mediaDevices.getUserMedia` +
 * Web Audio API.
 */
actual class AudioCapture actual constructor() {

    actual fun start(): Flow<ByteArray> = callbackFlow {
        if (!sttWasmHasGetUserMedia()) {
            close(IllegalStateException("navigator.mediaDevices is not available in this environment"))
            return@callbackFlow
        }

        val mediaStream: JsAny = try {
            sttWasmGetUserMedia().await()
        } catch (t: Throwable) {
            close(IllegalStateException("Microphone access denied: ${t.message ?: t}"))
            return@callbackFlow
        }

        val audioContextCtor: JsAny? = sttWasmAudioContextCtor()
        if (audioContextCtor == null) {
            sttWasmStopStream(mediaStream)
            close(IllegalStateException("Web Audio API is not supported in this browser"))
            return@callbackFlow
        }

        val ctx: JsAny = sttWasmNewAudioContext(audioContextCtor)
        runCatching { sttWasmResumeContext(ctx)?.await() }

        val sourceNode: JsAny = sttWasmCreateMediaStreamSource(ctx, mediaStream)
        val nativeSampleRate: Int = sttWasmSampleRate(ctx)
        "AudioCapture: AudioContext @ ${nativeSampleRate}Hz, downsampling to 16kHz".dLog(tag = "SttAudio")

        val stop: () -> Unit = {
            runCatching { sttWasmStopStream(mediaStream) }
            runCatching { sttWasmCloseContext(ctx) }
        }

        val processor: JsAny? = sttWasmSetupProcessor(
            ctx = ctx,
            sourceNode = sourceNode,
            sampleRateTarget = SAMPLE_RATE_HZ,
            onChunk = { chunk: JsAny ->
                if (isActive) {
                    val jsArray = sttWasmToJsArray(chunk)
                    val len = jsArray.length
                    val bytes = ByteArray(len) { i ->
                        jsArray[i]?.toInt()?.toByte() ?: 0
                    }
                    if (bytes.isNotEmpty()) {
                        trySend(bytes)
                    }
                }
            }
        )

        if (processor == null) {
            stop()
            close(IllegalStateException("Failed to setup audio processor"))
            return@callbackFlow
        }

        awaitClose {
            runCatching { sttWasmDisconnect(processor) }
            runCatching { sttWasmDisconnect(sourceNode) }
            stop()
        }
    }.flowOn(Dispatchers.Default)

    actual fun stop() {
        // The capture's resources live entirely inside the start() flow;
        // cancelling the collector triggers the `awaitClose { stop() }`
        // lambda which releases them.
    }

    actual fun isAvailable(): Boolean = sttWasmHasGetUserMedia()

    companion object {
        private const val SAMPLE_RATE_HZ = 16_000
    }
}

// -----------------------------------------------------------------------------
// Top-level JS-interop helpers.
// -----------------------------------------------------------------------------

@JsFun("() => typeof navigator !== 'undefined' && !!navigator.mediaDevices && !!navigator.mediaDevices.getUserMedia")
private external fun sttWasmHasGetUserMedia(): Boolean

@JsFun("() => { console.log('[STT] Requesting getUserMedia...'); return navigator.mediaDevices.getUserMedia({ audio: { channelCount: 1, echoCancellation: true, noiseSuppression: true, autoGainControl: true } }); }")
private external fun sttWasmGetUserMedia(): Promise<JsAny>

@JsFun("() => (typeof AudioContext !== 'undefined') ? AudioContext : (typeof webkitAudioContext !== 'undefined') ? webkitAudioContext : null")
private external fun sttWasmAudioContextCtor(): JsAny?

@JsFun("(ctor) => new ctor()")
private external fun sttWasmNewAudioContext(ctor: JsAny): JsAny

@JsFun("(ctx) => { try { if (ctx.state === 'suspended') return ctx.resume(); } catch (e) { console.error(e); } return null; }")
private external fun sttWasmResumeContext(ctx: JsAny): Promise<JsAny>?

@JsFun("(ctx, stream) => ctx.createMediaStreamSource(stream)")
private external fun sttWasmCreateMediaStreamSource(ctx: JsAny, stream: JsAny): JsAny

@JsFun("(ctx) => ctx.sampleRate | 0")
private external fun sttWasmSampleRate(ctx: JsAny): Int

@JsFun("(ctx, sourceNode, targetSampleRate, onChunk) => {\n" +
    "    try {\n" +
    "        const bufferSize = 4096;\n" +
    "        const processor = ctx.createScriptProcessor(bufferSize, 1, 1);\n" +
    "        const nativeSampleRate = ctx.sampleRate;\n" +
    "        const ratio = nativeSampleRate / targetSampleRate;\n" +
    "        const chunkDurationMs = 200;\n" +
    "        const targetChunkBytes = Math.floor(targetSampleRate * chunkDurationMs / 1000) * 2;\n" +
    "        let pcmBuffer = new Uint8Array(targetChunkBytes);\n" +
    "        let pcmFilled = 0;\n" +
    "        \n" +
    "        processor.onaudioprocess = (event) => {\n" +
    "            try {\n" +
    "                const inputData = event.inputBuffer.getChannelData(0);\n" +
    "                const inLen = inputData.length;\n" +
    "                const outLen = Math.floor(inLen / ratio);\n" +
    "                \n" +
    "                for (let i = 0; i < outLen; i++) {\n" +
    "                    const srcIdx = Math.floor(i * ratio);\n" +
    "                    if (srcIdx >= inLen) break;\n" +
    "                    let sample = inputData[srcIdx];\n" +
    "                    if (sample > 1.0) sample = 1.0;\n" +
    "                    else if (sample < -1.0) sample = -1.0;\n" +
    "                    \n" +
    "                    const intSample = sample < 0 ? Math.floor(sample * 32768) : Math.floor(sample * 32767);\n" +
    "                    const clamped = Math.max(-32768, Math.min(32767, intSample));\n" +
    "                    \n" +
    "                    if (pcmFilled + 1 < targetChunkBytes) {\n" +
    "                        pcmBuffer[pcmFilled++] = clamped & 0xFF;\n" +
    "                        pcmBuffer[pcmFilled++] = (clamped >> 8) & 0xFF;\n" +
    "                    }\n" +
    "                    \n" +
    "                    if (pcmFilled >= targetChunkBytes) {\n" +
    "                        const copy = new Uint8Array(pcmBuffer);\n" +
    "                        onChunk(copy);\n" +
    "                        pcmFilled = 0;\n" +
    "                    }\n" +
    "                }\n" +
    "            } catch (err) {\n" +
    "                console.error('[STT Audio] Process error:', err);\n" +
    "            }\n" +
    "        };\n" +
    "        \n" +
    "        sourceNode.connect(processor);\n" +
    "        processor.connect(ctx.destination);\n" +
    "        console.log('[STT Audio] Processor connected, ratio:', ratio);\n" +
    "        return processor;\n" +
    "    } catch (e) {\n" +
    "        console.error('[STT Audio] Setup error:', e);\n" +
    "        return null;\n" +
    "    }\n" +
    "}")
private external fun sttWasmSetupProcessor(ctx: JsAny, sourceNode: JsAny, sampleRateTarget: Int, onChunk: (JsAny) -> Unit): JsAny?

@JsFun("(u8Arr) => Array.from(u8Arr)")
private external fun sttWasmToJsArray(u8Arr: JsAny): JsArray<JsNumber>

@JsFun("(node) => { try { node.disconnect(); } catch (e) {} }")
private external fun sttWasmDisconnect(node: JsAny): JsAny

@JsFun("(ctx) => { try { ctx.close(); } catch (e) {} }")
private external fun sttWasmCloseContext(ctx: JsAny): JsAny

@JsFun("(stream) => { try { if (stream && stream.getTracks) stream.getTracks().forEach(function(t) { t.stop(); }); } catch (e) {} }")
private external fun sttWasmStopStream(stream: JsAny): JsAny
