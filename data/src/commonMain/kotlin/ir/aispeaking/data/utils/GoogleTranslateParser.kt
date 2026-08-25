package ir.aispeaking.data.utils

/**
 * Utility class for parsing Google Translate API responses
 */
object GoogleTranslateParser {

    /**
     * Parse the raw JSON response from Google Translate API
     * @param json The raw JSON string from the API
     * @return TranslationResult containing structured translation data
     */
//    fun parseTranslationResponse(json: String): Result<TranslationResult> = runCatching {
//        val jsonElement = Json.parseToJsonElement(json)
//        val jsonArray = jsonElement.jsonArray
//
//        // Extract main translation data
//        val sourceWord = extractSourceWord(jsonArray)
//        val mainTranslation = extractMainTranslation(jsonArray)
//        val sourceLanguage = extractSourceLanguage(jsonArray)
//        val targetLanguage = jsonArray[2].jsonPrimitive.contentOrNull ?: "en"
//        val pronunciation = extractPronunciation(jsonArray)
//
//        // Extract alternative translations
//        val alternativeTranslations = extractAlternativeTranslations(jsonArray)
//
//        // Extract definition and examples if available
//        val definition = extractDefinition(jsonArray)
//        val examples = extractExamples(jsonArray)
//
//        TranslationResult(
//            sourceWord = sourceWord,
//            mainTranslation = mainTranslation,
//            sourceLanguage = sourceLanguage,
//            targetLanguage = targetLanguage,
//            pronunciation = pronunciation,
//            alternativeTranslations = alternativeTranslations,
//            definition = definition,
//            examples = examples
//        )
//    }

//    private fun extractSourceWord(jsonArray: JsonArray?): String {
//        return try {
//            jsonArray?.firstOrNull()?.jsonArray?.firstOrNull()?.jsonArray?.firstOrNull()?.jsonPrimitive?.content ?: ""
//        } catch (e: Exception) {
//            ""
//        }
//    }
//
//    private fun extractMainTranslation(jsonArray: JsonArray?): String {
//        return try {
//            jsonArray?.firstOrNull()?.jsonArray?.get(0)?.jsonArray?.get(1)?.jsonPrimitive?.content ?: ""
//        } catch (e: Exception) {
//            ""
//        }
//    }
//
//    private fun extractPronunciation(jsonArray: JsonArray?): String? {
//        return try {
//            jsonArray?.firstOrNull()?.jsonArray?.get(1)?.jsonArray?.get(3)?.jsonPrimitive?.contentOrNull
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun extractSourceLanguage(jsonArray: JsonArray): String {
//        return try {
//            val detectedLanguages = jsonArray.getOrNull(8)?.jsonArray
//            detectedLanguages?.getOrNull(0)?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content ?: ""
//        } catch (e: Exception) {
//            ""
//        }
//    }
//
//    private fun extractAlternativeTranslations(jsonArray: JsonArray): List<AlternativeTranslation> {
//        return try {
//            val alternativesArray = jsonArray.getOrNull(1)?.jsonArray?.getOrNull(0)?.jsonArray?.getOrNull(2)?.jsonArray
//                ?: return emptyList()
//
//            alternativesArray.mapNotNull { alternativeData ->
//                try {
//                    val alternativeWordData = alternativeData.jsonArray
//                    val word = alternativeWordData.getOrNull(0)?.jsonPrimitive?.content ?: return@mapNotNull null
//                    val translations = alternativeWordData.getOrNull(1)?.jsonArray?.mapNotNull {
//                        it.jsonPrimitive.contentOrNull
//                    } ?: emptyList()
//                    val confidence = alternativeWordData.getOrNull(3)?.jsonPrimitive?.doubleOrNull
//
//                    // Type is not directly in the structure, but we can infer it from array at index 1
//                    // In this case, types would come from the first element (index 0) which is "اسم" (Noun)
//                    val types = getTypesForWord(jsonArray)
//
//                    AlternativeTranslation(word, types, translations, confidence)
//                } catch (e: Exception) {
//                    null
//                }
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
//
//    private fun getTypesForWord(jsonArray: JsonArray): List<String>? {
//        return try {
//            // Try to find type information from different parts of the JSON
//            // First check if we have alternative translations section with type info
//            val alternativesSection = jsonArray.getOrNull(1)?.jsonArray?.getOrNull(0)
//            val typeFromAlternatives = alternativesSection?.jsonArray?.getOrNull(0)?.jsonPrimitive?.contentOrNull
//
//            // If found, return as a list
//            if (typeFromAlternatives != null) {
//                return listOf(typeFromAlternatives)
//            }
//
//            // Otherwise check in the dictionary part
//            val dictSection = jsonArray.getOrNull(12)
//            if (dictSection != null) {
//                val entries = dictSection.jsonArray
//                entries.forEach { entry ->
//                    val entryArray = entry.jsonArray
//                    val type = entryArray.getOrNull(0)?.jsonPrimitive?.contentOrNull
//                    if (type != null) {
//                        return listOf(type)
//                    }
//                }
//            }
//
//            null
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun extractDefinition(jsonArray: JsonArray): String? {
//        return try {
//            val definitions = jsonArray.getOrNull(12)?.jsonArray
//            definitions?.getOrNull(0)?.jsonArray?.getOrNull(1)?.jsonArray?.getOrNull(0)?.jsonArray?.getOrNull(0)?.jsonPrimitive?.contentOrNull
//        } catch (e: Exception) {
//            null
//        }
//    }
//
//    private fun extractExamples(jsonArray: JsonArray): List<String> {
//        return try {
//            val examplesSection = jsonArray.getOrNull(13)?.jsonArray?.getOrNull(0)
//            if (examplesSection != null) {
//                val definitionsArray = examplesSection.jsonArray.getOrNull(1)?.jsonArray
//                definitionsArray?.mapNotNull { definitionEntry ->
//                    // Example is at index 2 in each definition entry: [definition, source_id, example]
//                    definitionEntry.jsonArray.getOrNull(2)?.jsonPrimitive?.contentOrNull
//                } ?: emptyList()
//            } else {
//                emptyList()
//            }
//        } catch (e: Exception) {
//            emptyList()
//        }
//    }
}