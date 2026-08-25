package ir.aispeaking.data.repository.translator

import ir.aispeaking.domain.model.translate.AlternativeTranslation
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.utils.dLog
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray


object TranslateParser {
    fun parseTranslateData(jsonArray: JsonArray): Translation? {
        val mainTranslation = getMainTranslate(jsonArray)
        val alternatives = getAlternatives(jsonArray)
        val example = getExample(jsonArray)

        return if (mainTranslation != null) {
            Translation(
                sourceText = "",
                translatedText = mainTranslation,
                alternatives = alternatives,
                example = example ?: ""
            )
        } else null
    }

    private fun getMainTranslate(jsonArray: JsonArray): String? {
        val element = jsonArray
            .firstOrNull()?.jsonArray
            ?.firstOrNull()?.jsonArray
            ?.firstOrNull()
        return if (element is JsonPrimitive && element.isString) element.content else null
    }

    private fun getAlternatives(jsonArray: JsonArray): ImmutableList<AlternativeTranslation> {
        TODO("implement translator")
    }

    private fun JsonElement?.getString(): String? {
        return if (this is JsonPrimitive && this.isString) this.content else null
    }

    private fun getExample(jsonArray: JsonArray): String? {
        try {
            val examplesSection = jsonArray.getOrNull(12)?.jsonArray ?: return null

            for (partOfSpeech in examplesSection) {
                if (partOfSpeech !is JsonArray) continue

                // Get the definitions list for this part of speech
                val definitions = partOfSpeech.getOrNull(1)?.jsonArray ?: continue

                // Look through each definition
                for (definition in definitions) {
                    if (definition !is JsonArray) continue

                    // The example is in position 2
                    val exampleText = definition.getOrNull(2).getString()
                    if (!exampleText.isNullOrEmpty()) {
                        return exampleText
                    }
                }
            }
        } catch (e: Exception) {
            e.message.dLog("getExample")
        }
        return null
    }
}
