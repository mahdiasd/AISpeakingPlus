package ir.aispeaking.utils

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object MyUtils {
    @OptIn(ExperimentalUuidApi::class)
    fun generateStringUUID(): String {
        return Uuid.random().toString()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun generateUUID(): Uuid {
        return Uuid.random()
    }

}
