package ir.speaking.feature.stt.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicInteger

/**
 * Unit tests for [StreamConcurrencyLimiter] — the concurrency-limit
 * component used by [SttService] to cap the number of active WebSocket STT
 * sessions.
 *
 * These tests do NOT require the native sherpa-onnx library; they verify
 * pure-JVM semaphore/counter semantics.
 */
class StreamConcurrencyLimiterTest {

    @Test
    fun `acquire up to max then reject`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 2)

        assertTrue(limiter.tryAcquire(), "first acquire should succeed")
        assertTrue(limiter.tryAcquire(), "second acquire should succeed")
        assertEquals(2, limiter.active, "active should be 2 after two acquires")

        assertFalse(limiter.tryAcquire(), "third acquire must be rejected (capacity)")
        assertEquals(2, limiter.active, "active should still be 2 after rejected acquire")
    }

    @Test
    fun `release frees a permit`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 1)

        assertTrue(limiter.tryAcquire())
        assertFalse(limiter.tryAcquire(), "second should fail — at capacity")

        limiter.release()
        assertEquals(0, limiter.active, "active should drop to 0 after release")

        assertTrue(limiter.tryAcquire(), "acquire should succeed again after release")
        limiter.release()
    }

    @Test
    fun `maxConcurrent is reported correctly`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 3)
        assertEquals(3, limiter.maxConcurrent)
    }

    @Test
    fun `concurrent acquire from multiple threads respects the limit`() {
        val maxPermits = 4
        val limiter = StreamConcurrencyLimiter(maxConcurrent = maxPermits)
        val threadCount = 16
        val barrier = CyclicBarrier(threadCount)
        val acquiredCount = AtomicInteger(0)

        val threads = (1..threadCount).map {
            Thread {
                barrier.await()
                if (limiter.tryAcquire()) {
                    acquiredCount.incrementAndGet()
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(maxPermits, acquiredCount.get(),
            "exactly $maxPermits permits should have been granted")
        assertEquals(maxPermits, limiter.active, "active should equal max after concurrent acquires")
    }

    @Test
    fun `acquire-release-acquire cycle under concurrency`() {
        val maxPermits = 2
        val limiter = StreamConcurrencyLimiter(maxConcurrent = maxPermits)
        val successCount = AtomicInteger(0)
        val iterations = 100

        runBlocking {
            val jobs = (1..8).map {
                async(Dispatchers.IO) {
                    repeat(iterations) {
                        if (limiter.tryAcquire()) {
                            try {
                                successCount.incrementAndGet()
                            } finally {
                                limiter.release()
                            }
                        }
                    }
                }
            }
            jobs.awaitAll()
        }

        // All 8 * 100 = 800 attempts should succeed because every acquire
        // is immediately released (permits are recycled).
        assertEquals(8 * iterations, successCount.get(),
            "all acquire-release cycles should complete successfully")
        assertEquals(0, limiter.active, "all permits should be returned")
    }

    /**
     * Simulates the SttService pattern: acquire -> (use) -> release,
     * verifying that a rejected connection (null stream) scenario plays
     * out correctly when the limiter is full.
     */
    @Test
    fun `simulated session lifecycle - no permit leak on abrupt disconnect`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 2)

        // Simulate two active sessions.
        assertTrue(limiter.tryAcquire())
        assertTrue(limiter.tryAcquire())
        assertEquals(2, limiter.active)

        // A third session is rejected (server_busy).
        assertFalse(limiter.tryAcquire())
        assertEquals(2, limiter.active)

        // Session 1 disconnects abruptly -- must release the permit.
        limiter.release()
        assertEquals(1, limiter.active)

        // Now a new session can acquire the freed permit.
        assertTrue(limiter.tryAcquire())
        assertEquals(2, limiter.active)

        // All sessions disconnect.
        limiter.release() // session 2
        limiter.release() // session 3
        assertEquals(0, limiter.active)
        assertEquals(2, limiter.maxConcurrent, "maxConcurrent must be unchanged")
    }

    /**
     * Simulates a resource leak scenario: verify that if [release] is called
     * in a `finally` block (as SttService does), the active count never
     * goes negative and the semaphore is not over-released.
     */
    @Test
    fun `release in finally block prevents leak`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 1)

        // Simulate 10 sessions, each with a try/finally that guarantees release.
        repeat(10) {
            val acquired = limiter.tryAcquire()
            try {
                // "work"
            } finally {
                if (acquired) limiter.release()
            }
        }

        assertEquals(0, limiter.active, "no permits should leak after all sessions end")
    }

    /**
     * Multiple threads each repeatedly acquire and release. No permit should
     * leak and active should return to zero once all threads finish.
     */
    @Test
    fun `concurrent acquire-release does not leak permits`() {
        val limiter = StreamConcurrencyLimiter(maxConcurrent = 3)
        val threadCount = 10
        val opsPerThread = 200

        val threads = (1..threadCount).map {
            Thread {
                repeat(opsPerThread) {
                    if (limiter.tryAcquire()) {
                        try {
                            Thread.sleep(0, 100) // tiny amount of "work"
                        } finally {
                            limiter.release()
                        }
                    }
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        assertEquals(0, limiter.active, "active must be 0 after all threads finish")
        assertEquals(3, limiter.maxConcurrent)
    }
}
