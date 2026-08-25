package ir.speaking.feature.category.dto.response

import ir.speaking.feature.category.model.Category
import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val createdAt: String
)

fun Category.toResponse(baseUrl: String) = CategoryResponse(
    id = id.toString(),
    name = name,
    imageUrl = if (imageUrl != null) "$baseUrl/resources/$imageUrl" else null,
    createdAt = createdAt.toString()
)