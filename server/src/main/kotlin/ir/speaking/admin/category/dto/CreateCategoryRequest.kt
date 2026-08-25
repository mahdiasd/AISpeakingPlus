package ir.speaking.admin.category.dto

import ir.speaking.feature.category.model.Category
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.*

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val imageUrl: String?
)

fun CreateCategoryRequest.toCategory() = Category(
    id = UUID.randomUUID(),
    name = name,
    imageUrl = imageUrl,
    createdAt = LocalDateTime.now().toKotlinLocalDateTime()
)