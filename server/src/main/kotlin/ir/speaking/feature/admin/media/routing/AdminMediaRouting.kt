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
                var savedMimeType = "image/webp"
                var savedSizeBytes = 0L
                var validationErrorStatus: HttpStatusCode? = null
                var validationErrorMessage: String? = null

                val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")
                val allowedMimeTypes = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
                val maxFileSizeBytes = 5L * 1024L * 1024L // 5 MB

                multipart.forEachPart { part ->
                    if (part is PartData.FileItem && validationErrorStatus == null) {
                        val originalFileName = part.originalFileName ?: "image.webp"
                        val extension = originalFileName.substringAfterLast(".", "").lowercase()
                        val contentTypeStr = part.contentType?.let { "${it.contentType}/${it.contentSubtype}".lowercase() }

                        if (extension !in allowedExtensions || (contentTypeStr != null && contentTypeStr !in allowedMimeTypes && contentTypeStr != "application/octet-stream")) {
                            validationErrorStatus = HttpStatusCode.UnsupportedMediaType
                            validationErrorMessage = "فرمت فایل مجاز نیست. فقط تصاویر JPG، PNG و WEBP مجاز هستند."
                        } else {
                            val bytes = part.streamProvider().use { it.readNBytes((maxFileSizeBytes + 1).toInt()) }
                            if (bytes.size > maxFileSizeBytes) {
                                validationErrorStatus = HttpStatusCode.PayloadTooLarge
                                validationErrorMessage = "حجم فایل بیش از حد مجاز (حداکثر ۵ مگابایت) است."
                            } else {
                                val uniqueName = "${UUID.randomUUID()}.$extension"
                                val file = File(uploadDir, uniqueName)
                                file.writeBytes(bytes)

                                uploadedUrl = "/uploads/stages/$uniqueName"
                                savedFilename = uniqueName
                                savedMimeType = when (extension) {
                                    "jpg", "jpeg" -> "image/jpeg"
                                    "png" -> "image/png"
                                    else -> "image/webp"
                                }
                                savedSizeBytes = bytes.size.toLong()
                            }
                        }
                    }
                    part.dispose()
                }

                if (validationErrorStatus != null) {
                    call.failureRespond(validationErrorStatus!!, validationErrorMessage ?: "خطا در اعتبارسنجی فایل")
                } else if (uploadedUrl != null && savedFilename != null) {
                    call.successRespond(
                        data = AdminMediaUploadResponse(
                            url = uploadedUrl!!,
                            filename = savedFilename!!,
                            mimeType = savedMimeType,
                            sizeBytes = savedSizeBytes
                        ),
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
