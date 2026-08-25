package ir.speaking.core.network.utils
object PrintHelper {

    // ANSI Color Codes
    private const val RESET = "\u001B[0m"
    private const val RED = "\u001B[31m"
    private const val GREEN = "\u001B[32m"
    private const val YELLOW = "\u001B[33m"
    private const val BLUE = "\u001B[34m"
    private const val PURPLE = "\u001B[35m"
    private const val CYAN = "\u001B[36m"
    private const val WHITE = "\u001B[37m"

    // Background colors
    private const val BG_RED = "\u001B[41m"
    private const val BG_GREEN = "\u001B[42m"
    private const val BG_YELLOW = "\u001B[43m"
    private const val BG_BLUE = "\u001B[44m"

    // Text styles
    private const val BOLD = "\u001B[1m"
    private const val UNDERLINE = "\u001B[4m"

    private fun createSeparator(char: String = "=", length: Int = 80): String {
        return char.repeat(length)
    }

    private fun getCurrentTimestamp(): String {
        return java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        )
    }

    fun error(message: String, throwable: Throwable? = null) {
        val timestamp = getCurrentTimestamp()
        val separator = createSeparator("━", 80)

        println()
        println("$RED$BOLD$separator$RESET")
        println("$BG_RED$WHITE$BOLD ❌ ERROR ❌ $RESET $RED[$timestamp]$RESET")
        println("$RED$BOLD$separator$RESET")
        println("$RED$BOLD Message: $RESET$RED$message$RESET")

        throwable?.let {
            println("$RED$BOLD Exception: $RESET$RED${it::class.simpleName}$RESET")
            println("$RED$BOLD Cause: $RESET$RED${it.message}$RESET")
            if (it.stackTrace.isNotEmpty()) {
                println("$RED$BOLD Stack Trace:$RESET")
                it.stackTrace.take(5).forEach { stackElement ->
                    println("$RED  → ${stackElement}$RESET")
                }
            }
        }

        println("$RED$BOLD${createSeparator("━", 80)}$RESET")
        println()
    }

    fun success(message: String, data: Any? = null) {
        val timestamp = getCurrentTimestamp()
        val separator = createSeparator("─", 80)

        println()
        println("$GREEN$BOLD$separator$RESET")
        println("$BG_GREEN$WHITE$BOLD ✅ SUCCESS ✅ $RESET $GREEN[$timestamp]$RESET")
        println("$GREEN$BOLD$separator$RESET")
        println("$GREEN$BOLD Message: $RESET$GREEN$message$RESET")

        data?.let {
            println("$GREEN$BOLD Data: $RESET$CYAN$it$RESET")
        }

        println("$GREEN$BOLD${createSeparator("─", 80)}$RESET")
        println()
    }

    fun info(message: String, details: Any? = null) {
        val timestamp = getCurrentTimestamp()
        val separator = createSeparator("·", 80)

        println()
        println("$BLUE$BOLD$separator$RESET")
        println("$BG_BLUE$WHITE$BOLD ℹ️  INFO ℹ️  $RESET $BLUE[$timestamp]$RESET")
        println("$BLUE$BOLD$separator$RESET")
        println("$BLUE$BOLD Message: $RESET$CYAN$message$RESET")

        details?.let {
            println("$BLUE$BOLD Details: $RESET$PURPLE$it$RESET")
        }

        println("$BLUE$BOLD${createSeparator("·", 80)}$RESET")
        println()
    }

    fun warning(message: String, details: Any? = null) {
        val timestamp = getCurrentTimestamp()
        val separator = createSeparator("~", 80)

        println()
        println("$YELLOW$BOLD$separator$RESET")
        println("$BG_YELLOW$WHITE$BOLD ⚠️  WARNING ⚠️  $RESET $YELLOW[$timestamp]$RESET")
        println("$YELLOW$BOLD$separator$RESET")
        println("$YELLOW$BOLD Message: $RESET$YELLOW$message$RESET")

        details?.let {
            println("$YELLOW$BOLD Details: $RESET$PURPLE$it$RESET")
        }

        println("$YELLOW$BOLD${createSeparator("~", 80)}$RESET")
        println()
    }

    fun debug(message: String, data: Any? = null) {
        val timestamp = getCurrentTimestamp()
        val separator = createSeparator(".", 80)

        println()
        println("$PURPLE$separator$RESET")
        println("$PURPLE$BOLD 🐛 DEBUG 🐛 $RESET $PURPLE[$timestamp]$RESET")
        println("$PURPLE$separator$RESET")
        println("$PURPLE Message: $message$RESET")

        data?.let {
            println("$PURPLE Data: $CYAN$it$RESET")
        }

        println("$PURPLE$separator$RESET")
        println()
    }
}