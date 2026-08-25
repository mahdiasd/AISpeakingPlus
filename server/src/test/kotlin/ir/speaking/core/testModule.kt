package ir.speaking.core

import io.ktor.server.application.*
import ir.speaking.di.AppModule
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.testModule(dropTables: Boolean = false) {
    install(Koin) {
        slf4jLogger()
        modules(AppModule().module)
    }

    configureTestSecurity()
    configureSerialization()
    configureRoutingException()
    configureTestDatabases(dropTables)

    configureRouting()
}
