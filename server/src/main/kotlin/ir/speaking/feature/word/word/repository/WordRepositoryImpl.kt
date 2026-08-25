package ir.speaking.feature.word.word.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.word.word.DailyWordDAO
import ir.speaking.feature.word.word.DailyWordTable
import ir.speaking.feature.word.word.dto.CrudWordRequest
import ir.speaking.feature.word.word.model.DailyWord
import ir.speaking.feature.word.word.toModel
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.kotlin.datetime.date
import org.koin.core.annotation.Single
import java.time.LocalDateTime
import java.util.*

@Single
class WordRepositoryImpl : WordRepository {

    override suspend fun create(request: CrudWordRequest): DailyWord =
        suspendTransaction {
            DailyWordDAO.new {
                word = request.word
                options = request.options
                answerIndex = request.answerIndex
                points = request.points
                createdAt = LocalDateTime.now().toKotlinLocalDateTime()
                expiredAt =
                    LocalDateTime.parse(request.expiredAt).toKotlinLocalDateTime() // Parse the String to LocalDateTime
            }.toModel()
        }

    override suspend fun update(request: CrudWordRequest): DailyWord? =
        suspendTransaction {
            DailyWordDAO.findByIdAndUpdate(id = UUID.fromString(request.uid!!), block = {
                it.word = request.word
                it.options = request.options
                it.answerIndex = request.answerIndex
                it.createdAt = it.createdAt
                it.points = it.points
                it.expiredAt =
                    LocalDateTime.parse(request.expiredAt).toKotlinLocalDateTime() // Parse the String to LocalDateTime
            })?.toModel()
        }

    override suspend fun getLastActive(): DailyWord? =
        suspendTransaction {
            DailyWordDAO.find {
                (DailyWordTable.showDate.date() eq LocalDateTime.now().toKotlinLocalDateTime().date)
            }.lastOrNull()?.toModel()
        }

    override suspend fun getLast(): DailyWord? =
        suspendTransaction {
            DailyWordDAO.all().lastOrNull()?.toModel()
        }

    override suspend fun delete(id: UUID): Boolean = suspendTransaction {
        DailyWordDAO.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }

    override suspend fun count(): Long = suspendTransaction {
        DailyWordDAO.count()
    }

}