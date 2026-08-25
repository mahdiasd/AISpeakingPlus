package ir.speaking.feature.home.repository

import ir.speaking.feature.home.dto.HomeResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient
import java.time.Duration

@Single
class HomeRedisRepositoryImpl(private val redissonClient: RedissonClient) : HomeRedisRepository {

    companion object {
        // A dedicated key for the home page cache.
        private const val HOME_CACHE_KEY = "v1:home:response"
        // Set a Time-To-Live (TTL) for the cache. Here, it's 1 hour.
        private val CACHE_DURATION: Duration = Duration.ofDays(7)
    }

    override suspend fun saveHomeResponse(response: List<HomeResponse>) {
        // It's best practice to serialize the complex object to a JSON string before saving.
        // I'm assuming you are using kotlinx.serialization based on your code.
        val jsonString = Json.encodeToString(response)
        val bucket = redissonClient.getBucket<String>(HOME_CACHE_KEY)
        bucket.set(jsonString, CACHE_DURATION)
    }

    override suspend fun readHomeResponse(): String? {
        val bucket = redissonClient.getBucket<String>(HOME_CACHE_KEY)
        return bucket.get()
    }

    override suspend fun clearCache() {
        val bucket = redissonClient.getBucket<String>(HOME_CACHE_KEY)
        bucket.delete() // Simply delete the key.
    }
}