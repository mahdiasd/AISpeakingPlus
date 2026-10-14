package ir.speaking.feature.admin.promo.service

import ir.speaking.feature.admin.promo.model.*
import ir.speaking.feature.subscription.db.PromoCodeRedemptionTable
import ir.speaking.feature.subscription.db.PromoCodeTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID
import kotlin.math.min

class AdminPromoService {

    private val planPrices = mapOf(
        "plan-1m" to 199_000L,
        "plan-3m" to 499_000L,
        "plan-6m" to 899_000L,
        "1_MONTH" to 199_000L,
        "3_MONTHS" to 499_000L,
        "6_MONTHS" to 899_000L
    )

    fun normalizePlanId(id: String): String = when (id.uppercase().trim()) {
        "1_MONTH", "PLAN-1M" -> "plan-1m"
        "3_MONTHS", "PLAN-3M" -> "plan-3m"
        "6_MONTHS", "PLAN-6M" -> "plan-6m"
        else -> id.lowercase().trim()
    }

    fun getPlanPrice(planId: String): Long? = planPrices[planId] ?: planPrices[normalizePlanId(planId)]

    private fun parseInstantOrNull(str: String?): Instant? {
        if (str.isNullOrBlank()) return null
        return try {
            Instant.parse(str)
        } catch (_: Exception) {
            try {
                Instant.parse("${str}Z")
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun ResultRow.toPromoCodeItemDto(now: Instant): AdminPromoCodeItemDto {
        val exp = this[PromoCodeTable.expiresAt]
        val plansRaw = this[PromoCodeTable.applicablePlans]
        val plansList = if (plansRaw == "ALL" || plansRaw.isBlank()) {
            emptyList()
        } else {
            plansRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }

        return AdminPromoCodeItemDto(
            id = this[PromoCodeTable.id].value.toString(),
            code = this[PromoCodeTable.code],
            title = this[PromoCodeTable.title],
            discountType = this[PromoCodeTable.discountType],
            discountValue = this[PromoCodeTable.discountValue],
            maxDiscountCap = this[PromoCodeTable.maxDiscountCap],
            minOrderAmount = this[PromoCodeTable.minOrderAmount],
            applicablePlans = plansList,
            maxUses = this[PromoCodeTable.maxUses],
            usedCount = this[PromoCodeTable.usedCount],
            isOncePerUser = this[PromoCodeTable.isOncePerUser],
            startsAt = this[PromoCodeTable.startsAt]?.toString(),
            expiresAt = this[PromoCodeTable.expiresAt]?.toString(),
            isActive = this[PromoCodeTable.isActive],
            isExpired = exp != null && exp <= now,
            createdAt = this[PromoCodeTable.createdAt].toString()
        )
    }

    suspend fun getPromoCodes(
        search: String? = null,
        status: String? = null
    ): AdminPromoListResponse = newSuspendedTransaction(Dispatchers.IO) {
        val now = Clock.System.now()

        var condition: Op<Boolean> = PromoCodeTable.isDeleted eq false

        if (!search.isNullOrBlank()) {
            val q = "%${search.trim().lowercase()}%"
            val searchCondition = (PromoCodeTable.code.lowerCase() like q) or (PromoCodeTable.title.lowerCase() like q)
            condition = condition and searchCondition
        }

        if (!status.isNullOrBlank() && status != "ALL") {
            when (status.uppercase()) {
                "ACTIVE" -> {
                    val activeCond = (PromoCodeTable.isActive eq true) and
                            (PromoCodeTable.expiresAt.isNull() or (PromoCodeTable.expiresAt greater now)) and
                            (PromoCodeTable.startsAt.isNull() or (PromoCodeTable.startsAt lessEq now))
                    condition = condition and activeCond
                }
                "EXPIRED" -> {
                    val expiredCond = PromoCodeTable.expiresAt.isNotNull() and (PromoCodeTable.expiresAt lessEq now)
                    condition = condition and expiredCond
                }
                "INACTIVE" -> {
                    val inactiveCond = PromoCodeTable.isActive eq false
                    condition = condition and inactiveCond
                }
            }
        }

        val rows = PromoCodeTable.selectAll()
            .where { condition }
            .orderBy(PromoCodeTable.createdAt, SortOrder.DESC)
            .toList()

        val items = rows.map { it.toPromoCodeItemDto(now) }

        // Stats calculation
        val allNonDeleted = PromoCodeTable.selectAll().where { PromoCodeTable.isDeleted eq false }.toList()
        val totalCodes = allNonDeleted.size
        val activeCodes = allNonDeleted.count { row ->
            val exp = row[PromoCodeTable.expiresAt]
            val starts = row[PromoCodeTable.startsAt]
            row[PromoCodeTable.isActive] && (exp == null || exp > now) && (starts == null || starts <= now)
        }
        val totalRedemptions = PromoCodeRedemptionTable.selectAll().count().toInt()
        val totalDiscountGiven = PromoCodeRedemptionTable.selectAll()
            .sumOf { it[PromoCodeRedemptionTable.discountAmount] }

        AdminPromoListResponse(
            items = items,
            stats = AdminPromoStatsDto(
                totalCodes = totalCodes,
                activeCodes = activeCodes,
                totalRedemptions = totalRedemptions,
                totalDiscountGiven = totalDiscountGiven
            )
        )
    }

    suspend fun getPromoCodeById(id: UUID): AdminPromoCodeItemDto? = newSuspendedTransaction(Dispatchers.IO) {
        val now = Clock.System.now()
        PromoCodeTable.selectAll()
            .where { (PromoCodeTable.id eq id) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull()
            ?.toPromoCodeItemDto(now)
    }

    suspend fun createPromoCode(
        adminId: UUID?,
        req: AdminPromoCodeUpsertRequest
    ): AdminPromoCodeItemDto = newSuspendedTransaction(Dispatchers.IO) {
        val normalizedCode = req.code.trim().uppercase()
        require(normalizedCode.isNotEmpty()) { "کد تخفیف نمی‌تواند خالی باشد" }
        require(req.discountValue > 0) { "مقدار تخفیف باید بزرگ‌تر از صفر باشد" }
        if (req.discountType.uppercase() == "PERCENTAGE") {
            require(req.discountValue in 1..100) { "درصد تخفیف باید بین ۱ تا ۱۰۰ باشد" }
        }
        req.maxDiscountCap?.let { require(it > 0) { "سقف تخفیف باید بزرگ‌تر از صفر باشد" } }
        req.minOrderAmount?.let { require(it >= 0) { "حداقل مبلغ سفارش نمی‌تواند منفی باشد" } }
        req.maxUses?.let { require(it > 0) { "حداکثر دفعات استفاده باید بزرگ‌تر از صفر باشد" } }

        val existing = PromoCodeTable.selectAll()
            .where { (PromoCodeTable.code eq normalizedCode) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull()

        if (existing != null) {
            throw IllegalArgumentException("کد تخفیف «$normalizedCode» قبلاً ثبت شده است")
        }

        val startsInstant = parseInstantOrNull(req.startsAt)
        val expiresInstant = parseInstantOrNull(req.expiresAt)
        if (startsInstant != null && expiresInstant != null) {
            require(expiresInstant > startsInstant) { "تاریخ و زمان انقضا باید پس از تاریخ شروع باشد" }
        }
        val plansStr = if (req.applicablePlans.isEmpty()) "ALL" else req.applicablePlans.joinToString(",")
        val now = Clock.System.now()

        val newId = PromoCodeTable.insertAndGetId {
            it[code] = normalizedCode
            it[title] = req.title?.trim()
            it[discountType] = req.discountType.uppercase()
            it[discountValue] = req.discountValue
            it[maxDiscountCap] = req.maxDiscountCap
            it[minOrderAmount] = req.minOrderAmount
            it[applicablePlans] = plansStr
            it[maxUses] = req.maxUses
            it[usedCount] = 0
            it[isOncePerUser] = req.isOncePerUser
            it[startsAt] = startsInstant
            it[expiresAt] = expiresInstant
            it[isActive] = req.isActive
            it[isDeleted] = false
            it[createdBy] = adminId
            it[createdAt] = now
            it[updatedAt] = now
        }

        PromoCodeTable.selectAll()
            .where { PromoCodeTable.id eq newId }
            .first()
            .toPromoCodeItemDto(now)
    }

    suspend fun updatePromoCode(
        id: UUID,
        req: AdminPromoCodeUpsertRequest
    ): AdminPromoCodeItemDto = newSuspendedTransaction(Dispatchers.IO) {
        val existing = PromoCodeTable.selectAll()
            .where { (PromoCodeTable.id eq id) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull() ?: throw NoSuchElementException("کد تخفیف یافت نشد")

        val normalizedCode = req.code.trim().uppercase()
        require(normalizedCode.isNotEmpty()) { "کد تخفیف نمی‌تواند خالی باشد" }
        require(req.discountValue > 0) { "مقدار تخفیف باید بزرگ‌تر از صفر باشد" }
        if (req.discountType.uppercase() == "PERCENTAGE") {
            require(req.discountValue in 1..100) { "درصد تخفیف باید بین ۱ تا ۱۰۰ باشد" }
        }
        req.maxDiscountCap?.let { require(it > 0) { "سقف تخفیف باید بزرگ‌تر از صفر باشد" } }
        req.minOrderAmount?.let { require(it >= 0) { "حداقل مبلغ سفارش نمی‌تواند منفی باشد" } }
        req.maxUses?.let { require(it > 0) { "حداکثر دفعات استفاده باید بزرگ‌تر از صفر باشد" } }

        // Check if code was modified and conflicts with another existing code
        val conflict = PromoCodeTable.selectAll()
            .where { (PromoCodeTable.code eq normalizedCode) and (PromoCodeTable.id neq id) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull()

        if (conflict != null) {
            throw IllegalArgumentException("کد تخفیف «$normalizedCode» برای رکورد دیگری ثبت شده است")
        }

        val startsInstant = parseInstantOrNull(req.startsAt)
        val expiresInstant = parseInstantOrNull(req.expiresAt)
        if (startsInstant != null && expiresInstant != null) {
            require(expiresInstant > startsInstant) { "تاریخ و زمان انقضا باید پس از تاریخ شروع باشد" }
        }
        val plansStr = if (req.applicablePlans.isEmpty()) "ALL" else req.applicablePlans.joinToString(",")
        val now = Clock.System.now()

        PromoCodeTable.update({ PromoCodeTable.id eq id }) {
            it[code] = normalizedCode
            it[title] = req.title?.trim()
            it[discountType] = req.discountType.uppercase()
            it[discountValue] = req.discountValue
            it[maxDiscountCap] = req.maxDiscountCap
            it[minOrderAmount] = req.minOrderAmount
            it[applicablePlans] = plansStr
            it[maxUses] = req.maxUses
            it[isOncePerUser] = req.isOncePerUser
            it[startsAt] = startsInstant
            it[expiresAt] = expiresInstant
            it[isActive] = req.isActive
            it[updatedAt] = now
        }

        PromoCodeTable.selectAll()
            .where { PromoCodeTable.id eq id }
            .first()
            .toPromoCodeItemDto(now)
    }

    suspend fun toggleStatus(id: UUID, isActive: Boolean): AdminPromoCodeItemDto = newSuspendedTransaction(Dispatchers.IO) {
        val exists = PromoCodeTable.selectAll()
            .where { (PromoCodeTable.id eq id) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull() ?: throw NoSuchElementException("کد تخفیف یافت نشد")

        val now = Clock.System.now()
        PromoCodeTable.update({ PromoCodeTable.id eq id }) {
            it[PromoCodeTable.isActive] = isActive
            it[updatedAt] = now
        }

        PromoCodeTable.selectAll()
            .where { PromoCodeTable.id eq id }
            .first()
            .toPromoCodeItemDto(now)
    }

    suspend fun deletePromoCode(id: UUID): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val updated = PromoCodeTable.update({ (PromoCodeTable.id eq id) and (PromoCodeTable.isDeleted eq false) }) {
            it[isDeleted] = true
            it[isActive] = false
            it[updatedAt] = Clock.System.now()
        }
        updated > 0
    }

    suspend fun getRedemptions(promoId: UUID): List<AdminPromoRedemptionItemDto> = newSuspendedTransaction(Dispatchers.IO) {
        val query = (PromoCodeRedemptionTable innerJoin UserTable)
            .selectAll()
            .where { PromoCodeRedemptionTable.promoCodeId eq promoId }
            .orderBy(PromoCodeRedemptionTable.redeemedAt, SortOrder.DESC)

        query.map { row ->
            AdminPromoRedemptionItemDto(
                id = row[PromoCodeRedemptionTable.id].value.toString(),
                userId = row[PromoCodeRedemptionTable.userId].value.toString(),
                phoneNumber = row[UserTable.mobile],
                nickName = row[UserTable.nickName],
                planId = row[PromoCodeRedemptionTable.planId],
                originalPrice = row[PromoCodeRedemptionTable.originalPrice],
                discountAmount = row[PromoCodeRedemptionTable.discountAmount],
                finalPrice = row[PromoCodeRedemptionTable.finalPrice],
                redeemedAt = row[PromoCodeRedemptionTable.redeemedAt].toString()
            )
        }
    }

    data class PromoValidationResult(
        val isValid: Boolean,
        val promoCodeId: UUID? = null,
        val normalizedCode: String,
        val discountPercent: Int = 0,
        val discountType: String = "PERCENTAGE",
        val discountAmountTomans: Long = 0,
        val originalPriceTomans: Long = 0,
        val finalPriceTomans: Long = 0,
        val message: String
    )

    suspend fun validateForUserAndPlan(
        code: String,
        userId: UUID? = null,
        planId: String? = null
    ): PromoValidationResult = newSuspendedTransaction(Dispatchers.IO) {
        val normalized = code.trim().uppercase()
        if (normalized.isEmpty()) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                normalizedCode = normalized,
                message = "کد تخفیف وارد نشده است"
            )
        }

        val row = PromoCodeTable.selectAll()
            .where { (PromoCodeTable.code eq normalized) and (PromoCodeTable.isDeleted eq false) }
            .firstOrNull()

        if (row == null) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                normalizedCode = normalized,
                message = "کد تخفیف واردشده وجود ندارد"
            )
        }

        val promoId = row[PromoCodeTable.id].value
        val now = Clock.System.now()

        // 1. Is Active check
        if (!row[PromoCodeTable.isActive]) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "این کد تخفیف در حال حاضر غیرفعال است"
            )
        }

        // 2. Date window check
        val startsAt = row[PromoCodeTable.startsAt]
        if (startsAt != null && startsAt > now) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "زمان استفاده از این کد تخفیف هنوز آغاز نشده است"
            )
        }

        val expiresAt = row[PromoCodeTable.expiresAt]
        if (expiresAt != null && expiresAt <= now) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "مهلت استفاده از این کد تخفیف به پایان رسیده است"
            )
        }

        // 3. Max uses check
        val maxUses = row[PromoCodeTable.maxUses]
        val usedCount = row[PromoCodeTable.usedCount]
        if (maxUses != null && usedCount >= maxUses) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "سقف تعداد استفاده از این کد تخفیف تکمیل شده است"
            )
        }

        // 4. Once per user check
        val isOncePerUser = row[PromoCodeTable.isOncePerUser]
        if (userId != null && isOncePerUser) {
            val alreadyUsed = PromoCodeRedemptionTable.selectAll()
                .where { (PromoCodeRedemptionTable.promoCodeId eq promoId) and (PromoCodeRedemptionTable.userId eq userId) }
                .count() > 0

            if (alreadyUsed) {
                return@newSuspendedTransaction PromoValidationResult(
                    isValid = false,
                    promoCodeId = promoId,
                    normalizedCode = normalized,
                    message = "شما قبلاً یک بار از این کد تخفیف استفاده کرده‌اید"
                )
            }
        }

        // 5. Applicable plans check
        val applicablePlansRaw = row[PromoCodeTable.applicablePlans]
        val applicableList = if (applicablePlansRaw == "ALL" || applicablePlansRaw.isBlank()) {
            emptyList()
        } else {
            applicablePlansRaw.split(",").map { normalizePlanId(it) }
        }

        val normalizedInputPlan = planId?.let { normalizePlanId(it) }
        if (normalizedInputPlan != null && applicableList.isNotEmpty() && normalizedInputPlan !in applicableList) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "این کد تخفیف برای پلن انتخابی شما قابل استفاده نیست"
            )
        }

        // 6. Pricing & calculation
        val originalPrice = normalizedInputPlan?.let { getPlanPrice(it) } ?: (planId?.let { getPlanPrice(it) } ?: 0L)
        val minOrder = row[PromoCodeTable.minOrderAmount]
        if (minOrder != null && originalPrice > 0 && originalPrice < minOrder) {
            return@newSuspendedTransaction PromoValidationResult(
                isValid = false,
                promoCodeId = promoId,
                normalizedCode = normalized,
                message = "حداقل مبلغ سفارش برای اعمال این کد $minOrder تومان است"
            )
        }

        val discountType = row[PromoCodeTable.discountType]
        val discountValue = row[PromoCodeTable.discountValue]
        val maxCap = row[PromoCodeTable.maxDiscountCap]

        val discountAmount: Long
        val discountPercent: Int

        if (discountType == "PERCENTAGE") {
            discountPercent = discountValue.toInt()
            val rawDiscount = if (originalPrice > 0) (originalPrice * discountPercent / 100) else 0L
            discountAmount = if (maxCap != null && rawDiscount > maxCap) maxCap else rawDiscount
        } else {
            // FIXED_AMOUNT
            discountAmount = if (originalPrice > 0) min(originalPrice, discountValue) else discountValue
            discountPercent = if (originalPrice > 0) {
                ((discountAmount.toDouble() / originalPrice.toDouble()) * 100).toInt()
            } else 0
        }

        val finalPrice = if (originalPrice > 0) (originalPrice - discountAmount).coerceAtLeast(0L) else 0L

        val successMsg = if (discountType == "PERCENTAGE") {
            "کد تخفیف $discountValue٪ با موفقیت اعمال شد"
        } else {
            "کد تخفیف مبلغ $discountValue تومان با موفقیت اعمال شد"
        }

        PromoValidationResult(
            isValid = true,
            promoCodeId = promoId,
            normalizedCode = normalized,
            discountPercent = discountPercent,
            discountType = discountType,
            discountAmountTomans = discountAmount,
            originalPriceTomans = originalPrice,
            finalPriceTomans = finalPrice,
            message = successMsg
        )
    }

    suspend fun recordRedemption(
        promoCodeId: UUID,
        userId: UUID,
        subscriptionId: UUID?,
        planId: String,
        originalPrice: Long,
        discountAmount: Long,
        finalPrice: Long
    ) = newSuspendedTransaction(Dispatchers.IO) {
        PromoCodeRedemptionTable.insert {
            it[PromoCodeRedemptionTable.promoCodeId] = promoCodeId
            it[PromoCodeRedemptionTable.userId] = userId
            it[PromoCodeRedemptionTable.subscriptionId] = subscriptionId
            it[PromoCodeRedemptionTable.planId] = planId
            it[PromoCodeRedemptionTable.originalPrice] = originalPrice
            it[PromoCodeRedemptionTable.discountAmount] = discountAmount
            it[PromoCodeRedemptionTable.finalPrice] = finalPrice
            it[redeemedAt] = Clock.System.now()
        }

        PromoCodeTable.update({ PromoCodeTable.id eq promoCodeId }) {
            with(SqlExpressionBuilder) {
                it.update(PromoCodeTable.usedCount, PromoCodeTable.usedCount + 1)
            }
            it[updatedAt] = Clock.System.now()
        }
    }
}
