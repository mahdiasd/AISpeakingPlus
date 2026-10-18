package ir.aispeaking.domain.repository

import ir.aispeaking.domain.model.payment.AppMarket

interface PlatformMarketDetector {
    fun detectMarket(): AppMarket
}
