package ir.speaking.admin.category.dto

import ir.speaking.feature.category.model.Category
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class UpdateCategoryRequest(
    val id: String,
    val name: String,
    val imageUrl: String?
)

fun UpdateCategoryRequest.toCategory() = Category(
    id = UUID.fromString(id),
    name = name,
    imageUrl = imageUrl,
    createdAt = LocalDateTime.now().toKotlinLocalDateTime()
)