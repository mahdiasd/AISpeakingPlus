package ir.aispeaking.domain.usecase.plan

import ir.aispeaking.domain.repository.plan.PlanRepository
import org.koin.core.annotation.Single

@Single
class GetPlansUseCase(private val repo: PlanRepository) {
    suspend operator fun invoke() = repo.getAllPlans()
}