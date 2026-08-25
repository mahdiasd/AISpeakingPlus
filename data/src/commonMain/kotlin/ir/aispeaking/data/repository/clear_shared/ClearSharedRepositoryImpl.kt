package ir.aispeaking.data.repository.clear_shared

import ir.aispeaking.domain.repository.clear_shared.ClearSharedRepository
import ir.aispeaking.storage.preferences.clear.ClearSharedPreferences
import org.koin.core.annotation.Single

@Single
class ClearSharedRepositoryImpl(private val pref: ClearSharedPreferences) : ClearSharedRepository {

    override suspend fun clear() {
        pref.clear()
    }

}