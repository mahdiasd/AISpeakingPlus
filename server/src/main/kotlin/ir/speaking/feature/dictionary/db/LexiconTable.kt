package ir.speaking.feature.dictionary.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object LexiconTable : UUIDTable("lexicon_entries") {
    val normalizedText = varchar("normalized_text", 255).uniqueIndex()
    val headword = varchar("headword", 128)
    val phonetic = varchar("phonetic", 128).nullable()
    val partOfSpeech = varchar("part_of_speech", 64).nullable()
    val contextMeaningFa = text("context_meaning_fa")
    val otherMeanings = text("other_meanings_json").nullable()
    val definitionEn = text("definition_en").nullable()
    val sampleSentenceEn = text("sample_sentence_en").nullable()
    val sampleSentenceFa = text("sample_sentence_fa").nullable()
    val audioUrl = varchar("audio_url", 512).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
