package ir.speaking.feature.admin.promo.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.admin.promo.model.*
import ir.speaking.feature.admin.promo.service.AdminPromoService
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Route.adminPromoRouting(
    adminPromoService: AdminPromoService,
    auditLogService: AuditLogService
) {
    route("/api/admin/promo-codes") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get {
                val search = call.request.queryParameters["search"]
                val status = call.request.queryParameters["status"]
                val res = adminPromoService.getPromoCodes(search, status)
                call.successRespond(res, message = "کدهای تخفیف با موفقیت دریافت شدند")
            }.describe {
                tag("Admin Promo Codes")
                summary = "List Promo Codes"
                description = "Get all promo codes with search and status filtering, including aggregate stats"
                responses {
                    HttpStatusCode.OK {
                        description = "Promo codes list with stats"
                        schema = jsonSchema<SuccessResponse<AdminPromoListResponse>>()
                    }
                }
            }

            post {
                val principal = call.principal<AdminPrincipal>()
                val req = try {
                    call.receive<AdminPromoCodeUpsertRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست ایجاد کد تخفیف نامعتبر است")
                    return@post
                }

                val created = try {
                    adminPromoService.createPromoCode(principal?.id, req)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "اطلاعات ورودی نامعتبر است")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "PROMO_CODE_CREATE",
                    targetType = "PROMO_CODE",
                    targetId = created.id,
                    detailsJson = buildJsonObject {
                        put("code", created.code)
                        put("discountType", created.discountType)
                        put("discountValue", created.discountValue)
                    }.toString()
                )

                call.successRespond(created, message = "کد تخفیف با موفقیت ایجاد شد")
            }.describe {
                tag("Admin Promo Codes")
                summary = "Create Promo Code"
                description = "Create a new discount code with limits and plan rules"
                requestBody {
                    description = "Promo code configuration"
                    schema = jsonSchema<AdminPromoCodeUpsertRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Promo code created successfully"
                        schema = jsonSchema<SuccessResponse<AdminPromoCodeItemDto>>()
                    }
                }
            }

            put("/{id}") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["id"] ?: return@put call.failureRespond(HttpStatusCode.BadRequest, "شناسه کد تخفیف الزامی است")
                val promoId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@put call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه نامعتبر است")
                }

                val req = try {
                    call.receive<AdminPromoCodeUpsertRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست ویرایش نامعتبر است")
                    return@put
                }

                val updated = try {
                    adminPromoService.updatePromoCode(promoId, req)
                } catch (e: NoSuchElementException) {
                    call.failureRespond(HttpStatusCode.NotFound, "کد تخفیف یافت نشد")
                    return@put
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "اطلاعات ورودی نامعتبر است")
                    return@put
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "PROMO_CODE_UPDATE",
                    targetType = "PROMO_CODE",
                    targetId = updated.id,
                    detailsJson = buildJsonObject {
                        put("code", updated.code)
                        put("discountType", updated.discountType)
                        put("discountValue", updated.discountValue)
                    }.toString()
                )

                call.successRespond(updated, message = "کد تخفیف با موفقیت ویرایش شد")
            }.describe {
                tag("Admin Promo Codes")
                summary = "Update Promo Code"
                description = "Update an existing discount code"
                parameters {
                    path("id") { description = "Promo Code UUID" }
                }
                requestBody {
                    description = "Updated promo code payload"
                    schema = jsonSchema<AdminPromoCodeUpsertRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Promo code updated successfully"
                        schema = jsonSchema<SuccessResponse<AdminPromoCodeItemDto>>()
                    }
                }
            }

            patch("/{id}/status") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["id"] ?: return@patch call.failureRespond(HttpStatusCode.BadRequest, "شناسه کد تخفیف الزامی است")
                val promoId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@patch call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه نامعتبر است")
                }

                val req = try {
                    call.receive<AdminPromoStatusToggleRequest>()
                } catch (_: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست تغییر وضعیت نامعتبر است")
                    return@patch
                }

                val updated = try {
                    adminPromoService.toggleStatus(promoId, req.isActive)
                } catch (e: NoSuchElementException) {
                    call.failureRespond(HttpStatusCode.NotFound, "کد تخفیف یافت نشد")
                    return@patch
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "PROMO_CODE_STATUS_TOGGLE",
                    targetType = "PROMO_CODE",
                    targetId = updated.id,
                    detailsJson = buildJsonObject {
                        put("code", updated.code)
                        put("isActive", updated.isActive)
                    }.toString()
                )

                call.successRespond(updated, message = "وضعیت کد تخفیف با موفقیت تغییر یافت")
            }.describe {
                tag("Admin Promo Codes")
                summary = "Toggle Promo Status"
                description = "Activate or deactivate a promo code quickly"
                parameters {
                    path("id") { description = "Promo Code UUID" }
                }
                requestBody {
                    description = "Active toggle payload"
                    schema = jsonSchema<AdminPromoStatusToggleRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Status updated"
                        schema = jsonSchema<SuccessResponse<AdminPromoCodeItemDto>>()
                    }
                }
            }

            delete("/{id}") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["id"] ?: return@delete call.failureRespond(HttpStatusCode.BadRequest, "شناسه کد تخفیف الزامی است")
                val promoId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@delete call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه نامعتبر است")
                }

                val success = adminPromoService.deletePromoCode(promoId)
                if (!success) {
                    call.failureRespond(HttpStatusCode.NotFound, "کد تخفیف یافت نشد")
                    return@delete
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "PROMO_CODE_DELETE",
                    targetType = "PROMO_CODE",
                    targetId = promoId.toString(),
                    detailsJson = buildJsonObject {
                        put("deleted", true)
                    }.toString()
                )

                call.successRespond(mapOf("deleted" to true), message = "کد تخفیف با موفقیت بایگانی/حذف شد")
            }.describe {
                tag("Admin Promo Codes")
                summary = "Delete Promo Code"
                description = "Soft delete / archive a discount code"
                parameters {
                    path("id") { description = "Promo Code UUID" }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Promo code deleted"
                        schema = jsonSchema<SuccessResponse<Map<String, Boolean>>>()
                    }
                }
            }

            get("/{id}/redemptions") {
                val idStr = call.parameters["id"] ?: return@get call.failureRespond(HttpStatusCode.BadRequest, "شناسه کد تخفیف الزامی است")
                val promoId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@get call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه نامعتبر است")
                }

                val redemptions = adminPromoService.getRedemptions(promoId)
                call.successRespond(redemptions, message = "سوابق استفاده با موفقیت دریافت شد")
            }.describe {
                tag("Admin Promo Codes")
                summary = "Get Promo Redemptions"
                description = "Get list of users who redeemed this promo code"
                parameters {
                    path("id") { description = "Promo Code UUID" }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "List of redemptions"
                        schema = jsonSchema<SuccessResponse<List<AdminPromoRedemptionItemDto>>>()
                    }
                }
            }
        }
    }
}
