package ir.aispeaking.chat.stt

import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform microphone capture that emits raw mono PCM16 (16 kHz,
 * little-endian, signed 16-bit) bytes ready to be sent as binary WebSocket
 * frames to the server-side STT endpoint.
 *
 * Server contract (§5.1 of STT_CLIENT_INTEGRATION.md): mono, 16 000 Hz,
 * PCM 16-bit signed, little-endian, no WAV header.
 *
 * The actuals differ per target:
 *  - Android uses `AudioRecord` at 16 kHz directly (no resampling needed).
 *  - JS / WASM use `navigator.mediaDevices.getUserMedia` + Web Audio API
 *    and downsample the AudioContext rate (usually 44.1/48 kHz) to 16 kHz.
 *  - iOS / Desktop JVM are currently no-ops (STT is only needed on Android
 *    and Web for the chat mic button; tapping the button on those targets
 *    surfaces an error to the user).
 *
 * Each frame emitted by the flow MUST be ~100–300 ms of audio (server
 * recommended cadence is ~200 ms / 6 400 bytes).
 *
 * Constructor is zero-arg on every target. Android reaches its [Context]
 * via [AndroidAudioCapture.attach] from `Activity.onCreate`; other targets
 * need no setup.
 */
expect class AudioCapture() {

    /**
     * Start capturing audio and emit PCM16 little-endian byte chunks.
     *
     * The returned flow is single-shot: collecting once plays one capture
     * session. Callers are expected to cancel the collector (and call
     * [stop]) to end the session — at which point any final bytes still
     * in the AudioRecord buffer should be flushed before the flow completes.
     */
    fun start(): Flow<ByteArray>

    /**
     * Stop the active capture session, releasing the mic. Idempotent.
     * Must be called when the host [Flow] is cancelled to free the device.
     */
    fun stop()

    /**
     * Returns true if a microphone is available AND the app currently holds
     * the RECORD_AUDIO permission / browser getUserMedia grant.
     */
    fun isAvailable(): Boolean
}
