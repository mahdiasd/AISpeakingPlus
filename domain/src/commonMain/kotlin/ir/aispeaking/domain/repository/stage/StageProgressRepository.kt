package ir.aispeaking.domain.repository.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.StageProgress

interface StageProgressRepository {
    suspend fun syncGuestProgress(): DataResult<List<StageProgress>>
    suspend fun saveLocalGuestProgress(stageId: String, stars: Int, score: Int): DataResult<Unit>
    suspend fun getLocalGuestProgress(): DataResult<Map<String, StageProgress>>
}
