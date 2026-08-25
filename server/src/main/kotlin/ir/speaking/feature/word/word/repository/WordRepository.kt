package ir.speaking.feature.word.word.repository

import ir.speaking.feature.word.word.dto.CrudWordRequest
import ir.speaking.feature.word.word.model.DailyWord
import java.util.*

interface WordRepository {
    suspend fun create(request: CrudWordRequest): DailyWord
    suspend fun update(request: CrudWordRequest): DailyWord?

    suspend fun getLastActive(): DailyWord?
    suspend fun getLast(): DailyWord?

    suspend fun delete(id: UUID): Boolean
    suspend fun count(): Long
}