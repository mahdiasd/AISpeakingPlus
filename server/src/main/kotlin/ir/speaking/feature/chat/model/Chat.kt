package ir.speaking.feature.chat.model

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Chat(
    val uid: String = UUID.randomUUID().toString(),
    val userId: String,
    val scenarioId: String,
    val message: String,
    val role: Role = Role.User
)

@Serializable
enum class Role(val key: String) {
    System("System"),
    Model("Model"),
    User("User");
}

fun String.toRole(): Role {
    return Role.entries.find { it.key.equals(this, true) } ?: throw IllegalArgumentException("Unknown role: $this")
}