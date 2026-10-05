FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install tools needed for native libs and model management
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    tar \
    bzip2 \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copy the fat jar produced by `./gradlew buildFatJar`
COPY build/libs/*-all.jar app.jar

# Default directories
RUN mkdir -p /app/native /app/models /app/audio_cache /app/logs

# Pre-download Kokoro-82M model during build for instant container availability
RUN curl -SL "https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-en-v0_19.tar.bz2" -o /tmp/kokoro.tar.bz2 && \
    tar -xjf /tmp/kokoro.tar.bz2 -C /app/models && \
    rm -f /tmp/kokoro.tar.bz2

# Speech-to-Text settings
ENV STT_MODEL_DIR=/app/models/sherpa-onnx-streaming-zipformer-en-2023-06-21
ENV STT_MAX_STREAMS=2
ENV STT_NUM_THREADS=1
ENV STT_SAMPLE_RATE=16000

# Text-to-Speech (Kokoro-82M) settings
ENV TTS_MODEL_DIR=/app/models/kokoro-en-v0_19
ENV TTS_AUTO_DOWNLOAD=true
ENV TTS_NUM_THREADS=2
ENV TTS_DEFAULT_SID=0
ENV TTS_DEFAULT_SPEED=1.0
ENV TTS_CACHE_DIR=/app/audio_cache
ENV TTS_ENABLED=true

EXPOSE 8080

# JVM heap: cap at 2 GB to leave room for native libs + onnxruntime arena
ENTRYPOINT ["java", "-Xmx2g", "-Dsherpa_onnx.native.path=/app/native", "-jar", "app.jar"]
