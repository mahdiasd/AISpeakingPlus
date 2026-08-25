package ir.speaking.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCORS() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(HttpHeaders.Referrer)
        allowHeader(HttpHeaders.AcceptLanguage)
        allowHeader(HttpHeaders.AcceptEncoding)
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.AccessControlAllowHeaders)
        allowHeader(HttpHeaders.AccessControlAllowMethods)
        allowHeader(HttpHeaders.AccessControlRequestHeaders)
        allowHeader(HttpHeaders.AccessControlRequestMethod)
        allowHeader(HttpHeaders.Upgrade)
        allowHeader(HttpHeaders.Connection)
        allowHeaders { true }
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Head)
        allowNonSimpleContentTypes = true
    }
}
