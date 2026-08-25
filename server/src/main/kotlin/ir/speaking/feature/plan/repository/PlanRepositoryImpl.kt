package ir.speaking.feature.plan.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.plan.db.PlanDAO
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.plan.db.PlanTable.discountedPrice
import ir.speaking.feature.plan.db.PlanTable.price
import ir.speaking.feature.plan.db.toModel
import ir.speaking.feature.plan.model.Plan
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.*

@Single
class PlanRepositoryImpl : PlanRepository {

    override suspend fun createPlan(plan: Plan): Plan = suspendTransaction {
        PlanDAO.new {
            populateFields(this, plan)
        }.toModel()
    }

    override suspend fun getAllPlans(): List<Plan> = suspendTransaction {
        PlanDAO.all().map { it.toModel() }
    }

    override suspend fun updatePlan(plan: Plan): Plan? = suspendTransaction {
        PlanDAO.findByIdAndUpdate(
            id = plan.id,
            block = { populateFields(it, plan) }
        )?.toModel()
    }



    override suspend fun deletePlan(id: UUID): Boolean = suspendTransaction {
        PlanDAO.findById(id)?.delete() != null
    }

    override suspend fun getPlan(id: UUID): Plan? = suspendTransaction {
        PlanDAO.findById(id)?.toModel()
    }

    override fun getPlanNonSuspend(id: UUID): Plan? {
        return PlanDAO.findById(id)?.toModel()
    }

    override suspend fun updateMultiplePlans(plans: List<Plan>): List<Plan> = suspendTransaction {
        plans.mapNotNull { plan ->
            PlanDAO.findByIdAndUpdate(
                id = plan.id,
                block = { populateFields(it, plan) }
            )?.toModel()
        }
    }

    private fun populateFields(planDAO: PlanDAO, plan: Plan) {
        planDAO.title = plan.title
        planDAO.description = plan.description
        planDAO.price = plan.price
        planDAO.discountedPrice = plan.discountedPrice
        planDAO.dayDuration = plan.dayDuration
        planDAO.createdAt = plan.createdAt
    }
}