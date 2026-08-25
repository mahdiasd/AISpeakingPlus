package ir.speaking.feature.word.progress.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.word.progress.db.WordProgressDAO
import ir.speaking.feature.word.progress.db.WordProgressTable
import ir.speaking.feature.word.progress.model.WordProgress
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.time.LocalDateTime
import java.util.*

@Single
class WordProgressRepositoryImpl : WordProgressRepository {

    override suspend fun create(
        userId: UUID,
        wordId: UUID,
        selectedOptionIndex: Int,
        score: Int,
        isCorrect: Boolean
    ): WordProgress =
        suspendTransaction {
            WordProgressDAO.new {
                this.userId = userId
                this.wordId = wordId
                this.selectedOptionIndex = selectedOptionIndex
                this.isCorrect = isCorrect
                this.score = score
                this.answeredAt = LocalDateTime.now().toKotlinLocalDateTime()
            }.toModel()
        }

    override suspend fun findByUserId(userId: UUID, wordId: UUID): WordProgress? = suspendTransaction {
        WordProgressDAO
            .find { (WordProgressTable.userId eq userId) and (WordProgressTable.wordId eq wordId) }.firstOrNull()
            ?.toModel()
    }

    override suspend fun countByUser(userId: UUID) = newSuspendedTransaction {
        WordProgressTable.selectAll()
            .where { WordProgressTable.userId eq userId }
            .count()
    }

}