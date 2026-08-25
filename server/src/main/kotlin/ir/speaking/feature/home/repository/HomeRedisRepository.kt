package ir.speaking.feature.home.repository

import ir.speaking.feature.home.dto.HomeResponse

/**
 * Interface for Caching Home page response in Redis.
 */
interface HomeRedisRepository {
    /**
     * Saves the entire home page response list as a single JSON string in Redis.
     * @param response The list of HomeResponse to be cached.
     */
    suspend fun saveHomeResponse(response: List<HomeResponse>)

    /**
     * Reads the cached home page response from Redis.
     * @return A JSON string of the cached response, or null if it doesn't exist.
     */
    suspend fun readHomeResponse(): String?

    /**
     * Deletes the home page cache key from Redis to force a refresh on the next request.
     */
    suspend fun clearCache()
}