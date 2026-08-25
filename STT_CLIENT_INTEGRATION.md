# Speech-to-Text (STT) Integration Guide
## For Compose Multiplatform Client Implementation

This document describes the streaming Speech-to-Text service added to the
AiSpeakingKtor backend so that an AI agent (or human developer) can implement
the client side inside the **AiSpeakingMultiplatform** Compose Multiplatform
app (Android, iOS, Desktop, Web/JS-WASM) without re-reading the server source.

---

## 1. Overview

The backend runs **sherpa-onnx** (k2-fsa) for real-time English speech
recognition. A Ktor WebSocket endpoint at `/api/v1/stt` (authenticated via
user JWT) receives streaming audio from the client and returns partial/final
transcription results as JSON text frames.

Key facts for the client developer:

- **Model**: `streaming-zipformer-en-2023-06-21` (int8 transducer), English
  only, **streaming** (online recognition — results appear while the user is
  still speaking).
- **Audio format**: **mono, 16 kHz, PCM 16-bit signed, little-endian** (PCM16).
  The client MUST resample audio to 16 000 Hz and send it as binary WebSocket
  frames.
- **Transport**: a single persistent WebSocket connection per recognition
  session. Binary frames carry audio (client → server); text frames carry JSON
  messages (server → client).
- **Capacity**: the server allows a small number of concurrent sessions
  (default 2). If the server is full, the connection is rejected with an
  `error` message (see §4.3).
- **Authentication**: the route is behind the user-JWT `authenticate { }`
  block. The client must send its JWT (see §2 "Authentication").
---

## 2. Endpoint URL

| Environment | WebSocket URL |
|-------------|---------------|
| Local dev | `ws://localhost:8080/api/v1/stt` |
| Staging | `wss://staging.aispeaking.ir/api/v1/stt` |

The route is registered at `/api/v1/stt` (matches the convention used by every
other feature: `/api/v1/...`). nginx already proxies the whole `/api/` prefix
to the backend with WebSocket upgrade headers and a 24 h read timeout, so no
extra nginx configuration is needed.

> The first version of this code used `/ws/stt` (outside `/api/`), which nginx
> did not proxy — so it was unreachable through the reverse proxy. Moving it
> under `/api/v1/` fixed both the proxy gap and the routing-consistency issue.

### Authentication

The `/api/v1/stt` route is wrapped in `authenticate(MyConstant.USER_JWT_NAME)`,
so a valid user JWT is **required** to open the WebSocket. The client must
pass the token during the WebSocket handshake. Because browsers cannot set
custom headers on a `WebSocket` connection, the convention used on this
project is to send the token as a **subprotocol** or as a **query parameter**:

```
ws://localhost:8080/api/v1/stt?token=<USER_JWT>
```

> **Note**: the server currently uses the standard `Authorization` header for
> its JWT `authenticate { ... }` block (validated via `Ktor's JWT verifier`).
> WebSocket clients that CAN set headers (Android/OkHttp, iOS URLSession,
> Desktop JVM) should send `Authorization: Bearer <token>`. The browser JS/WASM
> client cannot set headers on a raw `WebSocket`, so for the web target the
> simplest approach is to add a query-param route that extracts the token — or
> use a thin authenticated REST endpoint that returns a short-lived single-use
> WS ticket. Choose one before implementing the browser client.


---

## 3. WebSocket Protocol

### 3.1 Client → Server (Binary Frames)

Send raw **PCM16** bytes (little-endian signed 16-bit) as binary WebSocket
frames.

- **Sample rate**: 16 000 Hz (MUST match; the server expects 16 kHz).
- **Channels**: mono (1 channel).
- **Chunk size**: ~100–300 ms of audio per frame, i.e. 3 200–9 600 samples
  (6 400–19 200 bytes per frame). Sending larger frames (up to 16 000 samples
  = 1 s) is accepted but not recommended for latency.
- **Do NOT send a WAV header.** Send the raw PCM16 sample bytes only. The
  server reads the frame payload directly as `short[]` → `float[-1.0, 1.0]`.
- To signal end-of-stream, close the WebSocket (or send an empty binary frame
  — the server ignores empty frames but it is a harmless sentinel).

### 3.2 Server → Client (Text Frames — JSON)

Every message is a JSON object with a `type` discriminator field.

The complete sealed type (defined in
`src/main/kotlin/ir/speaking/feature/stt/dto/SttMessage.kt`):

```kotlin
@Serializable
sealed interface SttMessage {
    @Serializable @SerialName("partial") data class PartialTranscript(val text: String)
    @Serializable @SerialName("final")   data class FinalTranscript(val text: String)
    @Serializable @SerialName("error")   data class ErrorMessage(val message: String)
    @Serializable @SerialName("ready")   data class ReadyMessage(val message: String = "...")
}
```

| `type` | Fields | When sent | Client action |
|--------|--------|-----------|----------------|
| `ready` | `message: String` | Once, immediately after the WS connects and a stream slot is acquired | Start sending audio. Do NOT send audio before this. |
| `partial` | `text: String` | After each decode step, while the user is still speaking | Show `text` as the live interim transcription (update in place). |
| `final` | `text: String` | When the endpoint detector fires (silence / utterance boundary) | Append `text` to the committed transcript. The stream auto-resets for the next utterance. |
| `error` | `message: String` | Capacity reached (`"server_busy"`) | Show a "try again later" message. The server closes the connection right after. |

### 3.3 Example Wire JSON

```jsonc
// 1. On connect:
{"type":"ready","message":"Stream ready. Send mono PCM16 audio as binary frames."}

// 2. While user speaks:
{"type":"partial","text":"hello"}
{"type":"partial","text":"hello world"}

// 3. After a pause (endpoint):
{"type":"final","text":"hello world"}

// 4. If server is full:
{"type":"error","message":"server_busy"}
```

### 3.4 Sequence Diagram

```
 Client                              Server
   |                                    |
   |--- WS CONNECT ws(s)://.../api/v1/stt (auth) --> |  (server allocates an OnlineStream)
   |<--- {"type":"ready",...} --------- |  (commit: you may send audio now)
   |                                    |
   |=== binary PCM16 (200ms) ==========>|
   |<--- {"type":"partial","text":".."}-|  (interim result)
   |=== binary PCM16 (200ms) ==========>|
   |<--- {"type":"partial","text":"..."}|
   |     ... (user pauses) ...          |
   |<--- {"type":"final","text":"..."} -|  (endpoint detected; stream resets)
   |                                    |
   |=== binary PCM16 (200ms) ==========>|  (next utterance begins)
   |<--- {"type":"partial","text":".."}-|
   |                                    |
   |--- WS CLOSE --------------------->|  (server frees the OnlineStream)
```

---

## 4. JSON Schema (for parsing on the client)

Use a kotlinx.serialization sealed type mirroring the server's `SttMessage`.

### 4.1 Shared `commonMain` model

Put this in `commonMain` so all targets (Android/iOS/Desktop/Web) share it:

```kotlin
@Serializable
sealed interface SttMessage {
    @Serializable @SerialName("ready")   data class Ready(val message: String)
    @Serializable @SerialName("partial") data class Partial(val text: String)
    @Serializable @SerialName("final")   data class Final(val text: String)
    @Serializable @SerialName("error")   data class Error(val message: String)
}
```

### 4.2 Recommended Json config

```kotlin
val sttJson = Json {
    encodeDefaults = true
    explicitNulls = false
    classDiscriminator = "type"
    ignoreUnknownKeys = true  // forward-compatible if the server adds fields
}
```

### 4.3 Exact contract (from server serialization tests)

These shapes are asserted by `SttMessageSerializationTest` and are guaranteed
stable:

```json
{"type":"ready","message":"Stream ready. Send mono PCM16 audio as binary frames."}
{"type":"partial","text":"hello world"}
{"type":"final","text":"the quick brown fox"}
{"type":"error","message":"server_busy"}
```

The server uses `Json { explicitNulls = false }`, so optional/empty fields are
omitted.

---

## 5. Audio Capture Requirements

### 5.1 Format summary

| Property | Value |
|----------|-------|
| Sample rate | 16 000 Hz |
| Channels | 1 (mono) |
| Bit depth | 16-bit signed |
| Endianness | Little-endian |
| Encoding | Raw PCM (no WAV/RIFF header) |
| Frame content | Sample bytes only |

### 5.2 Resampling

Most platform audio APIs default to 44.1 kHz or 48 kHz. You MUST resample to
16 kHz before sending. Libraries by target:

| Target | Capture API | Resampling approach |
|--------|-------------|---------------------|
| Android | `AudioRecord` (MediaRecorder.AudioSource.MIC) | `AudioRecord` supports 16 kHz directly — request it natively. |
| iOS | AVAudioEngine + AVAudioInputNode | Set `AVAudioFormat(44100/48000)`, then convert with `AVAudioConverter` to 16 kHz mono. |
| Desktop (JVM) | Java `TargetDataLine` | Request 16 kHz directly from `AudioFormat`. |
| Web (JS/WASM) | `navigator.mediaDevices.getUserMedia` + Web Audio API | `AudioContext` at native rate, then `ScriptProcessorNode` or `AudioWorklet` to downsample to 16 kHz. |

All platforms: convert captured samples to **little-endian PCM16 `ByteArray`**
before sending as a binary WebSocket frame.

### 5.3 Chunk cadence

- Send a frame every ~200 ms (3 200 samples = 6 400 bytes).
- Do not buffer more than ~400 ms — latency matters for live transcription.
- Avoid sending zero/empty frames except as an end-of-stream sentinel.

For testing, a 16 kHz mono PCM16 WAV file can be streamed directly (strip the
44-byte WAV header, then send the remaining body in 6 400-byte chunks).

---

## 6. Compose Multiplatform Client — Architecture

### 6.1 Layer overview

```
commonMain/
  stt/
    SttMessage.kt            (sealed model + JSON config — §4)
    SttClient.kt             (platform-agnostic WS client, Flow-based)
    SttController.kt          (state holder → drives Compose UI)
    AudioCapture.kt          (expect — platform mic capture → PCM16 Flow)
    AudioResampler.kt        (expect — if needed)
  SttScreen.kt               (Composable: mic button + live transcript)

androidMain/
  stt/AudioCapture.kt         (actual: AudioRecord @ 16 kHz)
iosMain/
  stt/AudioCapture.kt         (actual: AVAudioEngine + AVAudioConverter)
desktopMain/
  stt/AudioCapture.kt         (actual: TargetDataLine @ 16 kHz)
jsMain / wasmJsMain/
  stt/AudioCapture.kt         (actual: getUserMedia + AudioWorklet)
```

### 6.2 WebSocket client (commonMain)

There is no officially cross-platform WebSocket client in the Ktor client
family that covers all four targets (Android, iOS, Desktop, JS/WASM) with
binary frame support yet. Recommended approach:

| Target | WebSocket implementation |
|--------|--------------------------|
| Android, Desktop (JVM) | Ktor `WebSocketClient` (CIO engine) — supports binary frames. |
| iOS | Ktor `WebSocketClient` (Darwin engine) — supports binary frames. |
| JS / WASM | `WebSocket` browser API directly (Ktor JS WS support is limited for binary). |

The pattern for the client:

```kotlin
// commonMain — SttClient.kt
class SttClient(
    private val url: String,
    private val json: Json,
    private val authToken: String,   // user JWT — sent as Authorization header (native) or via ?token= (web)
) {
    // Emits server messages; consume audio to send as raw PCM16 bytes.
    fun session(
        audioInput: Flow<ByteArray>,   // PCM16 frames from mic
    ): Flow<SttMessage> = channelFlow {
        // platform WS connect (expect/actual) — pass the token per target
        wsConnect(url, authToken) { send, receive ->
            // wait for ready
            val first = json.decodeFromString(SttMessage.serializer(), receive())
            trySend(first)  // Ready
            if (first is SttMessage.Error) { close(); return@wsConnect }

            // pump audio -> binary frames
            launch {
                audioInput.collect { bytes -> send(bytes) }
            }
            // pump server -> messages
            for (text in incomingText()) {
                trySend(json.decodeFromString(SttMessage.serializer(), text))
            }
        }
        awaitClose()
    }
}
```

### 6.3 State + UI (Compose)

Drive the UI from a `StateFlow<SttUiState>`:

```kotlin
data class SttUiState(
    val connected: Boolean = false,
    val partial: String = "",
    val final: List<String> = emptyList(),  // committed utterances
    val error: String? = null,
)

@Composable
fun SttScreen(url: String) {
    val controller = remember { SttController(url) }
    val state by controller.uiState.collectAsState()
    // mic toggle button -> controller.start()/stop()
    // display state.partial (live) above state.final.joinToString()
}
```

`SttController`:
- `start()`: open a microphone `Flow<ByteArray>` (16 kHz PCM16) and a WebSocket
  session; on each `SttMessage` update `uiState`.
- `stop()`: cancel the audio flow and close the WS (the server frees resources
  automatically).

### 6.4 Handling messages

```kotlin
when (msg) {
    is SttMessage.Ready   -> uiState.update { it.copy(connected = true, error = null) }
    is SttMessage.Partial -> uiState.update { it.copy(partial = msg.text) }
    is SttMessage.Final   -> uiState.update { it.copy(
        partial = "",
        final = it.final + msg.text,
    )}
    is SttMessage.Error   -> uiState.update { it.copy(error = msg.message, connected = false) }
}
```

---

## 7. Error Handling & Edge Cases

| Scenario | What happens | Client should |
|----------|--------------|---------------|
| Server at capacity (2 streams busy) | Server sends `{"type":"error","message":"server_busy"}` then closes the WS. | Show "Server is busy, please try again" and allow retry. |
| Client disconnects abruptly | Server frees the stream in a `finally` block — no leak. | Nothing special. |
| WebSocket idle timeout | Server-side ping 15 s, timeout 15 s. If the client stops responding, the server closes. | Ensure OVOPD/pings are handled by the WS library (usually automatic). |
| Audio not 16 kHz | Recognition quality will degrade or be garbage. | Resample before sending (§5.2). |
| Audio with wrong endianness or WAV header | Samples are misread → garbage output. | Send raw little-endian PCM16 only (strip any header). |
| Server restarts / connection drops mid-session | The WS closes. | Detect close, show reconnect option, discard partial results. |

---

## 8. nginx / Deployment Notes

The staging nginx (`nginx/application.conf`) proxies `/api/` to the backend
with WebSocket upgrade headers and `proxy_read_timeout 86400s`. Because the
STT route is now registered at `/api/v1/stt`, it is **already proxied** by
the existing `location /api/` block — no extra nginx configuration is needed.

The existing `/api/` block already includes the critical WebSocket directives
(`proxy_http_version 1.1`, `Upgrade`/`Connection` headers, 24 h read+send
timeout). For reference, the required directives (all already present):

```nginx
location /api/ {
    proxy_pass http://127.0.0.1:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "upgrade";
    proxy_read_timeout 86400s;
    proxy_send_timeout 86400s;
}
```

For SSL (staging.aispeaking.ir uses certbot on port 443), the WebSocket must
use `wss://` — the `location /api/` block must exist in the HTTPS `server`
section too (it does in the current config). Most browsers block `ws://`
(insecure) from `https://` pages.

---

## 9. Open Decisions (confirm before implementing client)

1. **Token transport for the browser (JS/WASM) target**: the server uses the
   standard `Authorization: Bearer <token>` header for JWT verification. The
   native Android (OkHttp), iOS (URLSession), and Desktop (JVM CIO) WebSocket
   clients CAN set custom headers during the handshake — so those targets
   just send the `Authorization` header. The browser `WebSocket` API CANNOT
   set custom headers, so for the web target you must pick one:
   - Pass `?token=<jwt>` as a query param and add a second route (or a custom
     `authentication` function) that reads the token from the query string.
   - Use a subprotocol: `new WebSocket(url, ["bearer.<jwt>"])` and read it
     from the `Sec-WebSocket-Protocol` header on the server side.
   - Issue a short-lived single-use ticket via an authenticated REST call,
     then connect with `?ticket=<ticket>`.
   Until you pick one, the browser target cannot authenticate against `/api/v1/stt`.
   (The native targets already work with the `Authorization` header.)
2. **Concurrency UX**: with only 2 concurrent streams, the app should
   gracefully handle `server_busy` — e.g. queue, retry with backoff, or show
   "please wait" in the UI.
3. **End-of-stream signal**: the server currently relies on WS close to free
   the stream. Decide whether to send an explicit empty binary frame
   sentinel (currently a no-op but harmless) or just close the WebSocket.

---

## 10. Server-Side File Reference

For the AI implementing the client, these are the authoritative server files:

| File | Purpose |
|------|---------|
| `src/main/kotlin/ir/speaking/feature/stt/dto/SttMessage.kt` | JSON wire types (the contract). |
| `src/main/kotlin/ir/speaking/feature/stt/routing/SttRouting.kt` | WebSocket handler: frame loop, PCM16 decode, partial/final logic. |
| `src/main/kotlin/ir/speaking/feature/stt/service/SttService.kt` | Singleton: owns `OnlineRecognizer`, manages streams + concurrency. |
| `src/main/kotlin/ir/speaking/feature/stt/service/SttConfig.kt` | Config from env vars (`STT_*`). |
| `src/main/kotlin/ir/speaking/feature/stt/service/StreamConcurrencyLimiter.kt` | Semaphore-based concurrency cap. |
| `src/main/kotlin/ir/speaking/core/WebSockets.kt` | Ktor WS plugin config (ping 15 s, timeout 15 s). |
| `src/main/kotlin/ir/speaking/core/Routing.kt` | Route registration — `sttRouting()` is the last entry. |
| `src/main/resources/application.yaml` | `stt:` config block (env vars take precedence at runtime). |
| `Dockerfile` | Downloads JNI `.so` files + model at build time; `-Dsherpa_onnx.native.path=/app/native`. |
| `docker-compose.yml` | `STT_*` env vars passed to the container. |
| `nginx/application.conf` | Reverse proxy config (already proxies `/api/` incl. WS upgrade headers, so `/api/v1/stt` works through it). |
| `src/test/.../SttMessageSerializationTest.kt` | Asserts the exact JSON shapes (the wire contract test). |
| `src/test/.../StreamConcurrencyLimiterTest.kt` | Concurrency-limit edge cases (8 tests, pure JVM). |

---

## 11. Quick Test (Python reference client)

To verify the server works before writing any Kotlin:

```python
import asyncio, json, websockets, wave

async def test(path="test.wav"):
    async with websockets.connect(
        "ws://localhost:8080/api/v1/stt",
        extra_headers={"Authorization": "Bearer <USER_JWT>"},
    ) as ws:
        print(json.loads(await ws.recv()))          # {"type":"ready",...}
        with wave.open(path, "rb") as wf:
            assert wf.getframerate() == 16000 and wf.getnchannels() == 1
            # skip 44-byte header implicitly (readframes returns raw PCM)
            while True:
                chunk = wf.readframes(3200)         # 200 ms
                if not chunk: break
                await ws.send(chunk)                 # binary frame
                try:
                    while True:
                        print(json.loads(await asyncio.wait_for(ws.recv(), 0.05)))
                except asyncio.TimeoutError:
                    pass

asyncio.run(test())
```

Expected output: a `ready` message, then a stream of `partial` updates,
then `final` messages around pauses.

---

## 12. Migration Checklist (for the AI)

- [ ] Add `SttMessage` sealed model in `commonMain` (§4.1).
- [ ] Add `expect class AudioCapture` in `commonMain` with `actual`
      implementations per target (§5.2, §6.1).
- [ ] Implement a WebSocket client per target (§6.2) using the platform's
      native WS if Ktor's doesn't support binary on that target.
- [ ] Native targets (Android/iOS/Desktop): send `Authorization: Bearer <jwt>`
      header during the WS handshake (§2 Authentication).
- [ ] Browser target (JS/WASM): pick a token-transport option from §9.1 and add
      the server-side support for it.
- [ ] Implement `SttController` driving `StateFlow<SttUiState>` (§6.3–6.4).
- [ ] Build `SttScreen` Compose UI (mic toggle + live transcript).
- [ ] Handle `server_busy` in the UI (§7).
- [ ] Connect to `ws(s)://<host>/api/v1/stt` (NOT `/ws/stt`).
