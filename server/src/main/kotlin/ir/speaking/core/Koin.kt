package ir.speaking.core

import io.ktor.server.application.*
import ir.speaking.di.AppModule
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDI() {
    install(Koin) {
        slf4jLogger()
        modules(AppModule().module)
    }
}