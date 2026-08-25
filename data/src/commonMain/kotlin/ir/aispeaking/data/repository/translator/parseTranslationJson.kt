package ir.aispeaking.data.repository.translator

import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.utils.dLog
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray


fun parseTranslationJson(jsonArray: JsonArray): Translation? {
    try {
        // Extract main translation: [0][0][1]
        val mainTranslation = jsonArray
            .firstOrNull()?.jsonArray?.firstOrNull()?.jsonArray
            ?.firstOrNull()?.toString() ?: ""

//        val alternatives =

        return null
//        return TranslationData(mainTranslation, alternatives.toImmutableList(), example)
    } catch (e: Exception) {
        e.dLog("parseTranslationJson: ${e.message}")
        return null
    }
}