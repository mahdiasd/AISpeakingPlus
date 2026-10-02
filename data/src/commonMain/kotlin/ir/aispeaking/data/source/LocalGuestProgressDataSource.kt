package ir.aispeaking.data.source

import com.russhwolf.settings.Settings
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class LocalGuestProgressDataSource(
    private val settings: Settings
) {
    private val key = "KEY_LOCAL_GUEST_PROGRESS"
    private val json = Json { ignoreUnknownKeys = true }

    fun saveProgress(progress: LocalGuestProgress) {
        val currentList = getProgressList().toMutableList()
        val index = currentList.indexOfFirst { it.stageId == progress.stageId }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = progress.copy(
                stars = maxOf(existing.stars, progress.stars),
                score = maxOf(existing.score, progress.score)
            )
        } else {
            currentList.add(progress)
        }
        settings.putString(key, json.encodeToString(currentList))
    }

    fun getProgressList(): List<LocalGuestProgress> {
        val raw: String? = settings.getStringOrNull(key)
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<LocalGuestProgress>>(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun clear() {
        settings.remove(key)
    }
}
