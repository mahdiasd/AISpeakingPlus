package ir.aispeaking.data.payment

import android.content.Context
import android.os.Build
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.repository.PlatformMarketDetector

class AndroidMarketDetector(
    private val context: Context
) : PlatformMarketDetector {

    override fun detectMarket(): AppMarket {
        return try {
            val packageName = context.packageName
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.packageManager.getInstallSourceInfo(packageName).installingPackageName
                } catch (_: Exception) {
                    @Suppress("DEPRECATION")
                    context.packageManager.getInstallerPackageName(packageName)
                }
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(packageName)
            }

            when (installer) {
                "com.farsitel.bazaar" -> AppMarket.CAFE_BAZAAR
                "ir.mservices.market" -> AppMarket.MYKET
                else -> AppMarket.DIRECT_WEB
            }
        } catch (_: Exception) {
            AppMarket.DIRECT_WEB
        }
    }
}
