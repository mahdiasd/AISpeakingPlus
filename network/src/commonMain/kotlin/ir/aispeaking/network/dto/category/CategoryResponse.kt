package ir.aispeaking.network.dto.category

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val createdAt: String
)