package ir.speaking.feature.discount.repository

import ir.speaking.feature.discount.model.DiscountCode
import java.util.*

interface DiscountCodeRepository {
    suspend fun createDiscountCode(request: DiscountCode): DiscountCode
    suspend fun getAllDiscountCodes(): List<DiscountCode>
    suspend fun getDiscountCodeById(id: UUID): DiscountCode?
    suspend fun updateDiscountCode(request: DiscountCode): DiscountCode?
    suspend fun deleteDiscountCode(id: UUID): Boolean
    suspend fun getActiveDiscountCodes(): List<DiscountCode>
    suspend fun getDiscountCodeByCode(code: String): DiscountCode?
}