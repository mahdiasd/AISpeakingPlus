package ir.aispeaking.androidApp.payment

import android.content.Context
import ir.aispeaking.data.payment.AndroidMarketDetector
import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.repository.PlatformMarketDetector
import org.koin.dsl.module

fun androidPaymentModule(context: Context) = module {
    single<PlatformMarketDetector> { AndroidMarketDetector(context) }
    single<MarketBillingGateway> {
        CompositeMarketBillingGateway(
            context = context,
            detector = get()
        )
    }
}
