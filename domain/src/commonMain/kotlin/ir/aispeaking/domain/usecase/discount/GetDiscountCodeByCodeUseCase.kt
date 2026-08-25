package ir.aispeaking.domain.usecase.discount

import ir.aispeaking.domain.repository.discount.DiscountCodeRepository
import org.koin.core.annotation.Single

@Single
class GetDiscountCodeByCodeUseCase(private val repo: DiscountCodeRepository) {
    suspend operator fun invoke(code: String) = repo.checkDiscount(code)
}