package ir.speaking.feature.category.model

import java.util.*

data class Category(
    val id: UUID,
    val name: String,
    val imageUrl: String?,
    val createdAt: kotlinx.datetime.LocalDateTime
)