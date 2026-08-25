package ir.aispeaking.storage.preferences.user

import ir.aispeaking.storage.model.user.SharedPrefUser

interface UserPreferences {

    fun save(user: SharedPrefUser?)

    fun read(): SharedPrefUser?
}
