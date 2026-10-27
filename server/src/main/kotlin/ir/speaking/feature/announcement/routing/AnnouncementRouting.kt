package ir.speaking.feature.announcement.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.successRespond
import ir.speaking.feature.announcement.model.AnnouncementDto
import ir.speaking.feature.announcement.service.AnnouncementService

@OptIn(ExperimentalKtorApi::class)
fun Application.announcementRouting(announcementService: AnnouncementService = AnnouncementService()) {
    routing {
        route("/api/announcements") {
            get("/active") {
                val active = announcementService.getActiveAnnouncement()
                call.successRespond(active, message = if (active != null) "اطلاعیه فعال یافت شد" else "هیچ اطلاعیه فعالی وجود ندارد")
            }.describe {
                tag("Announcements")
                summary = "Get active announcement"
                description = "Get current active system-wide broadcast announcement for in-app dialogs"
                responses {
                    HttpStatusCode.OK {
                        description = "Active announcement or null"
                        schema = jsonSchema<SuccessResponse<AnnouncementDto?>>()
                    }
                }
            }
        }
    }
}
