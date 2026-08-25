package ir.speaking.core

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.admin.admin.firebase.routing.adminNotificationRouting
import ir.speaking.admin.admin.routing.adminRouting
import ir.speaking.admin.category.routing.adminCategoryRouting
import ir.speaking.admin.challenge.routing.adminChallengeRouting
import ir.speaking.admin.message.adminMessageRouting
import ir.speaking.admin.report.reportRouting
import ir.speaking.admin.scenario.scenario.routing.adminScenarioRouting
import ir.speaking.feature.category.routing.categoryRouting
import ir.speaking.feature.challenge.challenge.routing.challengeRouting
import ir.speaking.feature.challenge.progress.routing.challengeProgressRouting
import ir.speaking.feature.chat.routing.chatRouting
import ir.speaking.feature.config.configRouting
import ir.speaking.feature.discount.routing.discountCodeRouting
import ir.speaking.feature.home.routing.homeRouting
import ir.speaking.feature.lightener.routing.configureTranslationRouting
import ir.speaking.feature.plan.routing.planRouting
import ir.speaking.feature.purchase.routing.purchaseRouting
import ir.speaking.feature.scenario.progress.routing.scenarioProgressRouting
import ir.speaking.feature.scenario.scenario.routing.scenarioRouting
import ir.speaking.feature.scenario.task.routing.scenarioTaskRouting
import ir.speaking.feature.stt.routing.sttRouting
import ir.speaking.feature.tts.routing.ttsRouting
import ir.speaking.feature.user.routing.userRouting
import ir.speaking.feature.word.progress.routing.wordProgressRouting
import ir.speaking.feature.word.word.routing.wordRouting

fun Application.configureRouting() {
    routing {
        staticResources("/resources", "static")

        // Unauthenticated health endpoint for Docker healthcheck and load balancers.
        // Returns 200 OK with a simple JSON body once the app has started.
        get("/health") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "UP"))
        }
    }
    reportRouting()
    adminRouting()
    adminCategoryRouting()
    adminScenarioRouting()
    adminChallengeRouting()
    adminNotificationRouting()
    adminMessageRouting()

    configRouting()
    userRouting()
    categoryRouting()
    scenarioRouting()
    scenarioTaskRouting()
    scenarioProgressRouting()
    homeRouting()
    planRouting()
    purchaseRouting()
    discountCodeRouting()
    configureTranslationRouting()
    chatRouting()
    wordRouting()
    wordProgressRouting()
    challengeRouting()
    challengeProgressRouting()

    sttRouting()
    ttsRouting()
}