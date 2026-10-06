package ir.speaking

import io.ktor.server.application.*
import ir.speaking.core.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureDatabases()
    configureDI()
    configureSecurity()
    configureSerialization()
    configureCORS()
    configureWebSockets()
    configureRoutingException()
    configureRouting()
}
