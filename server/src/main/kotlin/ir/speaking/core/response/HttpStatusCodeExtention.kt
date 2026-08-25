package ir.speaking.core.response

import io.ktor.http.*
fun HttpStatusCode.getMessage(): String {
    return when (this) {
        // 1xx Informational responses
        HttpStatusCode.Continue -> "Continue"
        HttpStatusCode.SwitchingProtocols -> "Switching protocols..."
        HttpStatusCode.Processing -> "Processing..."

        // 2xx Success
        HttpStatusCode.OK -> "Success!"
        HttpStatusCode.Created -> "Successfully created."
        HttpStatusCode.Accepted -> "Your request has been accepted."
        HttpStatusCode.NonAuthoritativeInformation -> "Information may be outdated."
        HttpStatusCode.NoContent -> "No content to display."
        HttpStatusCode.ResetContent -> "Content has been reset."
        HttpStatusCode.PartialContent -> "Partial content loaded."

        // 3xx Redirection
        HttpStatusCode.MultipleChoices -> "More than one option is available."
        HttpStatusCode.MovedPermanently -> "This page has moved permanently."
        HttpStatusCode.Found -> "Resource found."
        HttpStatusCode.SeeOther -> "Please see another resource."
        HttpStatusCode.NotModified -> "The content has not changed."
        HttpStatusCode.UseProxy -> "A proxy must be used."
        HttpStatusCode.TemporaryRedirect -> "You are being temporarily redirected."
        HttpStatusCode.PermanentRedirect -> "You are being permanently redirected."

        // 4xx Client errors
        HttpStatusCode.BadRequest -> "There was a problem with your request. Please try again."
        HttpStatusCode.Unauthorized -> "Authentication failed. Please log in again."
        HttpStatusCode.PaymentRequired -> "Payment is required to proceed."
        HttpStatusCode.Forbidden -> "You don't have permission to access this."
        HttpStatusCode.NotFound -> "We couldn't find what you were looking for."
        HttpStatusCode.MethodNotAllowed -> "This action is not permitted."
        HttpStatusCode.NotAcceptable -> "The request is not acceptable."
        HttpStatusCode.ProxyAuthenticationRequired -> "Proxy authentication is required."
        HttpStatusCode.RequestTimeout -> "The request timed out. Please check your connection and try again."
        HttpStatusCode.Conflict -> "There was a conflict with your request. Please try again."
        HttpStatusCode.Gone -> "This content is no longer available."
        HttpStatusCode.LengthRequired -> "The request is missing a required length."
        HttpStatusCode.PreconditionFailed -> "A condition for the request failed."
        HttpStatusCode.PayloadTooLarge -> "The file or data you are trying to send is too large."
        HttpStatusCode.UnsupportedMediaType -> "This file type is not supported."
        HttpStatusCode.ExpectationFailed -> "The server could not meet the request's requirements."
        HttpStatusCode.UnprocessableEntity -> "The request was understood, but we couldn't process it."
        HttpStatusCode.Locked -> "This item is locked."
        HttpStatusCode.FailedDependency -> "A required action failed."
        HttpStatusCode.UpgradeRequired -> "Please upgrade your application to continue."
        HttpStatusCode.TooManyRequests -> "You've made too many requests. Please wait a moment and try again."

        // 5xx Server errors
        HttpStatusCode.InternalServerError -> "Something went wrong on our end. Please try again later."
        HttpStatusCode.NotImplemented -> "This feature is not yet available."
        HttpStatusCode.BadGateway -> "We're having trouble connecting. Please try again."
        HttpStatusCode.ServiceUnavailable -> "The service is temporarily unavailable. Please try again later."
        HttpStatusCode.GatewayTimeout -> "We couldn't get a response in time. Please check your connection and try again."
        HttpStatusCode.VariantAlsoNegotiates -> "There is a configuration error."
        HttpStatusCode.InsufficientStorage -> "There is not enough space to complete this action."

        // Default case
        else -> "An unknown error occurred. Please try again."
    }
}