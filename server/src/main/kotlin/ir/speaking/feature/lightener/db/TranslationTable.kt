package ir.speaking.feature.lightener.db

import ir.speaking.feature.lightener.model.AlternativeTranslation
import ir.speaking.feature.lightener.model.Translation
import ir.speaking.feature.user.db.UserTable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ColumnType
import java.util.*

object TranslationTable : UUIDTable("translation") {
    val sourceText = text("text")
    val translatedText = text("translated_text")
    val userId = uuid("user_id").references(UserTable.id)
    val alternatives = registerColumn("alternatives", AlternativesColumnType()).nullable()
    val example = text("example").nullable()
}

class AlternativesColumnType : ColumnType<Map<String, List<String>>>() {
    override fun sqlType(): String = "TEXT"

    override fun valueFromDB(value: Any): Map<String, List<String>> = when (value) {
        is String -> Json.decodeFromString<Map<String, List<String>>>(value)
        else -> emptyMap()
    }

    override fun valueToDB(value: Map<String, List<String>>?): Any? = when (value) {
        is Map<*, *> -> Json.encodeToString(value)
        else -> null
    }
}

class TranslationDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TranslationDAO>(TranslationTable)

    var sourceText by TranslationTable.sourceText
    var translatedText by TranslationTable.translatedText
    var alternatives by TranslationTable.alternatives
    var userId by TranslationTable.userId
    var example by TranslationTable.example
}


fun TranslationDAO.toModel() =
    Translation(
        uid = id.value,
        userId = userId,
        sourceText = sourceText,
        translatedText = translatedText,
        alternatives = alternatives?.toAlternatives(),
        example = example
    )

fun (Map<String, List<String>>).toAlternatives(): List<AlternativeTranslation> {
    val list = mutableListOf<AlternativeTranslation>()
    this.forEach {
        list.add(AlternativeTranslation(type = it.key, translations = it.value))
    }
    return list
}