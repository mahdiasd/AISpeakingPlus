package ir.aispeaking.network

fun resolveMediaUrl(url: String): String {
    if (url.startsWith("http://") || url.startsWith("https://")) return url
    val base = platformBaseUrl().trimEnd('/')
    val path = if (url.startsWith("/")) url else "/$url"
    return "$base$path"
}
