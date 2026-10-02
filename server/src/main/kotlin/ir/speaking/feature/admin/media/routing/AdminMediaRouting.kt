package ir.speaking.feature.admin.media.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.model.AdminMediaUploadResponse
import java.io.File
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Route.adminMediaRouting() {
    val uploadDir = File("uploads/stages")
    if (!uploadDir.exists()) {
        uploadDir.mkdirs()
    }

    // Static files serving for uploaded assets
    staticFiles("/uploads", File("uploads"))

    route("/api/admin/media") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            post("/upload") {
                val multipart = try {
                    call.receiveMultipart()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "ارسال فایل چندبخشی (Multipart) نامعتبر است")
                    return@post
                }

                var uploadedUrl: String? = null
                var savedFilename: String? = null

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem) {
                        val originalFileName = part.originalFileName ?: "image.webp"
                        val extension = originalFileName.substringAfterLast(".", "webp")
                        val uniqueName = "${UUID.randomUUID()}.$extension"
                        val file = File(uploadDir, uniqueName)

                        part.streamProvider().use { input ->
                            file.outputStream().buffered().use { output ->
                                input.copyTo(output)
                            }
                        }

                        uploadedUrl = "/uploads/stages/$uniqueName"
                        savedFilename = uniqueName
                    }
                    part.dispose()
                }

                if (uploadedUrl != null && savedFilename != null) {
                    call.successRespond(
                        data = AdminMediaUploadResponse(url = uploadedUrl!!, filename = savedFilename!!),
                        message = "فایل با موفقیت آپلود شد"
                    )
                } else {
                    call.failureRespond(HttpStatusCode.BadRequest, "هیچ فایلی در درخواست دریافت نشد")
                }
            }.describe {
                tag("Admin Media")
                summary = "Upload Media Asset"
                description = "Upload image file for stage background or avatar"
                responses {
                    HttpStatusCode.OK {
                        description = "Media uploaded"
                        schema = jsonSchema<SuccessResponse<AdminMediaUploadResponse>>()
                    }
                }
            }
        }
    }
}
