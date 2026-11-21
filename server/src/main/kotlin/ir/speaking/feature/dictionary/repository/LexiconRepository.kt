package ir.speaking.feature.dictionary.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.dictionary.model.LexiconEntry
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class LexiconRepository {

    suspend fun findByNormalizedText(normalizedText: String): LexiconEntry? = suspendTransaction {
        val trimmed = normalizedText.trim().lowercase()
        LexiconTable.selectAll()
            .where { LexiconTable.normalizedText eq trimmed }
            .firstOrNull()
            ?.toLexiconEntry()
    }

    suspend fun findById(id: UUID): LexiconEntry? = suspendTransaction {
        LexiconTable.selectAll()
            .where { LexiconTable.id eq id }
            .firstOrNull()
            ?.toLexiconEntry()
    }

    suspend fun save(
        normalizedText: String,
        headword: String,
        phonetic: String?,
        partOfSpeech: String?,
        contextMeaningFa: String,
        otherMeaningsJson: String?,
        definitionEn: String?,
        sampleSentenceEn: String?,
        sampleSentenceFa: String?,
        audioUrl: String?
    ): LexiconEntry = suspendTransaction {
        val trimmed = normalizedText.trim().lowercase()
        val existing = LexiconTable.selectAll()
            .where { LexiconTable.normalizedText eq trimmed }
            .firstOrNull()

        if (existing != null) {
            existing.toLexiconEntry()
        } else {
            val newId = LexiconTable.insertAndGetId { row ->
                row[LexiconTable.normalizedText] = trimmed
                row[LexiconTable.headword] = headword
                row[LexiconTable.phonetic] = phonetic
                row[LexiconTable.partOfSpeech] = partOfSpeech
                row[LexiconTable.contextMeaningFa] = contextMeaningFa
                row[LexiconTable.otherMeanings] = otherMeaningsJson
                row[LexiconTable.definitionEn] = definitionEn
                row[LexiconTable.sampleSentenceEn] = sampleSentenceEn
                row[LexiconTable.sampleSentenceFa] = sampleSentenceFa
                row[LexiconTable.audioUrl] = audioUrl
            }

            LexiconEntry(
                id = newId.value.toString(),
                normalizedText = trimmed,
                headword = headword,
                phonetic = phonetic,
                partOfSpeech = partOfSpeech,
                contextMeaningFa = contextMeaningFa,
                otherMeaningsJson = otherMeaningsJson,
                definitionEn = definitionEn,
                sampleSentenceEn = sampleSentenceEn,
                sampleSentenceFa = sampleSentenceFa,
                audioUrl = audioUrl,
                createdAt = null
            )
        }
    }

    suspend fun updateAudioUrl(id: UUID, audioUrl: String): Boolean = suspendTransaction {
        LexiconTable.update({ LexiconTable.id eq id }) { row ->
            row[LexiconTable.audioUrl] = audioUrl
        } > 0
    }

    suspend fun deleteById(id: UUID): Boolean = suspendTransaction {
        LexiconTable.deleteWhere { LexiconTable.id eq id } > 0
    }

    suspend fun deleteByNormalizedText(normalizedText: String): Boolean = suspendTransaction {
        val trimmed = normalizedText.trim().lowercase()
        LexiconTable.deleteWhere { LexiconTable.normalizedText eq trimmed } > 0
    }

    private fun ResultRow.toLexiconEntry(): LexiconEntry {
        return LexiconEntry(
            id = this[LexiconTable.id].value.toString(),
            normalizedText = this[LexiconTable.normalizedText],
            headword = this[LexiconTable.headword],
            phonetic = this[LexiconTable.phonetic],
            partOfSpeech = this[LexiconTable.partOfSpeech],
            contextMeaningFa = this[LexiconTable.contextMeaningFa],
            otherMeaningsJson = this[LexiconTable.otherMeanings],
            definitionEn = this[LexiconTable.definitionEn],
            sampleSentenceEn = this[LexiconTable.sampleSentenceEn],
            sampleSentenceFa = this[LexiconTable.sampleSentenceFa],
            audioUrl = this[LexiconTable.audioUrl],
            createdAt = this[LexiconTable.createdAt].toString()
        )
    }
}
