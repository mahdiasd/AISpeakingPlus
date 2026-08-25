package ir.aispeaking.domain.usecase.purchase
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.repository.purchase.PurchaseRepository
import org.koin.core.annotation.Single

@Single
class CreatePurchaseUseCase(private val repo: PurchaseRepository) {
    suspend operator fun invoke(purchase: Purchase) = repo.createPurchase(purchase)
}
