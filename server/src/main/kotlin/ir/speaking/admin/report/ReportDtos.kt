package ir.speaking.admin.report

import kotlinx.serialization.Serializable

@Serializable
data class UserReportDto(
    val id: String,
    val nickname: String?,
    val firstname: String?,
    val lastname: String?,
    val mobile: String?,
    val score: Int,
    val languageLevel: String,
    val scenarios: List<CompletedScenarioDto>,
    val challenges: List<CompletedChallengeDto>,
    val wordsCount: Int,
    val createdAt: String
)

@Serializable
data class CompletedScenarioDto(
    val id: String,
    val scenarioName: String
)

@Serializable
data class CompletedChallengeDto(
    val id: String,
    val challengeName: String
)


@Serializable
data class PurchaseReportDto(
    val discountCode: String,
    val purchaseDate: String,
    val expiryDate: String,
    val amountPaid: String,
    val status: String,
    val token: String,
    val createdAt: String,
    val user: UserPurchaseDto,
    val plan: PlanReportDto
)

@Serializable
data class UserPurchaseDto(
    val id: String,
    val nickname: String?,
    val firstname: String?,
    val lastname: String?,
)


@Serializable
data class PlanReportDto(
    val title: String,
    val name: String?,
    val price: String?,
    val discountedPrice: String?,
    val dayDuration: String?,
)