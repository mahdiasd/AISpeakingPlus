package ir.aispeaking.domain.usecase.scenario

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.domain.repository.scenario.ScenarioRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class GetScenariosUseCase(private val repo: ScenarioRepository) {
    suspend operator fun invoke(
        searchText: String?,
        categoryId: String?,
        page: Int,
        pageSize: Int? = null
    ): Flow<DataResult<Paging<ScenarioSummary>>> {
        return repo.getScenarios(
            searchText = searchText,
            categoryId = categoryId,
            page = page,
            pageSize = pageSize
        )
    }
}