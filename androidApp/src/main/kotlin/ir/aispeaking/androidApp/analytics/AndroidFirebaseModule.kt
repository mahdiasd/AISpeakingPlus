package ir.aispeaking.androidApp.analytics

import android.content.Context
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.crashlytics.CrashReporter
import org.koin.dsl.module

fun androidFirebaseModule(context: Context) = module {
    single<AnalyticsTracker> { FirebaseAnalyticsTracker(context) }
    single<CrashReporter> { FirebaseCrashReporter() }
}
