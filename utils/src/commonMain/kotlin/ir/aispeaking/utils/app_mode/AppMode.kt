package ir.aispeaking.utils.app_mode

enum class AppMode {
    Online,
    Offline
}

/**
 * Defines the application distribution mode: Online or Offline.
 * This setting is determined at build time
 * And dictates specific configurations throughout the application.
 * For example, the Offline mode might enable features like fake SSL certificates for socket setup
 * For sample see: provideSocket.kt
 */
val ApplicationMode = AppMode.Online
