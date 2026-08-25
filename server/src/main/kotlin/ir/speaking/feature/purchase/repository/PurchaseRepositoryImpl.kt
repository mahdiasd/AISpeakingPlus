package ir.speaking.feature.purchase.repository

import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.PagedList
import ir.speaking.core.response.PagingMeta
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.core.utils.toUUID
import ir.speaking.core.utils.toUUIDOrNull
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.plan.model.Plan
import ir.speaking.feature.plan.repository.PlanRepository
import ir.speaking.feature.purchase.db.PurchaseDAO
import ir.speaking.feature.purchase.db.PurchaseTable
import ir.speaking.feature.purchase.db.toModel
import ir.speaking.feature.purchase.dto.PurchaseRequest
import ir.speaking.feature.purchase.dto.PurchaseResponse
import ir.speaking.feature.purchase.dto.toResponse
import ir.speaking.feature.purchase.model.Purchase
import ir.speaking.feature.purchase.model.PurchaseStatus
import ir.speaking.feature.purchase.model.toPurchaseStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*
import kotlin.time.toKotlinDuration

@Single
class PurchaseRepositoryImpl(
    private val planRepository: PlanRepository,
) : PurchaseRepository {

    override suspend fun createPurchase(purchase: PurchaseRequest): PurchaseResponse {
        val activePurchases = getActivePurchasesByUserId(purchase.userId.toUUID("userId not exists!"))
        return suspendTransaction {
            val currentPlan = planRepository.getPlanNonSuspend(purchase.planId.toUUID("plan id is wrong!"))
                ?: throw AppException.NotFound("No plan found for ${purchase.planId}")

            val newPurchase = PurchaseDAO.new {
                userId = purchase.userId.toUUID("user id is wrong!")
                planId = purchase.planId.toUUID("plan id is wrong!")
                discountCode = purchase.discountCodeId?.toUUIDOrNull()
                purchaseDate = Clock.System.now()
                expiryDate = calculateExpiryDate(activePurchases, currentPlan.dayDuration.toLong())
                amountPaid = purchase.amountPaid
                status = purchase.status.toPurchaseStatus()
                token = purchase.token
                createdAt = Clock.System.now()
            }
            newPurchase.toModel().toResponse(currentPlan)
        }
    }

    override suspend fun giftCharge(userId: UUID, plan: Plan): PurchaseResponse {
        val activePurchases = getActivePurchasesByUserId(userId)
        return suspendTransaction {
            val newPurchase = PurchaseDAO.new {
                this.userId = userId
                this.planId = plan.id
                this.discountCode = null
                this.purchaseDate = Clock.System.now()
                this.expiryDate = calculateExpiryDate(activePurchases, plan.dayDuration.toLong())
                this.amountPaid = "0"
                this.status = PurchaseStatus.SUCCESS
                this.token = ""
                this.createdAt = Clock.System.now()
            }
            newPurchase.toModel().toResponse(plan)
        }
    }

    override suspend fun getPurchasesByUserId(
        userId: UUID,
        page: Int,
        pageSize: Int
    ): PagedList<PurchaseResponse> {
        return suspendTransaction {
            val query = PurchaseTable
                .join(
                    otherTable = PlanTable,
                    joinType = JoinType.INNER,
                    onColumn = PurchaseTable.planId,
                    otherColumn = PlanTable.id
                )
                .selectAll()
                .where { PurchaseTable.userId eq userId }

            val totalItems = query.count()
            PagedList(
                items = query
                    .limit(pageSize)
                    .offset(((page - 1) * pageSize).toLong())
                    .map {
                        // Now 'it' (the ResultRow) contains columns from both
                        // PurchaseTable and PlanTable, so mapping will succeed.
                        mapRowToPurchaseResponse(it)
                    },
                pagingMeta = PagingMeta(
                    totalPages = ((totalItems + pageSize - 1) / pageSize).toInt(),
                    totalItems = totalItems,
                    page = page
                )
            )
        }
    }

    override suspend fun deletePurchase(id: UUID): Boolean = suspendTransaction {
        PurchaseDAO.findById(id)?.delete() != null
    }

    override suspend fun getActivePurchasesByUserId(userId: UUID): List<PurchaseResponse> {
        return suspendTransaction {
            (PurchaseTable innerJoin PlanTable)
                .selectAll()
                .where {
                    (PurchaseTable.userId eq userId) and
                            (PurchaseTable.expiryDate greater Clock.System.now())
                }
                .map { row ->
                    mapRowToPurchaseResponse(row)
                }
        }
    }

    private fun mapRowToPurchaseResponse(row: org.jetbrains.exposed.sql.ResultRow): PurchaseResponse {
        val purchase = Purchase(
            id = row[PurchaseTable.id].value,
            userId = row[PurchaseTable.userId],
            planId = row[PurchaseTable.planId],
            discountCodeId = row[PurchaseTable.discountCodeId],
            purchaseDate = row[PurchaseTable.purchaseDate],
            expiryDate = row[PurchaseTable.expiryDate],
            amountPaid = row[PurchaseTable.amountPaid],
            token = row[PurchaseTable.token],
            status = row[PurchaseTable.status],
            createdAt = row[PurchaseTable.createdAt]
        )
        val plan = Plan(
            id = row[PlanTable.id].value,
            title = row[PlanTable.title],
            name = row[PlanTable.name],
            description = row[PlanTable.description],
            price = row[PlanTable.price],
            discountedPrice = row[PlanTable.discountedPrice],
            cafeBazaarId = row[PlanTable.cafeBazaarId],
            dayDuration = row[PlanTable.dayDuration],
            visibility = row[PlanTable.visibility],
            createdAt = row[PlanTable.createdAt]
        )
        return purchase.toResponse(plan)
    }

    private fun calculateExpiryDate(
        activePurchases: List<PurchaseResponse>,
        dayDuration: Long
    ): Instant {
        val lastExpireDate =
            activePurchases.lastOrNull()?.expiryDate?.let { Instant.parse(it) } ?: Clock.System.now()
        return lastExpireDate.plus(java.time.Duration.ofDays(dayDuration).toKotlinDuration())
    }

}
