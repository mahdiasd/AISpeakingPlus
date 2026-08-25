package ir.speaking.core.network.model

import kotlinx.serialization.Serializable


@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>,
    val usageMetadata: UsageMetadata? = null,
    val modelVersion: String? = null
)

@Serializable
data class Candidate(
    val content: Content,
    val finishReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class Content(
    val parts: List<Part>,
    val role: String
)

@Serializable
data class Part(
    val text: String
)

@Serializable
data class SafetyRating(
    val category: String,
    val probability: String
)

@Serializable
data class UsageMetadata(
    val promptTokenCount: Int,
    val totalTokenCount: Int,
    val candidatesTokenCount: Int? = null,
    val promptTokensDetails: List<TokenDetails>? = null,
    val candidatesTokensDetails: List<TokenDetails>? = null
)

@Serializable
data class TokenDetails(
    val modality: String,
    val tokenCount: Int
)

