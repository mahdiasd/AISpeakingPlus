package ir.aispeaking.storage.di

import com.russhwolf.settings.Settings
import org.koin.core.annotation.Single

@Single
fun provideSettings(): Settings = Settings()