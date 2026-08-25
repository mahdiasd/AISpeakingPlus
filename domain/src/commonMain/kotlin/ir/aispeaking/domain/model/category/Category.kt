package ir.aispeaking.domain.model.category

import kotlin.time.Instant


data class Category(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val createdAt: Instant
)