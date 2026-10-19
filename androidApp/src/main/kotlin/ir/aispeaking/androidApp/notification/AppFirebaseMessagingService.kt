package ir.aispeaking.androidApp.notification

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import ir.aispeaking.utils.analytics.AnalyticsTracker
import org.koin.core.context.GlobalContext

class AppFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = Logger.withTag("AppFirebaseMessaging")

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.i { "FCM Registration Token refreshed: $token" }

        try {
            val analytics = GlobalContext.get().getOrNull<AnalyticsTracker>()
            analytics?.setUserProperty("fcm_token", token)
            analytics?.logEvent(
                "fcm_token_refreshed",
                mapOf("platform" to "android")
            )
        } catch (t: Throwable) {
            logger.e(t) { "Failed to record new FCM token in analytics" }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        logger.d { "FCM Message received from: ${remoteMessage.from}, data=${remoteMessage.data}" }

        val data = remoteMessage.data
        val notificationTitle = remoteMessage.notification?.title
            ?: data["title"]
            ?: "AI Speaking Plus"
        val notificationBody = remoteMessage.notification?.body
            ?: data["body"]
            ?: data["message"]
            ?: ""

        try {
            val analytics = GlobalContext.get().getOrNull<AnalyticsTracker>()
            val stageId = data["stage_id"] ?: data["stageId"] ?: ""
            val screen = data["screen"] ?: ""
            analytics?.logEvent(
                "notification_received",
                mapOf(
                    "platform" to "android",
                    "title" to notificationTitle,
                    "stage_id" to stageId,
                    "screen" to screen
                )
            )
        } catch (_: Throwable) {}

        // Present local notification with pending intent containing all deep link payload fields
        NotificationHelper.showNotification(
            context = applicationContext,
            title = notificationTitle,
            body = notificationBody,
            data = data
        )
    }
}
