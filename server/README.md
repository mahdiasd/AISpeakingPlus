# Ai Speaking

This project was created using the [Ktor Project Generator](https://start.ktor.io).

Here are some useful links to get you started:

- [Ktor Documentation](https://ktor.io/docs/home.html)
- [Ktor GitHub page](https://github.com/ktorio/ktor)
- The [Ktor Slack chat](https://app.slack.com/client/T09229ZC6/C0A974TJ9). You'll need
  to [request an invite](https://surveys.jetbrains.com/s3/ktor-slack-sign-up) to join.

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
|------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Static Content](https://start.ktor.io/p/static-content)               | Serves static files from defined locations                                         |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [WebSockets](https://start.ktor.io/p/ktor-websockets)                  | Adds WebSocket protocol support for bidirectional client connections               |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```

---

## Streaming Speech-to-Text (sherpa-onnx)

The server includes a WebSocket endpoint at `/api/v1/stt` (authenticated
via user JWT) that performs real-time English speech recognition using
[sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)
with the `streaming-zipformer-en-2023-06-21` int8 model.

### Architecture

```
                   WebSocket /api/v1/stt (JWT-auth)
                          |
                          v
   +-------------------------------------------+
   |              SttService (Singleton)        |
   |                                            |
   |  OnlineRecognizer  (loaded once, shared)   |
   |  StreamConcurrencyLimiter (Semaphore)      |
   |                                            |
   |  tryAcquireStream()  ->  OnlineStream?     |
   |  releaseStream()     ->  frees native ptr  |
   +-------------------------------------------+
          |                          |
     acquire ok               acquire null (busy)
          |                          |
   create OnlineStream      send {"type":"error",
   feed PCM16 audio         "message":"server_busy"}
   decode + getText          close connection
   detect endpoint
   send partial/final JSON
```

Key design points:

- The native ONNX model is loaded **exactly once** at startup (Koin `@Single`)
  and shared across all sessions.
- Each WebSocket connection gets its own `OnlineStream` from the shared
  `OnlineRecognizer`.
- A fair `Semaphore` limits concurrent streams (default 2, configurable via
  `STT_MAX_STREAMS`). When the limit is reached, new connections receive
  `{"type":"error","message":"server_busy"}` and are closed.
- The `OnlineStream` is **always** released in a `finally` block, so native
  resources are freed even on abrupt disconnect (TCP reset, crash, timeout).

### WebSocket Protocol

**Authentication:** the `/api/v1/stt` route is behind `authenticate(USER_JWT)`.
The client must send `Authorization: Bearer <JWT>` as a header during the
WebSocket handshake (native targets: Android/iOS/Desktop JVM set it directly;
browser JS/WASM need a query-param/subprotocol workaround — see
`docs/STT_CLIENT_INTEGRATION.md` §9.1).

**Client -> Server:** Binary frames containing **mono 16 kHz PCM16**
(little-endian signed 16-bit) audio. Send chunks of ~100-300 ms
(3,200-9,600 samples per frame).

**Server -> Client:** Text frames (JSON) with these shapes:

| Message | JSON | When |
|---------|------|------|
| Ready   | `{"type":"ready","message":"Stream ready..."}` | Once, after stream acquired |
| Partial | `{"type":"partial","text":"..."}` | Interim recognition result |
| Final   | `{"type":"final","text":"..."}` | After endpoint (silence / utterance boundary) |
| Error   | `{"type":"error","message":"server_busy"}` | Capacity reached; connection closed |

### Configuration

All STT settings are read from **environment variables** (with fallbacks):

| Variable | Default | Description |
|----------|---------|-------------|
| `STT_MODEL_DIR` | `/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21` | Directory with encoder/decoder/joiner .onnx + tokens.txt |
| `STT_MAX_STREAMS` | `2` | Max concurrent WebSocket STT sessions |
| `STT_NUM_THREADS` | `1` | Inference threads per recognizer (keep low on 2-4 vCPU) |
| `STT_SAMPLE_RATE` | `16000` | Audio sample rate in Hz |
| `STT_RULE1_TRAILING` | `2.4` | rule1 min trailing silence (seconds) |
| `STT_RULE2_TRAILING` | `1.4` | rule2 min trailing silence (seconds) |
| `STT_RULE3_UTTERANCE` | `20.0` | rule3 max utterance length (seconds) |

The same configuration is also available in `application.yaml` under the `stt:`
key for documentation purposes, but **environment variables take precedence**
at runtime.

### Local Development

#### 1. Download the model

```bash
mkdir -p ~/models
cd ~/models
curl -L -o model.tar.bz2 \
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/asr-models/sherpa-onnx-streaming-zipformer-en-2023-06-21.tar.bz2"
tar xjf model.tar.bz2
rm model.tar.bz2
```

This extracts a directory `sherpa-onnx-streaming-zipformer-en-2023-06-21/`
containing:

```
encoder-epoch-99-avg-1.int8.onnx
decoder-epoch-99-avg-1.onnx
joiner-epoch-99-avg-1.int8.onnx
tokens.txt
```

#### 2. Download native libraries (JNI)

The sherpa-onnx Java API needs platform-specific `.so` (Linux), `.dylib` (macOS),
or `.dll` (Windows) files.

**For Linux x86_64 (production / Docker):**

```bash
mkdir -p ~/sherpa-native
SHERPA_VERSION=1.13.4
curl -L -o jni.tar.bz2 \
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/v${SHERPA_VERSION}/sherpa-onnx-v${SHERPA_VERSION}-linux-x64-jni.tar.bz2"
tar xjf jni.tar.bz2
cp sherpa-onnx-v${SHERPA_VERSION}-linux-x64-jni/lib/*.so ~/sherpa-native/
rm -rf jni.tar.bz2 sherpa-onnx-v${SHERPA_VERSION}-linux-x64-jni
```

**For macOS (local dev on Apple Silicon):**

```bash
mkdir -p ~/sherpa-native
SHERPA_VERSION=1.13.4
curl -L -o jni.tar.bz2 \
  "https://github.com/k2-fsa/sherpa-onnx/releases/download/v${SHERPA_VERSION}/sherpa-onnx-v${SHERPA_VERSION}-osx-arm64-jni.tar.bz2"
tar xjf jni.tar.bz2
cp sherpa-onnx-v${SHERPA_VERSION}-osx-arm64-jni/lib/*.dylib ~/sherpa-native/
rm -rf jni.tar.bz2 sherpa-onnx-v${SHERPA_VERSION}-osx-arm64-jni
```

#### 3. Run the server locally

```bash
export STT_MODEL_DIR="$HOME/models/sherpa-onnx-streaming-zipformer-en-2023-06-21"
export STT_MAX_STREAMS=2
export STT_NUM_THREADS=1

./gradlew run -Dsherpa_onnx.native.path="$HOME/sherpa-native"
```

The `-Dsherpa_onnx.native.path` JVM flag tells the sherpa-onnx `LibraryLoader`
where to find `libsherpa-onnx-jni.so` / `libsherpa-onnx-jni.dylib` and
`libonnxruntime.so` / `libonnxruntime.dylib`.

#### 4. Test with a WebSocket client

You can use any WebSocket client that sends binary PCM16 frames. Here is a
quick Python example:

```python
import websockets, json, struct, wave, asyncio

async def stt_client(wav_path):
    async with websockets.connect(
        "ws://localhost:8080/api/v1/stt",
        extra_headers={"Authorization": "Bearer <USER_JWT>"},
    ) as ws:
        msg = json.loads(await ws.recv())
        print("Server:", msg)  # {"type":"ready", ...}

        with wave.open(wav_path, "rb") as wf:
            channels = wf.getnchannels()
            sample_width = wf.getsampwidth()
            sample_rate = wf.getframerate()
            assert channels == 1 and sample_width == 2 and sample_rate == 16000

            chunk_ms = 200
            chunk_samples = int(sample_rate * chunk_ms / 1000)
            while True:
                data = wf.readframes(chunk_samples)
                if not data:
                    break
                await ws.send(data)  # binary frame

                # Read any pending responses (non-blocking)
                try:
                    while True:
                        text = await asyncio.wait_for(ws.recv(), timeout=0.05)
                        print("Server:", json.loads(text))
                except asyncio.TimeoutError:
                    pass

        # Signal end of audio
        await ws.send(b"")
        try:
            while True:
                text = await asyncio.wait_for(ws.recv(), timeout=1.0)
                print("Server:", json.loads(text))
        except asyncio.TimeoutError:
            pass

asyncio.run(stt_client("test.wav"))
```

### Docker Deployment

The `Dockerfile` automatically downloads both the native JNI libraries and the
model during the image build. The image uses `eclipse-temurin:21-jre-jammy`
(Ubuntu / glibc) instead of Alpine, because sherpa-onnx native libraries are
built against glibc.

```bash
./gradlew buildFatJar
docker build -t ai-speaking:latest .
docker compose up -d
```

Server constraints (4 GB RAM, 2-4 vCPU):

- JVM heap is capped at `-Xmx2g` to leave ~2 GB for native libs + onnxruntime.
- Default `STT_MAX_STREAMS=2` and `STT_NUM_THREADS=1` keep memory and CPU
  within budget. Adjust based on available resources.

### Testing

STT-specific tests (`./gradlew test --tests "ir.speaking.feature.stt.*"`):

| Test class | What it verifies |
|------------|------------------|
| `StreamConcurrencyLimiterTest` (8 tests) | Semaphore respects max limit; concurrent acquire from multiple threads; release frees permits; no permit leak on abrupt disconnect; try/finally pattern |
| `SttMessageSerializationTest` (9 tests) | JSON wire format: `type` discriminator, exact `server_busy` shape, round-trip encode/decode of partial/final/error/ready messages |

These tests are **pure JVM** — they do not require the native sherpa-onnx
library to be loaded.
