package ir.aispeaking.domain.repository.dictionary

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.dictionary.DictionaryEntry

interface DictionaryRepository {
    suspend fun lookupWord(word: String, sentenceContext: String? = null): DataResult<DictionaryEntry>
}
