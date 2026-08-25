package ir.aispeaking.domain.usecase.purchase

import ir.aispeaking.domain.repository.purchase.PurchaseRepository
import org.koin.core.annotation.Single

@Single
class GetActivePurchasesUseCase(private val repo: PurchaseRepository) {
    suspend operator fun invoke() = repo.getActivePurchasesByUserId()
}