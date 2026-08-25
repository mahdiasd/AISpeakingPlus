// navigation/src/commonMain/kotlin/ir/aispeaking/navigation/di/AppModule.kt
package ir.aispeaking.navigation.di

import ir.aispeaking.chat.di.ChatKoinModule
import ir.aispeaking.competition.di.CompetitionKoinModule
import ir.aispeaking.editprofile.di.EditProfileKoinModule
import ir.aispeaking.englishLevel.di.EnglishLevelKoinModule
import ir.aispeaking.lightener.di.LightenerKoinModule
import ir.aispeaking.profile.di.ProfileKoinModule
import ir.aispeaking.purchases.di.PurchasesKoinModule
import ir.aispeaking.register.di.RegisterKoinModule
import ir.aispeaking.roadmap.di.RoadmapKoinModule
import ir.aispeaking.scenario_detail.di.ScenarioDetailKoinModule
import ir.aispeaking.scenarios.di.ScenariosKoinModule
import ir.aispeaking.search.di.SearchKoinModule
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
        ir.aispeaking.feature.auth.di.AuthKoinModule::class,
        ir.aispeaking.onboarding.di.OnboardingKoinModule::class,
        ir.aispeaking.splash.di.SplashKoinModule::class,
        ir.aispeaking.main.di.MainKoinModule::class,
        ScenariosKoinModule::class,
        ProfileKoinModule::class,
        RoadmapKoinModule::class,
        CompetitionKoinModule::class,
        EditProfileKoinModule::class,
        EnglishLevelKoinModule::class,
        LightenerKoinModule::class,
        PurchasesKoinModule::class,
        RegisterKoinModule::class,
        ScenarioDetailKoinModule::class,
        SearchKoinModule::class,
        ChatKoinModule::class,
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