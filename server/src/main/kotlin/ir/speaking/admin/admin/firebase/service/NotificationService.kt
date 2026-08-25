package ir.speaking.admin.admin.firebase.service

interface NotificationService {
    fun sendNotificationToToken(token: String, title: String, body: String, imageUrl: String? = null)
    fun sendNotificationToTopic(topic: String, title: String, body: String, imageUrl: String? = null)
    suspend fun sendNotificationToAllUsers(title: String, body: String, imageUrl: String? = null)
}