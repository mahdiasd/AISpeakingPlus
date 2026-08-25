package ir.speaking

import io.ktor.utils.io.*
import io.ktor.utils.io.locks.*
import kotlinx.serialization.json.JsonObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileLogger {
    private const val LOG_DIR_NAME = "application_logs"
    @OptIn(InternalAPI::class)
    private val logFileLock = ReentrantLock()
    
    private val logDirectory by lazy {
        File(System.getProperty("user.dir"), LOG_DIR_NAME).apply {
            if (!exists()) mkdirs()
        }
    }

    private val currentLogFile by lazy {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        File(logDirectory, "log_$timestamp.log").apply {
            createNewFile()
        }
    }

    private val jsonLogFile by lazy {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm"))
        File(logDirectory, "log_$timestamp.json").apply {
            createNewFile()
        }
    }

    private val logFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")

    @OptIn(InternalAPI::class)
    fun log(message: String) {
        logFileLock.withLock {
            try {
                val logEntry = buildString {
                    append("[${LocalDateTime.now().format(logFormatter)}]")
                    append(" $message\n")
                }
                
                currentLogFile.appendText(logEntry)
            } catch (e: Exception) {
                System.err.println("Logging failed: ${e.javaClass.simpleName} - ${e.message}")
            }
        }
    }

    @OptIn(InternalAPI::class)
    fun logJson(jsonObject: JsonObject) {
        logFileLock.withLock {
            try {
                val logEntry = buildString {
                    append(" $jsonObject\n")
                }

                jsonLogFile.appendText(logEntry)
            } catch (e: Exception) {
                System.err.println("Logging failed: ${e.javaClass.simpleName} - ${e.message}")
            }
        }
    }

    fun logWithContext(message: String, context: Map<String, Any?> = emptyMap()) {
        val contextString = context.entries.joinToString(", ") { "${it.key}=${it.value}" }
        log("$message | $contextString")
    }
}