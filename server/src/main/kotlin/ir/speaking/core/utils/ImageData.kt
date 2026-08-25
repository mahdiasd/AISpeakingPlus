package ir.speaking.core.utils

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.util.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField

@Serializable
data class ImageData(
    val fileName: String,
    val fileBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (fileName != other.fileName) return false
        if (!fileBytes.contentEquals(other.fileBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class FileUpload(val fieldName: String = "")


data class MultipartResult<T>(
    val data: T,
    val files: Map<String, ImageData> = emptyMap()
)
/**
 * Main generic parser function
 * */
suspend inline fun <reified T : Any> parseMultipartData(
    multipart: MultiPartData,
    json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
): T {
    val result = parseMultipartDataWithFiles<T>(multipart, json)

    // Inject files into data class if it has @FileUpload annotated properties
    injectFiles(result.data, result.files)

    return result.data
}


/**
 * Parser that returns both data and files*/
suspend inline fun <reified T : Any> parseMultipartDataWithFiles(
    multipart: MultiPartData,
    json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        allowSpecialFloatingPointValues = true
        allowStructuredMapKeys = true
    }
): MultipartResult<T> {
    val formData = mutableMapOf<String, JsonElement>()
    val files = mutableMapOf<String, ImageData>()

    multipart.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                val value = part.value
                formData[part.name as String] = parseJsonElement(value)
            }

            is PartData.FileItem -> {
                val fileName = part.originalFileName ?: "${part.name}_${UUID.randomUUID()}.jpg"
                val fileBytes = part.provider().readRemaining().readByteArray()
                files[part.name as String] = ImageData(fileName, fileBytes)
            }

            else -> {}
        }
        part.dispose()
    }


    // Convert to JsonObject and deserialize
    val jsonObject = JsonObject(formData)
//    val data = jsonObject.toString().fromJson<T>()!!
    val data = json.decodeFromJsonElement<T>(jsonObject)

    return MultipartResult(data, files)
}

/**
 * Helper function to parse string values to appropriate JsonElement*/
/**
 * Helper function to parse string values to appropriate JsonElement*/
fun parseJsonElement(value: String): JsonElement {
    return when {
        value.isEmpty() || value == "null" -> JsonNull
        value == "true" || value == "false" -> JsonPrimitive(value.toBoolean())

        // Handle numbers more carefully
        value.matches(Regex("^-?\\d+$")) -> {
            // Pure integer (no decimal point)
            value.toLongOrNull()?.let {
                if (it in Int.MIN_VALUE..Int.MAX_VALUE) {
                    JsonPrimitive(it.toInt())
                } else {
                    JsonPrimitive(it)
                }
            } ?: JsonPrimitive(value)
        }

        value.matches(Regex("^-?\\d+\\.0+$")) -> {
            // Number with only trailing zeros (like 150.0)
            val withoutDecimals = value.substringBefore('.')
            withoutDecimals.toIntOrNull()?.let { JsonPrimitive(it) } ?: JsonPrimitive(value)
        }

        value.toDoubleOrNull() != null -> {
            // Other decimal numbers
            JsonPrimitive(value.toDouble())
        }

        // Handle JSON arrays - this is the key fix
        value.trim().startsWith("[") && value.trim().endsWith("]") -> {
            try {
                // Create a more lenient JSON parser for form data
                val laxJson = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                }
                laxJson.parseToJsonElement(value.trim())
            } catch (e: Exception) {
                println("Failed to parse JSON array: $value")
                println("Error: ${e.message}")
                // If parsing fails, try to clean up the string and parse again
                try {
                    val cleanedValue = value.trim()
                        .replace("\\\"", "\"") // Fix escaped quotes
                        .replace("\"{", "{")   // Remove quotes around objects
                        .replace("}\"", "}")   // Remove quotes around objects
                    Json.parseToJsonElement(cleanedValue)
                } catch (e2: Exception) {
                    println("Failed to parse cleaned JSON: ${e2.message}")
                    JsonPrimitive(value)
                }
            }
        }

        // Handle JSON objects
        value.trim().startsWith("{") && value.trim().endsWith("}") -> {
            try {
                val laxJson = Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                }
                laxJson.parseToJsonElement(value.trim())
            } catch (e: Exception) {
                println("Failed to parse JSON object: $value")
                JsonPrimitive(value)
            }
        }

        else -> JsonPrimitive(value)
    }
}

/**
 * Function to inject files into data class properties marked with @FileUpload*/
inline fun <reified T : Any> injectFiles(instance: T, files: Map<String, ImageData>) {
    T::class.memberProperties.forEach { property ->
        val fileUploadAnnotation = property.findAnnotation<FileUpload>()
        if (fileUploadAnnotation != null) {
            val fieldName = fileUploadAnnotation.fieldName.ifEmpty { property.name }
            val file = files[fieldName]

            if (file != null && property.javaField != null) {
                property.javaField!!.isAccessible = true
                property.javaField!!.set(instance, file)
            }
        }
    }
}
