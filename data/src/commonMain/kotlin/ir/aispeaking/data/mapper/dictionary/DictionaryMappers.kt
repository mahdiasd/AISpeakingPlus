package ir.aispeaking.data.mapper.dictionary

import ir.aispeaking.data.model.dictionary.DictionaryEntryDto
import ir.aispeaking.domain.model.dictionary.DictionaryEntry
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

internal fun DictionaryEntryDto.toDomain(): DictionaryEntry {
    val parsedMeanings = if (otherMeanings.isNotEmpty()) {
        otherMeanings
    } else if (!otherMeaningsJson.isNullOrBlank()) {
        try {
            json.decodeFromString<List<String>>(otherMeaningsJson)
        } catch (_: Throwable) {
            emptyList()
        }
    } else {
        emptyList()
    }

    return DictionaryEntry(
        id = id,
        normalizedText = normalizedText,
        headword = headword,
        phonetic = phonetic,
        partOfSpeech = partOfSpeech,
        contextMeaningFa = contextMeaningFa,
        definitionEn = definitionEn,
        otherMeanings = parsedMeanings,
        sampleSentenceEn = sampleSentenceEn,
        sampleSentenceFa = sampleSentenceFa,
        audioUrl = audioUrl,
        createdAt = createdAt
    )
}
