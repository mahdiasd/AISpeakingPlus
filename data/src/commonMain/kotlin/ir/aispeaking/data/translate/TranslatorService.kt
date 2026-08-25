package ir.aispeaking.data.translate

import ir.aispeaking.domain.model.translate.Translation

interface TranslatorService {
    suspend fun translate(text: String): Result<Translation>
}