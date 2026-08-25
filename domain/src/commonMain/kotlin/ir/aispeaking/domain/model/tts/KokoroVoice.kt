package ir.aispeaking.domain.model.tts

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class KokoroVoice(
    val id: Int,
    val code: String,
    val name: String,
    val gender: String,
    val accent: String,
    val language: String = "en-US",
    val description: String = ""
)

val DEFAULT_KOKORO_VOICES: ImmutableList<KokoroVoice> = persistentListOf(
    KokoroVoice(
        id = 0,
        code = "af",
        name = "Heart (Default)",
        gender = "FEMALE",
        accent = "AMERICAN",
        description = "Clear, natural American female voice (recommended)"
    ),
    KokoroVoice(
        id = 1,
        code = "af_bella",
        name = "Bella",
        gender = "FEMALE",
        accent = "AMERICAN",
        description = "Warm and friendly American female voice"
    ),
    KokoroVoice(
        id = 2,
        code = "af_nicole",
        name = "Nicole",
        gender = "FEMALE",
        accent = "AMERICAN",
        description = "Energetic and clear American female voice"
    ),
    KokoroVoice(
        id = 3,
        code = "af_sarah",
        name = "Sarah",
        gender = "FEMALE",
        accent = "AMERICAN",
        description = "Deliberate and calm American female voice"
    ),
    KokoroVoice(
        id = 4,
        code = "af_sky",
        name = "Sky",
        gender = "FEMALE",
        accent = "AMERICAN",
        description = "Soft and gentle American female voice"
    ),
    KokoroVoice(
        id = 5,
        code = "am_adam",
        name = "Adam",
        gender = "MALE",
        accent = "AMERICAN",
        description = "Confident and clear American male voice"
    ),
    KokoroVoice(
        id = 6,
        code = "am_michael",
        name = "Michael",
        gender = "MALE",
        accent = "AMERICAN",
        description = "Warm and deep American male voice"
    ),
    KokoroVoice(
        id = 7,
        code = "bf_emma",
        name = "Emma",
        gender = "FEMALE",
        accent = "BRITISH",
        description = "Eloquent and formal British female voice"
    ),
    KokoroVoice(
        id = 8,
        code = "bf_isabella",
        name = "Isabella",
        gender = "FEMALE",
        accent = "BRITISH",
        description = "Pleasant and gentle British female voice"
    ),
    KokoroVoice(
        id = 9,
        code = "bm_george",
        name = "George",
        gender = "MALE",
        accent = "BRITISH",
        description = "Classic and mature British male voice"
    ),
    KokoroVoice(
        id = 10,
        code = "bm_lewis",
        name = "Lewis",
        gender = "MALE",
        accent = "BRITISH",
        description = "Modern and expressive British male voice"
    ),
)
