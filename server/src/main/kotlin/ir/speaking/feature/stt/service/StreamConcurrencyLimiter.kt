package ir.speaking.feature.stt.service

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger

/**
 * Enforces a hard cap on the number of concurrent resources (e.g.
 * [OnlineStream]s) via a fair [Semaphore].
 *
 * Extracted from [SttService] so the concurrency-limit behavior can be
 * unit-tested without requiring the native sherpa-onnx library to be
 * loaded.
 *
 * Usage:
 *  - [tryAcquire] returns `true` when a permit is available (and increments
 *    the active counter), `false` when the server is at capacity.
 *  - [release] decrements the counter and returns the permit.
 *
 * Thread-safe.
 */
class StreamConcurrencyLimiter(maxConcurrent: Int) {
    private val semaphore = Semaphore(maxConcurrent, true)
    private val activeCount = AtomicInteger(0)
    private val maxStreams: Int = maxConcurrent.coerceAtLeast(1)

    /** The maximum number of concurrent permits. */
    val maxConcurrent: Int get() = maxStreams

    /** The number of currently held permits. */
    val active: Int get() = activeCount.get()

    /**
     * Tries to acquire a permit. Returns `true` if a permit was acquired,
     * `false` if the limit has been reached.
     */
    fun tryAcquire(): Boolean {
        if (!semaphore.tryAcquire()) return false
        activeCount.incrementAndGet()
        return true
    }

    /**
     * Releases a previously acquired permit.
     */
    fun release() {
        activeCount.decrementAndGet()
        semaphore.release()
    }
}
