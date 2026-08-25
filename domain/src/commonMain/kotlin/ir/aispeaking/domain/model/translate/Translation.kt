package ir.aispeaking.domain.model.translate

import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class Translation(
    val uid: String = Uuid.generateV7().toString(),
    val sourceText: String,
    val translatedText: String,
    val alternatives: ImmutableList<AlternativeTranslation>,
    val example: String,

    /** Using in ui and in:
     * @see ir.speaking.lightener.component.LightenerItem
     * */
    val expanded: Boolean = false,

    /** Using in ui and in:
     * @see ir.speaking.lightener.component.LightenerItem
     * */
    val showTranslate: Boolean = false
)

@Serializable
data class AlternativeTranslation(
    val type: String,
    val translations: ImmutableList<String>
)