# Chat Module — Voice (Kokoro-82M TTS + Realtime STT)

This module implements the conversational chat feature of the `AiSpeakingMultiplatform` app.

## Voice Architecture

1. **Text-to-Speech (TTS)**:
   - Powered by the backend **Kokoro-82M** ONNX speech synthesis engine.
   - High-quality 24kHz mono WAV audio served directly from backend endpoints:
     - Pre-generated via `/api/v1/chat` (`audioUrl`).
     - On-demand streaming via `/api/v1/tts/speak?text=...&voiceId=...&speed=...`.
     - Direct synthesis via `/api/v1/tts/synthesize`.
   - Audio playback is handled cross-platform by `AudioPlayer`:
     - **Android**: `android.media.MediaPlayer`
     - **iOS**: `AVFoundation` (`AVPlayer`)
     - **JVM (Desktop)**: `javax.sound.sampled.AudioSystem` & `Clip`
     - **JS**: HTML5 `Audio`
     - **WasmJS**: `@JsFun` bindings to browser `Audio`
   - Voice selection supported: 11 Kokoro voices (Heart, Bella, Nicole, Sarah, Sky, Adam, Michael, Emma, Isabella, George, Lewis) with pitch, speed, and accents (American & British).

2. **Speech-to-Text (STT)**:
   - Server-side realtime STT streaming over WebSocket (`/api/v1/stt`) using `SttController`.
   - Native and browser audio recording with mono PCM16 audio streaming.

---

## Kokoro Voices

| ID | Name | Accent | Gender |
|---|---|---|---|
| 0 | Heart | American | Female (Default) |
| 1 | Bella | American | Female |
| 2 | Nicole | American | Female |
| 3 | Sarah | American | Female |
| 4 | Sky | American | Female |
| 5 | Adam | American | Male |
| 6 | Michael | American | Male |
| 7 | Emma | British | Female |
| 8 | Isabella | British | Female |
| 9 | George | British | Male |
| 10 | Lewis | British | Male |
