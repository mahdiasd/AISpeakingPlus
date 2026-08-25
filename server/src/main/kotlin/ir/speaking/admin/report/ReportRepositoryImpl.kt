package ir.speaking.admin.report

import ir.speaking.core.utils.toHumanReadable
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.progress.db.ChallengeProgressTable
import ir.speaking.feature.discount.db.DiscountCodeTable
import ir.speaking.feature.plan.db.PlanTable
import ir.speaking.feature.purchase.db.PurchaseTable
import ir.speaking.feature.scenario.progress.db.ScenarioProgressTable
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.word.progress.db.WordProgressTable
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class ReportRepositoryImpl : ReportRepository {

    override suspend fun getUserReport(userId: UUID): UserReportDto? {
        // بدون تغییر
        return newSuspendedTransaction {
            UserTable.selectAll()
                .where { UserTable.id eq userId }
                .orderBy(UserTable.createdAt, SortOrder.DESC)
                .singleOrNull()
                ?.let { userRow ->
                    val scenarios = (ScenarioProgressTable innerJoin ScenarioTable)
                        .selectAll()
                        .where { ScenarioProgressTable.userId eq userId }
                        .map {
                            CompletedScenarioDto(
                                id = it[ScenarioTable.id].toString(),
                                scenarioName = it[ScenarioTable.title]
                            )
                        }

                    val challenges = (ChallengeProgressTable innerJoin ChallengeTable)
                        .selectAll()
                        .where { ChallengeProgressTable.userId eq userId }
                        .map {
                            CompletedChallengeDto(
                                id = it[ChallengeTable.id].toString(),
                                challengeName = it[ChallengeTable.title]
                            )
                        }

                    val wordsCount = WordProgressTable
                        .selectAll()
                        .where { WordProgressTable.userId eq userId }
                        .count()
                        .toInt()

                    UserReportDto(
                        id = userId.toString(),
                        nickname = userRow[UserTable.nickName],
                        firstname = userRow[UserTable.firstName],
                        lastname = userRow[UserTable.lastName],
                        mobile = userRow[UserTable.mobile],
                        score = userRow[UserTable.score],
                        languageLevel = userRow[UserTable.languageLevel],
                        scenarios = scenarios,
                        challenges = challenges,
                        wordsCount = wordsCount,
                        createdAt = userRow[UserTable.createdAt].toJavaInstant().toHumanReadable(),
                    )
                }
        }
    }

    override suspend fun getUsersReportPaginated(page: Int, pageSize: Int, date: String?, sortBy: String?): List<UserReportDto> {
        return newSuspendedTransaction {
            val offset = (page - 1) * pageSize
            var query = UserTable.selectAll()

            // اعمال فیلتر تاریخ
            date?.let { dateString ->
                try {
                    val localDate = LocalDate.parse(dateString) // Format: YYYY-MM-DD
                    val timeZone = TimeZone.UTC
                    val startOfDay = localDate.atStartOfDayIn(timeZone)
                    val endOfDay = localDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
                    query = query.where { (UserTable.createdAt greaterEq startOfDay) and (UserTable.createdAt less endOfDay) }
                } catch (e: Exception) {
                    // اگر فرمت تاریخ اشتباه بود، فیلتر اعمال نمی‌شود
                }
            }

            // اعمال مرتب‌سازی
            val orderByExpression = when (sortBy) {
                "top_score" -> UserTable.score to SortOrder.DESC
                else -> UserTable.createdAt to SortOrder.DESC // حالت پیش‌فرض (latest)
            }
            query = query.orderBy(orderByExpression)

            query.limit(pageSize).offset(offset.toLong())
                .map { userRow ->
                    val userId = userRow[UserTable.id]
                    val scenarios = (ScenarioProgressTable innerJoin ScenarioTable)
                        .selectAll()
                        .where { ScenarioProgressTable.userId eq userId.value }
                        .map {
                            CompletedScenarioDto(
                                id = it[ScenarioTable.id].toString(),
                                scenarioName = it[ScenarioTable.title]
                            )
                        }
                    val challenges = (ChallengeProgressTable innerJoin ChallengeTable)
                        .selectAll()
                        .where { ChallengeProgressTable.userId eq userId.value }
                        .map {
                            CompletedChallengeDto(
                                id = it[ChallengeTable.id].toString(),
                                challengeName = it[ChallengeTable.title]
                            )
                        }
                    val wordsCount = WordProgressTable
                        .selectAll()
                        .where { WordProgressTable.userId eq userId.value }
                        .count()
                        .toInt()

                    UserReportDto(
                        id = userId.toString(),
                        nickname = userRow[UserTable.nickName],
                        firstname = userRow[UserTable.firstName],
                        lastname = userRow[UserTable.lastName],
                        mobile = userRow[UserTable.mobile],
                        score = userRow[UserTable.score],
                        languageLevel = userRow[UserTable.languageLevel],
                        createdAt = userRow[UserTable.createdAt].toJavaInstant().toHumanReadable(),
                        scenarios = scenarios,
                        challenges = challenges,
                        wordsCount = wordsCount
                    )
                }
        }
    }

    override suspend fun getPurchaseReport(purchaseId: UUID): PurchaseReportDto? {
        // بدون تغییر
        return newSuspendedTransaction {
            (PurchaseTable innerJoin UserTable innerJoin PlanTable)
                .selectAll()
                .where { PurchaseTable.id eq purchaseId }
                .singleOrNull()
                ?.let { row ->
                    val discountCode = row[PurchaseTable.discountCodeId]?.let { discountId ->
                        DiscountCodeTable
                            .selectAll()
                            .where { DiscountCodeTable.id eq discountId }
                            .singleOrNull()
                            ?.get(DiscountCodeTable.code)
                    } ?: ""

                    PurchaseReportDto(
                        discountCode = discountCode,
                        purchaseDate = row[PurchaseTable.purchaseDate].toString(),
                        expiryDate = row[PurchaseTable.expiryDate]?.toString() ?: "",
                        amountPaid = row[PurchaseTable.amountPaid],
                        status = row[PurchaseTable.status].name,
                        token = row[PurchaseTable.token],
                        createdAt = row[PurchaseTable.createdAt].toJavaInstant().toHumanReadable(),
                        user = UserPurchaseDto(
                            id = row[UserTable.id].toString(),
                            nickname = row[UserTable.nickName],
                            firstname = row[UserTable.firstName],
                            lastname = row[UserTable.lastName]
                        ),
                        plan = PlanReportDto(
                            title = row[PlanTable.title],
                            name = row[PlanTable.name],
                            price = row[PlanTable.price],
                            discountedPrice = row[PlanTable.discountedPrice],
                            dayDuration = row[PlanTable.dayDuration]?.toString()
                        ),
                    )
                }
        }
    }

    override suspend fun getPurchasesReportPaginated(page: Int, pageSize: Int, hasPaid: Boolean): List<PurchaseReportDto> {
        return newSuspendedTransaction {
            val offset = (page - 1) * pageSize
            var query = (PurchaseTable innerJoin UserTable innerJoin PlanTable).selectAll()

            // اعمال فیلتر برای پرداخت‌های موفق
            if (hasPaid) {
                // فرض بر این است که amountPaid یک ستون متنی است.
                query = query.where { PurchaseTable.amountPaid greater "0" }
            }

            query.orderBy(PurchaseTable.createdAt, SortOrder.DESC)
                .limit(pageSize).offset(offset.toLong())
                .map { row ->
                    val discountCode = row[PurchaseTable.discountCodeId]?.let { discountId ->
                        DiscountCodeTable
                            .selectAll()
                            .where { DiscountCodeTable.id eq discountId }
                            .singleOrNull()
                            ?.get(DiscountCodeTable.code)
                    } ?: ""

                    PurchaseReportDto(
                        discountCode = discountCode,
                        purchaseDate = row[PurchaseTable.purchaseDate].toString(),
                        expiryDate = row[PurchaseTable.expiryDate]?.toString() ?: "",
                        amountPaid = row[PurchaseTable.amountPaid],
                        status = row[PurchaseTable.status].name,
                        token = row[PurchaseTable.token],
                        createdAt = row[PurchaseTable.createdAt].toJavaInstant().toHumanReadable(),
                        user = UserPurchaseDto(
                            id = row[UserTable.id].toString(),
                            nickname = row[UserTable.nickName],
                            firstname = row[UserTable.firstName],
                            lastname = row[UserTable.lastName]
                        ),
                        plan = PlanReportDto(
                            title = row[PlanTable.title],
                            name = row[PlanTable.name],
                            price = row[PlanTable.price],
                            discountedPrice = row[PlanTable.discountedPrice],
                            dayDuration = row[PlanTable.dayDuration]?.toString()
                        )
                    )
                }
        }
    }
}