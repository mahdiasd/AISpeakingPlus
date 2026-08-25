package ir.speaking.feature.lightener.model

import kotlinx.serialization.Serializable
import java.util.*

data class Translation(
    val uid: UUID,
    val userId: UUID,

    val sourceText: String,
    val translatedText: String,
    val alternatives: List<AlternativeTranslation>?,
    val example: String?
)

@Serializable
data class AlternativeTranslation(
    val type: String,
    val translations: List<String>
)