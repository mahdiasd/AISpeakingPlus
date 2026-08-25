package ir.aispeaking.data.mapper.category

import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.network.dto.category.CategoryResponse
import ir.aispeaking.utils.time.toInstant

fun CategoryResponse.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        imageUrl = imageUrl,
        createdAt = createdAt.toInstant()
    )
}