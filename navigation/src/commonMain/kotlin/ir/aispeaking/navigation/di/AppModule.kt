package ir.aispeaking.navigation.di

import ir.aispeaking.sharedui.ui.di.UiKoinModule
import ir.aispeaking.utils.di.UtilsKoinModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module
import org.koin.plugin.module.dsl.startKoin

@Module
@Configuration
class MyModule

@KoinApplication
@ComponentScan("ir.aispeaking.navigation")
@Module(
    includes = [
        ir.aispeaking.domain.di.DomainKoinModule::class,
        ir.aispeaking.data.di.DataKoinModule::class,
        ir.aispeaking.network.di.NetworkKoinModule::class,
        ir.aispeaking.storage.di.StorageKoinModule::class,
        UiKoinModule::class,
        UtilsKoinModule::class,
    ]
)
class MyApp

fun initKoin() {
    startKoin<MyApp> {
        printLogger()
    }
}