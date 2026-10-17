package ir.speaking.feature.subscription.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import ir.speaking.core.network.utils.PrintHelper
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import java.util.Date
import java.util.UUID

@Single
class DynamicDiscountService {

    private val bazaarPackageName = System.getenv("BAZAAR_PACKAGE_NAME") ?: "ir.aispeaking.androidApp"
    private val discountSecret = System.getenv("BAZAAR_DISCOUNT_SECRET")
        ?: System.getenv("BAZAAR_CLIENT_SECRET")
        ?: "bazaar_dynamic_discount_secret"

    fun generateDiscountToken(
        sku: String,
        discountedPriceTomans: Long,
        packageName: String = bazaarPackageName,
        expireMinutes: Long = 30,
        accountId: String? = null
    ): String {
        return try {
            val priceRials = discountedPriceTomans * 10L // Convert Tomans to Rials as required by Bazaar
            val nowMs = Clock.System.now().toEpochMilliseconds()
            val expiresAt = Date(nowMs + (expireMinutes * 60 * 1000L))
            val nonce = UUID.randomUUID().toString()

            val algorithm = Algorithm.HMAC256(discountSecret)
            val builder = JWT.create()
                .withClaim("price", priceRials)
                .withClaim("package_name", packageName)
                .withClaim("sku", sku)
                .withClaim("nonce", nonce)
                .withExpiresAt(expiresAt)

            if (!accountId.isNullOrBlank()) {
                builder.withClaim("account_id", accountId)
            }

            builder.sign(algorithm)
        } catch (e: Exception) {
            PrintHelper.error("Error generating dynamic discount token: ${e.message}")
            "token_generation_failed"
        }
    }
}
