package ir.speaking.feature.tts.service

import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavUtils {
    /**
     * Converts float audio samples (-1.0f to 1.0f) into a 16-bit PCM WAV byte array.
     */
    fun samplesToWavBytes(samples: FloatArray, sampleRate: Int): ByteArray {
        val numChannels = 1
        val bitsPerSample = 16
        val byteRate = sampleRate * numChannels * bitsPerSample / 8
        val blockAlign = numChannels * bitsPerSample / 8
        val pcmDataSize = samples.size * 2
        val totalDataLen = pcmDataSize + 36

        val buffer = ByteBuffer.allocate(44 + pcmDataSize).order(ByteOrder.LITTLE_ENDIAN)

        // RIFF header
        buffer.put('R'.code.toByte())
        buffer.put('I'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.put('F'.code.toByte())
        buffer.putInt(totalDataLen)
        buffer.put('W'.code.toByte())
        buffer.put('A'.code.toByte())
        buffer.put('V'.code.toByte())
        buffer.put('E'.code.toByte())

        // "fmt " sub-chunk
        buffer.put('f'.code.toByte())
        buffer.put('m'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put(' '.code.toByte())
        buffer.putInt(16) // SubChunk1Size (16 for PCM)
        buffer.putShort(1.toShort()) // AudioFormat (1 = PCM)
        buffer.putShort(numChannels.toShort()) // NumChannels
        buffer.putInt(sampleRate)
        buffer.putInt(byteRate)
        buffer.putShort(blockAlign.toShort())
        buffer.putShort(bitsPerSample.toShort())

        // "data" sub-chunk
        buffer.put('d'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.put('t'.code.toByte())
        buffer.put('a'.code.toByte())
        buffer.putInt(pcmDataSize)

        // Convert PCM samples
        for (sample in samples) {
            val clamped = sample.coerceIn(-1.0f, 1.0f)
            val shortVal = (clamped * 32767.0f).toInt().toShort()
            buffer.putShort(shortVal)
        }

        return buffer.array()
    }
}
