package ir.speaking.core.utils

private const val ANSI_BLACK_BACKGROUND = "\u001B[40m"
private const val ANSI_RED_BACKGROUND = "\u001B[41m"
private const val ANSI_GREEN_BACKGROUND = "\u001B[42m"
private const val ANSI_YELLOW_BACKGROUND = "\u001B[43m"
private const val ANSI_BLUE_BACKGROUND = "\u001B[44m"
private const val ANSI_PURPLE_BACKGROUND = "\u001B[45m"
private const val ANSI_CYAN_BACKGROUND = "\u001B[46m"
private const val ANSI_WHITE_BACKGROUND = "\u001B[47m"
private const val ANSI_RESET = "\u001B[0m"


fun logError(message: String) {
    println("$ANSI_RED_BACKGROUND$message$ANSI_RESET")
}

fun logSuccess(message: String) {
    println("$ANSI_GREEN_BACKGROUND$message$ANSI_RESET")
}

fun logWarning(message: String) {
    println("$ANSI_YELLOW_BACKGROUND$message$ANSI_RESET")
}

fun logInfo(message: String) {
    println("$ANSI_BLUE_BACKGROUND$message$ANSI_RESET")
}

fun logDebug(message: String) {
    println("$ANSI_PURPLE_BACKGROUND$message$ANSI_RESET")
}

fun logNotice(message: String) {
    println("$ANSI_CYAN_BACKGROUND$message$ANSI_RESET")
}

fun logDefault(message: String) {
    println("$ANSI_WHITE_BACKGROUND$message$ANSI_RESET")
}

fun logCritical(message: String) {
    println("$ANSI_BLACK_BACKGROUND$message$ANSI_RESET")
}
