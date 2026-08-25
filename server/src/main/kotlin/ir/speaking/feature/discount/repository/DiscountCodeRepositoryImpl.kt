package ir.speaking.feature.discount.repository

import ir.speaking.core.exeptions.AppException
import ir.speaking.core.utils.now
import ir.speaking.feature.discount.db.DiscountCodeDAO
import ir.speaking.feature.discount.db.DiscountCodeTable
import ir.speaking.feature.discount.db.toModel
import ir.speaking.feature.discount.model.DiscountCode
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.time.LocalDateTime
import java.util.*

@Single
class DiscountCodeRepositoryImpl : DiscountCodeRepository {
    override suspend fun createDiscountCode(request: DiscountCode): DiscountCode = newSuspendedTransaction {
        if (DiscountCodeDAO.find { DiscountCodeTable.code eq request.code }.empty().not())
            throw AppException.BadRequest("Code already exists.")

        if (request.percentage !in 1..100)
            throw AppException.BadRequest("Percentage should be between 1 and 100.")

        if (request.expiryDate.toJavaLocalDateTime().isBefore(LocalDateTime.now()))
            throw AppException.BadRequest("Expiry date must be in the future.")

        DiscountCodeDAO.new {
            code = request.code
            percentage = request.percentage
            isActive = request.isActive
            expiryDate = request.expiryDate
            createdAt = LocalDateTime.now().toKotlinLocalDateTime()
            applicablePlans = request.applicablePlans.joinToString(",") // Store as comma-separated string
        }.toModel()
    }

    override suspend fun getAllDiscountCodes(): List<DiscountCode> = newSuspendedTransaction {
        DiscountCodeDAO.all().map { it.toModel() }
    }

    override suspend fun getDiscountCodeById(id: UUID): DiscountCode? = newSuspendedTransaction {
        DiscountCodeDAO.findById(id)?.toModel()
    }

    override suspend fun updateDiscountCode(request: DiscountCode): DiscountCode? = newSuspendedTransaction {
        val discount = DiscountCodeDAO.findById(request.id ?: return@newSuspendedTransaction null)
            ?: return@newSuspendedTransaction null

        if (request.percentage !in 1..100)
            throw AppException.BadRequest("Percentage should be between 1 and 100.")

        if (request.expiryDate.toJavaLocalDateTime().isBefore(LocalDateTime.now()))
            throw AppException.BadRequest("Expiry date must be in the future.")

        discount.code = request.code
        discount.percentage = request.percentage
        discount.isActive = request.isActive
        discount.expiryDate = request.expiryDate
        discount.applicablePlans = request.applicablePlans.joinToString(",")
        discount.toModel()
    }

    override suspend fun deleteDiscountCode(id: UUID): Boolean = newSuspendedTransaction {
        DiscountCodeDAO.findById(id)?.delete() != null
    }

    override suspend fun getActiveDiscountCodes(): List<DiscountCode> = newSuspendedTransaction {
        val now = LocalDateTime.now()
        DiscountCodeDAO.find {
            (DiscountCodeTable.isActive eq true) and (DiscountCodeTable.expiryDate greater now.toKotlinLocalDateTime())
        }.map { it.toModel() }
    }

    override suspend fun getDiscountCodeByCode(code: String): DiscountCode? = newSuspendedTransaction {
        val now = kotlinx.datetime.LocalDateTime.now()

        DiscountCodeDAO.find {
            (DiscountCodeTable.code eq code) and
                    (DiscountCodeTable.isActive eq true) and
                    (DiscountCodeTable.expiryDate greaterEq now) // اطمینان از عدم انقضا
        }.firstOrNull()?.toModel()
    }
}