package ir.speaking

import io.ktor.server.application.*
import ir.speaking.admin.admin.firebase.configureFirebase
import ir.speaking.core.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureFirebase()

    configureDatabases()

    configureDI()

    configureSecurity()

    configureSerialization()

    configureCORS()

    configureWebSockets()

    configureRoutingException()

    configureRouting()

//    configureSeedData()

//    routing {
//        staticResources("/resources", "static")
//    }
}

