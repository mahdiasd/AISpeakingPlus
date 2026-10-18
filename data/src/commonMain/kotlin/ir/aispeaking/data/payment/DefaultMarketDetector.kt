package ir.aispeaking.data.payment

import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.repository.PlatformMarketDetector
import org.koin.core.annotation.Single

@Single
class DefaultMarketDetector : PlatformMarketDetector {
    override fun detectMarket(): AppMarket {
        return AppMarket.DIRECT_WEB
    }
}
