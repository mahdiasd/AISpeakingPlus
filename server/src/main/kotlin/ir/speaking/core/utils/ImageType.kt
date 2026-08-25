package ir.speaking.core.utils

sealed class ImageType(val folderName: String) {
    data object SCENARIO : ImageType("scenario")
    data object CHALLENGE : ImageType("challenge")
    data object CATEGORY : ImageType("category")
    data object Message : ImageType("message")
}
