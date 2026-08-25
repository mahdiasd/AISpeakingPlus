FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the fat jar produced by `./gradlew buildFatJar`
COPY build/libs/*-all.jar app.jar

# Default directories
RUN mkdir -p /app/native /app/models /app/audio_cache /app/logs

# Speech-to-Text settings
ENV STT_MODEL_DIR=/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21
ENV STT_MAX_STREAMS=2
ENV STT_NUM_THREADS=1
ENV STT_SAMPLE_RATE=16000

# Text-to-Speech (Kokoro-82M) settings
ENV TTS_MODEL_DIR=/app/models/kokoro-en-v0_19
ENV TTS_NUM_THREADS=2
ENV TTS_DEFAULT_SID=0
ENV TTS_DEFAULT_SPEED=1.0
ENV TTS_CACHE_DIR=/app/audio_cache
ENV TTS_ENABLED=true

EXPOSE 8080

# JVM heap: cap at 2 GB to leave room for native libs + onnxruntime arena
ENTRYPOINT ["java", "-Xmx2g", "-Dsherpa_onnx.native.path=/app/native", "-jar", "app.jar"]
