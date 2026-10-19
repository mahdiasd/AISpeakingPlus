package ir.aispeaking.androidApp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import co.touchlab.kermit.Logger
import ir.aispeaking.androidApp.AppActivity
import ir.aispeaking.androidApp.R

object NotificationHelper {
    private val logger = Logger.withTag("NotificationHelper")

    const val CHANNEL_ID = "aispeaking_general_channel"
    private const val CHANNEL_NAME = "اطلاعیه‌ها و تمرین‌ها"
    private const val CHANNEL_DESC = "اعلان‌های یادآوری تمرین مکالمه و مراحل جدید"

    /**
     * Creates the Android Notification Channel required on API 26+ (Oreo+).
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
            logger.d { "Notification channel created: $CHANNEL_ID" }
        }
    }

    /**
     * Builds and presents a local notification when an FCM message is received in the foreground,
     * or when handling data-only notifications.
     */
    fun showNotification(
        context: Context,
        title: String?,
        body: String?,
        data: Map<String, String>,
        notificationId: Int = (System.currentTimeMillis() % 100000).toInt()
    ) {
        try {
            createNotificationChannel(context)

            val intent = Intent(context, AppActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                action = "ir.aispeaking.NOTIFICATION_CLICK"
                // Pass all FCM data payload keys into Intent extras for deep linking
                data.forEach { (key, value) ->
                    putExtra(key, value)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notificationTitle = title?.takeIf { it.isNotBlank() } ?: "AI Speaking Plus"
            val notificationBody = body ?: ""

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)

            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
            logger.i { "Notification displayed (ID: $notificationId): title='$notificationTitle'" }
        } catch (e: SecurityException) {
            logger.w(e) { "POST_NOTIFICATIONS permission not granted; cannot display notification" }
        } catch (t: Throwable) {
            logger.e(t) { "Failed to display notification" }
        }
    }
}
