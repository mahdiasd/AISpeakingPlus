package ir.speaking.feature.leitner.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.leitner.db.UserFlashcardTable
import ir.speaking.feature.leitner.db.UserLeitnerSettingsTable
import ir.speaking.feature.leitner.model.UserFlashcard
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class UserFlashcardRepository {

    suspend fun addCard(
        userId: UUID,
        lexiconId: UUID?,
        customWord: String? = null,
        customMeaning: String? = null,
        customExample: String? = null,
        customAudioUrl: String? = null,
        now: Instant = Clock.System.now()
    ): UserFlashcard = suspendTransaction {
        // If lexiconId is provided, check if user already has this card in deck
        if (lexiconId != null) {
            val existing = UserFlashcardTable
                .join(LexiconTable, JoinType.LEFT, UserFlashcardTable.lexiconId, LexiconTable.id)
                .selectAll()
                .where { (UserFlashcardTable.userId eq userId) and (UserFlashcardTable.lexiconId eq lexiconId) }
                .firstOrNull()

            if (existing != null) {
                return@suspendTransaction existing.toUserFlashcard()
            }
        }

        val newId = UserFlashcardTable.insertAndGetId { row ->
            row[UserFlashcardTable.userId] = userId
            row[UserFlashcardTable.lexiconId] = lexiconId
            row[UserFlashcardTable.customWord] = customWord
            row[UserFlashcardTable.customMeaning] = customMeaning
            row[UserFlashcardTable.customExample] = customExample
            row[UserFlashcardTable.customAudioUrl] = customAudioUrl
            row[UserFlashcardTable.boxNumber] = 1
            row[UserFlashcardTable.reviewCount] = 0
            row[UserFlashcardTable.lastReviewedAt] = null
            row[UserFlashcardTable.nextReviewAt] = now // Ready for immediate review
            row[UserFlashcardTable.createdAt] = now
        }

        getCardByIdInternal(newId.value, userId)!!
    }

    suspend fun getDueCards(
        userId: UUID,
        now: Instant = Clock.System.now(),
        limit: Int = 20
    ): List<UserFlashcard> = suspendTransaction {
        UserFlashcardTable
            .join(LexiconTable, JoinType.LEFT, UserFlashcardTable.lexiconId, LexiconTable.id)
            .selectAll()
            .where { (UserFlashcardTable.userId eq userId) and (UserFlashcardTable.nextReviewAt lessEq now) }
            .orderBy(UserFlashcardTable.nextReviewAt)
            .limit(limit)
            .map { it.toUserFlashcard() }
    }

    suspend fun getCardsByBox(userId: UUID, boxNumber: Int): List<UserFlashcard> = suspendTransaction {
        UserFlashcardTable
            .join(LexiconTable, JoinType.LEFT, UserFlashcardTable.lexiconId, LexiconTable.id)
            .selectAll()
            .where { (UserFlashcardTable.userId eq userId) and (UserFlashcardTable.boxNumber eq boxNumber) }
            .orderBy(UserFlashcardTable.createdAt)
            .map { it.toUserFlashcard() }
    }

    suspend fun getAllCards(userId: UUID): List<UserFlashcard> = suspendTransaction {
        UserFlashcardTable
            .join(LexiconTable, JoinType.LEFT, UserFlashcardTable.lexiconId, LexiconTable.id)
            .selectAll()
            .where { UserFlashcardTable.userId eq userId }
            .orderBy(UserFlashcardTable.createdAt)
            .map { it.toUserFlashcard() }
    }

    suspend fun getCardById(cardId: UUID, userId: UUID): UserFlashcard? = suspendTransaction {
        getCardByIdInternal(cardId, userId)
    }

    private fun getCardByIdInternal(cardId: UUID, userId: UUID): UserFlashcard? {
        return UserFlashcardTable
            .join(LexiconTable, JoinType.LEFT, UserFlashcardTable.lexiconId, LexiconTable.id)
            .selectAll()
            .where { (UserFlashcardTable.id eq cardId) and (UserFlashcardTable.userId eq userId) }
            .firstOrNull()
            ?.toUserFlashcard()
    }

    suspend fun updateReviewResult(
        cardId: UUID,
        userId: UUID,
        remembered: Boolean,
        now: Instant = Clock.System.now()
    ): UserFlashcard? = suspendTransaction {
        val current = UserFlashcardTable.selectAll()
            .where { (UserFlashcardTable.id eq cardId) and (UserFlashcardTable.userId eq userId) }
            .firstOrNull() ?: return@suspendTransaction null

        val currentBox = current[UserFlashcardTable.boxNumber]
        val currentReviewCount = current[UserFlashcardTable.reviewCount]

        val newBox = if (remembered) {
            (currentBox + 1).coerceAtMost(5)
        } else {
            (currentBox - 1).coerceAtLeast(1)
        }

        val daysToAdd = when (newBox) {
            1 -> 1
            2 -> 3
            3 -> 7
            4 -> 14
            else -> 30
        }

        val nextReview = now.plus(daysToAdd, DateTimeUnit.DAY, TimeZone.UTC)

        UserFlashcardTable.update({ (UserFlashcardTable.id eq cardId) and (UserFlashcardTable.userId eq userId) }) { row ->
            row[boxNumber] = newBox
            row[reviewCount] = currentReviewCount + 1
            row[lastReviewedAt] = now
            row[nextReviewAt] = nextReview
        }

        getCardByIdInternal(cardId, userId)
    }

    suspend fun deleteCard(cardId: UUID, userId: UUID): Boolean = suspendTransaction {
        UserFlashcardTable.deleteWhere {
            (UserFlashcardTable.id eq cardId) and (UserFlashcardTable.userId eq userId)
        } > 0
    }

    suspend fun countDueCards(userId: UUID, now: Instant = Clock.System.now()): Long = suspendTransaction {
        UserFlashcardTable.selectAll()
            .where { (UserFlashcardTable.userId eq userId) and (UserFlashcardTable.nextReviewAt lessEq now) }
            .count()
    }

    suspend fun getBoxCounts(userId: UUID): Map<Int, Long> = suspendTransaction {
        val counts = mutableMapOf<Int, Long>()
        for (box in 1..5) {
            val count = UserFlashcardTable.selectAll()
                .where { (UserFlashcardTable.userId eq userId) and (UserFlashcardTable.boxNumber eq box) }
                .count()
            counts[box] = count
        }
        counts
    }

    suspend fun setReminderTime(
        userId: UUID,
        reminderTime: String,
        enabled: Boolean = true,
        now: Instant = Clock.System.now()
    ): Pair<String, Boolean> = suspendTransaction {
        val updated = UserLeitnerSettingsTable.update({ UserLeitnerSettingsTable.userId eq userId }) {
            it[UserLeitnerSettingsTable.reminderTime] = reminderTime
            it[UserLeitnerSettingsTable.reminderEnabled] = enabled
            it[UserLeitnerSettingsTable.updatedAt] = now
        }
        if (updated == 0) {
            UserLeitnerSettingsTable.insert {
                it[UserLeitnerSettingsTable.userId] = userId
                it[UserLeitnerSettingsTable.reminderTime] = reminderTime
                it[UserLeitnerSettingsTable.reminderEnabled] = enabled
                it[UserLeitnerSettingsTable.updatedAt] = now
            }
        }
        Pair(reminderTime, enabled)
    }

    suspend fun getReminderTime(userId: UUID): Pair<String, Boolean> = suspendTransaction {
        UserLeitnerSettingsTable.selectAll()
            .where { UserLeitnerSettingsTable.userId eq userId }
            .firstOrNull()
            ?.let {
                Pair(it[UserLeitnerSettingsTable.reminderTime], it[UserLeitnerSettingsTable.reminderEnabled])
            } ?: Pair("20:00", true)
    }

    private fun ResultRow.toUserFlashcard(): UserFlashcard {
        val customWord = this.getOrNull(UserFlashcardTable.customWord)
        val customMeaning = this.getOrNull(UserFlashcardTable.customMeaning)
        val customExample = this.getOrNull(UserFlashcardTable.customExample)
        val customAudio = this.getOrNull(UserFlashcardTable.customAudioUrl)

        val lexiconHeadword = this.getOrNull(LexiconTable.headword)
        val lexiconPhonetic = this.getOrNull(LexiconTable.phonetic)
        val lexiconPos = this.getOrNull(LexiconTable.partOfSpeech)
        val lexiconMeaning = this.getOrNull(LexiconTable.contextMeaningFa)
        val lexiconExEn = this.getOrNull(LexiconTable.sampleSentenceEn)
        val lexiconExFa = this.getOrNull(LexiconTable.sampleSentenceFa)
        val lexiconAudio = this.getOrNull(LexiconTable.audioUrl)

        val effectiveHeadword = customWord ?: lexiconHeadword ?: ""
        val effectiveMeaning = customMeaning ?: lexiconMeaning ?: ""
        val effectiveExampleEn = customExample ?: lexiconExEn
        val effectiveAudio = customAudio ?: lexiconAudio

        return UserFlashcard(
            id = this[UserFlashcardTable.id].value.toString(),
            userId = this[UserFlashcardTable.userId].value.toString(),
            lexiconId = this[UserFlashcardTable.lexiconId]?.value?.toString(),
            customWord = customWord,
            customMeaning = customMeaning,
            customExample = customExample,
            customAudioUrl = customAudio,
            boxNumber = this[UserFlashcardTable.boxNumber],
            reviewCount = this[UserFlashcardTable.reviewCount],
            lastReviewedAt = this[UserFlashcardTable.lastReviewedAt]?.toString(),
            nextReviewAt = this[UserFlashcardTable.nextReviewAt].toString(),
            createdAt = this[UserFlashcardTable.createdAt].toString(),
            headword = effectiveHeadword,
            phonetic = lexiconPhonetic,
            partOfSpeech = lexiconPos,
            meaningFa = effectiveMeaning,
            exampleEn = effectiveExampleEn,
            exampleFa = lexiconExFa,
            audioUrl = effectiveAudio
        )
    }
}
