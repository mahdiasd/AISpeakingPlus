package ir.aispeaking.domain.model.scenario

import ir.aispeaking.domain.model.user.Gender
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable
import kotlin.time.Instant

data class Scenario(
    val id: String,
    val categoryId: String,
    val title: String,
    val persianTitle: String = "",
    val description: String,
    val persianDescription: String = "",
    val imageUrl: String?,
    val aiName: String?,
    val aiAvatar: String?,
    val score: Int,
    val tasks: ImmutableList<ScenarioTask>,
    val createdAt: Instant,
    val aiGender: Gender = Gender.Man,
    val starter: Role,
    val isChallenge: Boolean = false,
)

@Serializable
enum class Role(val key: String) {
    System("System"),
    Model("Model"),
    User("User");
}