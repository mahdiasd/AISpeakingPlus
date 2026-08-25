# Cross-Platform STT (Speech-to-Text) & TTS (Text-to-Speech) Libraries

A curated list of GitHub libraries that provide speech recognition (STT) and text-to-speech (TTS) capabilities for Android, iOS, and Web — prioritized by libraries that support **both** STT and TTS together, followed by single-capability libraries.

This list was researched on GitHub in July 2026.

---

## 🥇 Libraries Supporting Both STT & TTS (Combined)

| Library | Stars | Language | Platforms | Description |
|---------|-------|----------|-----------|-------------|
| **[k2-fsa/sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)** | ~13,500 ★ | C++ | Android, iOS, Web(WS), Desktop, Embedded | Full-featured: STT, TTS, speaker diarization, VAD, speech enhancement. Offline-first, ONNX runtime, no internet required. Supports 12+ programming languages. |
| **[ramanujammv1988/edge-veda](https://github.com/ramanujammv1988/edge-veda)** | ~86 ★ | Dart | Android, iOS, Web | On-device AI SDK for Flutter: LLM inference, vision, **STT**, **TTS**, image generation, embeddings, RAG. Metal GPU on iOS/macOS. |
| **[jbpassot/flutter_voice_friend](https://github.com/jbpassot/flutter_voice_friend)** | ~34 ★ | Dart | Android, iOS | Open-source Flutter app combining STT and TTS for voice-driven chatbot experiences. Good reference implementation. |
| **[cscoderr/gemini_talking_ai](https://github.com/cscoderr/gemini_talking_ai)** | ~11 ★ | Dart | Android, iOS | Flutter app using STT (voice input) → Gemini LLM → TTS (spoken response). Hands-free conversational experience template. |

---

## 📱 Kotlin Multiplatform (KMP) Libraries

Your project uses Kotlin Multiplatform — these are the most directly relevant.

### STT Libraries (KMP)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[ferranpons/Llamatik](https://github.com/ferranpons/Llamatik)** | ~164 ★ | Android, iOS, Desktop, JVM, WASM | On-device AI: LLM, **STT** (whisper.cpp), Image Generation. Powered by llama.cpp, whisper.cpp, stable-diffusion.cpp. |
| **[kmpile/whisper.cpp-kmp](https://github.com/kmpile/whisper.cpp-kmp)** | ~0 ★ (new) | Android, iOS, JVM/Desktop | whisper.cpp STT binding for KMP — streams `Flow<Segment>`, VAD support, whisper.cpp 1.8.x. Ideal low-level binding. |
| **[eslamwael74/speechtotextkit](https://github.com/eslamwael74/speechtotextkit)** | ~19 ★ | Android, iOS, Desktop | KMP unified API for speech-to-text across multiple platforms. |
| **[findusl/wav-recorder](https://github.com/findusl/wav-recorder)** | ~0 ★ (new) | Android, iOS, Desktop, Web | Minimalistic KMP WAV recording library for speech/AI models. Useful as audio capture layer. |

### TTS Libraries (KMP)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[Marc-JB/TextToSpeechKt](https://github.com/Marc-JB/TextToSpeechKt)** | ~56 ★ | Android, iOS, macOS, JS/Web, WASM | Kotlin Multiplatform Text-to-Speech library. Most complete KMP TTS solution — covers Web (JS+WASM) too. |
| **[mohaberabi/text-to-speech-cmp](https://github.com/mohaberabi/text-to-speech-cmp)** | ~0 ★ (new) | Android, iOS (Compose MP) | TTS in Compose Multiplatform. Simpler alternative. |

---

## 🎯 Flutter Libraries (Cross-Platform: Android + iOS + Web)

### STT (Speech-to-Text)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[csdcorp/speech_to_text](https://github.com/csdcorp/speech_to_text)** | ~470 ★ | Android, iOS, Web | Exposes device-specific speech recognition. Good Web support via browser Speech API. |
| **[CodeSagePath/whisper_kit](https://github.com/CodeSagePath/whisper_kit)** | ~4 ★ | Android, iOS (planned) | Flutter library using whisper.cpp for offline STT. iOS planned. |
| **[umair13adil/background_stt](https://github.com/umair13adil/background_stt)** | ~12 ★ | Android | Always-on background speech-to-text service in Flutter. |
| **[jjordanoc/azure_speech_recognition_null_safety](https://github.com/jjordanoc/azure_speech_recognition_null_safety)** | ~13 ★ | Android, iOS | Flutter plugin for Azure Cognitive Services Speech-to-Text API. |

### TTS (Text-to-Speech)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[dlutton/flutter_tts](https://github.com/dlutton/flutter_tts)** | ~748 ★ | Android, iOS, Web | Most popular Flutter TTS package. Wraps native platform TTS engines. |
| **[ghuyfel/flutter_azure_tts](https://github.com/ghuyfel/flutter_azure_tts)** | ~39 ★ | Android, iOS, Web | Flutter implementation of Microsoft Azure Cognitive TTS API. |
| **[ixsans/text_to_speech](https://github.com/ixsans/text_to_speech)** | ~19 ★ | Android, iOS | Flutter plugin providing TTS service. |
| **[alfianlosari/flutter_cloud_text_to_speech](https://github.com/alfianlosari/flutter_cloud_text_to_speech)** | ~21 ★ | Android, iOS, Web | Google Cloud Text-to-Speech via REST API in Flutter. |
| **[markokosticdev/cloud_text_to_speech_flutter](https://github.com/markokosticdev/cloud_text_to_speech_flutter)** | ~8 ★ | Android, iOS | Single interface to Google, Microsoft, and Amazon Text-to-Speech. |

---

## ⚛️ React Native Libraries

### STT

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[react-native-voice/voice](https://github.com/react-native-voice/voice)** | ~2,159 ★ | Android, iOS | Voice recognition (online + offline support). Native Android SpeechRecognizer + iOS SFSpeechRecognizer. |
| **[mybigday/whisper.rn](https://github.com/mybigday/whisper.rn)** | ~796 ★ | Android, iOS | whisper.cpp binding for React Native — on-device whisper transcription. |

### TTS

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[ak1394/react-native-tts](https://github.com/ak1394/react-native-tts)** | ~697 ★ | Android, iOS | React Native Text-to-Speech library. |

---

## 📦 Capacitor/Ionic Libraries (Web + Mobile)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[capacitor-community/speech-recognition](https://github.com/capacitor-community/speech-recognition)** | ~127 ★ | Android, iOS, Web | Capacitor plugin for speech recognition. |
| **[Cap-go/capacitor-speech-synthesis](https://github.com/Cap-go/capacitor-speech-synthesis)** | ~7 ★ | Android, iOS, Web | Capacitor plugin for speech synthesis (TTS) — control language, voice, pitch, rate, volume. |
| **[gaudravi09/capacitor-offline-speech-recognition](https://github.com/gaudravi09/capacitor-offline-speech-recognition)** | ~0 ★ (new) | Android, iOS | Offline STT plugin — Android true offline (multiple languages), iOS offline English + online fallback. |

---

## 🔬 Expo Libraries (React Native Expo — Android + iOS + Web)

| Library | Stars | Platforms | Description |
|---------|-------|-----------|-------------|
| **[jamsch/expo-speech-recognition](https://github.com/jamsch/expo-speech-recognition)** | ~652 ★ | Android, iOS, Web | Speech Recognition for Expo projects. Wraps native speech APIs. |

---

## 🧠 Native/Underlying Engines (most powerful, not plug-and-play)

These are the best-in-class engine-level libraries. Use them via wrappers above.

| Library | Stars | Capability | Description |
|---------|-------|------------|-------------|
| **[k2-fsa/sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)** | ~13,500 ★ | STT + TTS + more | **The best combined library.** Offline, ONNX, supports Android/iOS/Embedded/x86/Web. |
| **[ggml-org/whisper.cpp](https://github.com/ggml-org/whisper.cpp)** | ~51,700 ★ | STT only | OpenAI Whisper model in C/C++. Runs on Android (via NDK), iOS (via Metal), Web (via WASM). |
| **[alphacep/vosk-api](https://github.com/alphacep/vosk-api)** | ~14,900 ★ | STT only | Offline speech recognition API for Android, iOS, Raspberry Pi, servers. Python, Java, C#, Node. |
| **[coqui-ai/TTS](https://github.com/coqui-ai/TTS)** | ~45,700 ★ | TTS only | Deep learning TTS toolkit. Research-grade, production-tested. |
| **[rhasspy/piper](https://github.com/rhasspy/piper)** | ~11,200 ★ | TTS only | Fast neural local TTS system. Runs on Android (Termux), Linux, Windows, macOS. |
| **[hexgrad/kokoro](https://github.com/hexgrad/kokoro)** | ~7,900 ★ | TTS only | Kokoro-82M — very natural-sounding TTS model (via HuggingFace). API wrappers available for mobile. |
| **[remsky/Kokoro-FastAPI](https://github.com/remsky/Kokoro-FastAPI)** | ~5,200 ★ | TTS server | Dockerized FastAPI wrapper for Kokoro-82M — CPU, AMD, NVIDIA GPU support. Can serve mobile clients. |

---

## 🎯 Recommendations for Your Project (Kotlin Multiplatform)

Your project is Kotlin Multiplatform targeting Android, iOS, Desktop, and Web. Here's the most practical path:

### Option A: Sherpa-ONNX (STT + TTS in one)
- **[k2-fsa/sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx)** — 13,500 ★
- Supports Android (NDK), iOS, WASM (Web), desktop, embedded.
- Provides both STT and TTS offline, plus VAD and speaker diarization.
- Has C API — can be wrapped in Kotlin/Native or via cinterop.
- **Best single-library solution.**

### Option B: Separate Best-in-Class for Each
- **STT**: **[ggml-org/whisper.cpp](https://github.com/ggml-org/whisper.cpp)** (~51k ★) wrapped via **[kmpile/whisper.cpp-kmp](https://github.com/kmpile/whisper.cpp-kmp)** (KMP binding).
- **TTS**: **[Marc-JB/TextToSpeechKt](https://github.com/Marc-JB/TextToSpeechKt)** (~56 ★) for KMP TTS with Web support.

### Option C: Llamatik (AI + STT in one)
- **[ferranpons/Llamatik](https://github.com/ferranpons/Llamatik)** (~164 ★)
- Already wraps whisper.cpp for STT.
- Add TextToSpeechKt for TTS if needed.

---

## 📊 Summary Table

| Library | STT | TTS | Android | iOS | Web | Stars | Language |
|---------|:---:|:---:|:-------:|:---:|:---:|-------|----------|
| [sherpa-onnx](https://github.com/k2-fsa/sherpa-onnx) | ✅ | ✅ | ✅ | ✅ | ✅ | ~13,500 | C++ |
| [edge-veda](https://github.com/ramanujammv1988/edge-veda) | ✅ | ✅ | ✅ | ✅ | ✅ | ~86 | Dart |
| [Llamatik](https://github.com/ferranpons/Llamatik) | ✅ | ❌ | ✅ | ✅ | ✅ | ~164 | Kotlin |
| [TextToSpeechKt](https://github.com/Marc-JB/TextToSpeechKt) | ❌ | ✅ | ✅ | ✅ | ✅ | ~56 | Kotlin |
| [speechtotextkit (KMP)](https://github.com/eslamwael74/speechtotextkit) | ✅ | ❌ | ✅ | ✅ | ❌ | ~19 | Kotlin |
| [whisper.cpp-kmp](https://github.com/kmpile/whisper.cpp-kmp) | ✅ | ❌ | ✅ | ✅ | ❌ | ~0 | Kotlin |
| [whisper.rn](https://github.com/mybigday/whisper.rn) | ✅ | ❌ | ✅ | ✅ | ❌ | ~796 | C++ |
| [react-native-voice/voice](https://github.com/react-native-voice/voice) | ✅ | ❌ | ✅ | ✅ | ❌ | ~2,159 | TypeScript |
| [react-native-tts](https://github.com/ak1394/react-native-tts) | ❌ | ✅ | ✅ | ✅ | ❌ | ~697 | Java |
| [flutter_tts](https://github.com/dlutton/flutter_tts) | ❌ | ✅ | ✅ | ✅ | ✅ | ~748 | Dart |
| [csdcorp/speech_to_text](https://github.com/csdcorp/speech_to_text) | ✅ | ❌ | ✅ | ✅ | ✅ | ~470 | Dart |
| [capacitor-community/speech-recognition](https://github.com/capacitor-community/speech-recognition) | ✅ | ❌ | ✅ | ✅ | ✅ | ~127 | Java |
| [Cap-go/capacitor-speech-synthesis](https://github.com/Cap-go/capacitor-speech-synthesis) | ❌ | ✅ | ✅ | ✅ | ✅ | ~7 | JavaScript |
| [expo-speech-recognition](https://github.com/jamsch/expo-speech-recognition) | ✅ | ❌ | ✅ | ✅ | ✅ | ~652 | TypeScript |
| [whisper.cpp](https://github.com/ggml-org/whisper.cpp) | ✅ | ❌ | ✅ | ✅ | ✅ | ~51,700 | C/C++ |
| [vosk-api](https://github.com/alphacep/vosk-api) | ✅ | ❌ | ✅ | ✅ | ❌ | ~14,900 | Python/Java/C# |
| [coqui-ai/TTS](https://github.com/coqui-ai/TTS) | ❌ | ✅ | ❌ | ❌ | ❌ | ~45,700 | Python |
| [piper](https://github.com/rhasspy/piper) | ❌ | ✅ | ✅ | ❌ | ❌ | ~11,200 | C++ |
| [kokoro](https://github.com/hexgrad/kokoro) | ❌ | ✅ | ❌ | ❌ | ❌ | ~7,900 | Python |

---

*Last updated: July 2026 — researched via GitHub API*