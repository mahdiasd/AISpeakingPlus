package ir.speaking.core.utils

import ir.speaking.core.exeptions.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.*

private const val STATIC_DIR = "src/main/resources/static"
private const val MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")


object ImageSaver {
    /**
     * Saves an image to the resources/static/{imageType} folder
     * @param fileName Original file name
     * @param fileBytes File content as byte array
     * @param imageType Type of image (determines folder)
     * @return Relative URL path to access the image
     */
    suspend fun saveImage(
        fileName: String,
        fileBytes: ByteArray,
        imageType: ImageType
    ): String {
        return withContext(Dispatchers.IO) {
            if (fileBytes.size > MAX_FILE_SIZE) {
                throw AppException.BadRequest("File size exceeds maximum allowed size (5MB)")
            }

            val extension = fileName.substringAfterLast('.').lowercase()
            if (extension !in ALLOWED_EXTENSIONS) {
                throw AppException.BadRequest("Invalid file type. Allowed types: ${ALLOWED_EXTENSIONS.joinToString()}")
            }

            val uploadDir = File("$STATIC_DIR/${imageType.folderName}")
            if (!uploadDir.exists()) {
                uploadDir.mkdirs()
            }

            val uniqueFileName = "${UUID.randomUUID()}_${fileName.replace(" ", "_")}"
            val file = File(uploadDir, uniqueFileName)

            file.writeBytes(fileBytes)

            "${imageType.folderName}/$uniqueFileName"
        }
    }

    /**
     * Deletes an image from the static folder
     * @param imageUrl The URL path of the image (e.g., "/static/scenario/uuid_image.jpg")
     */
    suspend fun deleteImage(imageUrl: String?) {
        withContext(Dispatchers.IO) {
            if (imageUrl.isNullOrBlank()) return@withContext

            try {
                // Extract the relative path from URL
                val relativePath = imageUrl.removePrefix("/")
                val file = File("src/main/resources/$relativePath")

                if (file.exists()) {
                    file.delete()
                }
            } catch (e: Exception) {
                // Log error but don't throw - image deletion failure shouldn't break the flow
                println("Failed to delete image: ${e.message}")
            }
        }
    }

    /**
     * Moves an image from one location to another (useful for temp uploads)
     */
    suspend fun moveImage(
        oldUrl: String,
        newImageType: ImageType
    ): String? {
        return withContext(Dispatchers.IO) {
            if (oldUrl.isBlank()) return@withContext null

            try {
                val oldPath = oldUrl.removePrefix("/")
                val oldFile = File("src/main/resources/$oldPath")

                if (!oldFile.exists()) return@withContext null

                val fileName = oldFile.name
                val newDir = File("$STATIC_DIR/${newImageType.folderName}")
                if (!newDir.exists()) {
                    newDir.mkdirs()
                }

                val newFile = File(newDir, fileName)
                oldFile.copyTo(newFile, overwrite = true)
                oldFile.delete()

                "/static/${newImageType.folderName}/$fileName"
            } catch (e: Exception) {
                println("Failed to move image: ${e.message}")
                null
            }
        }
    }
}