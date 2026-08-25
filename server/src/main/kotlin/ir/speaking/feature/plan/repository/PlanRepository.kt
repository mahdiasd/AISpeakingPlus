package ir.speaking.feature.plan.repository

import ir.speaking.feature.plan.model.Plan
import java.util.*

interface PlanRepository {
    suspend fun createPlan(plan: Plan): Plan
    suspend fun getAllPlans(): List<Plan>
    suspend fun updatePlan(plan: Plan): Plan?
    suspend fun deletePlan(id: UUID): Boolean
    suspend fun getPlan(id: UUID): Plan?
    fun getPlanNonSuspend(id: UUID): Plan?

    suspend fun updateMultiplePlans(plans: List<Plan>): List<Plan>
}