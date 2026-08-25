package ir.speaking.feature.lightener.repository

import ir.speaking.core.exeptions.AppException
import ir.speaking.core.response.PagedList
import ir.speaking.core.response.PagingMeta
import ir.speaking.core.utils.toUUIDOrNull
import ir.speaking.feature.lightener.db.TranslationDAO
import ir.speaking.feature.lightener.db.TranslationTable
import ir.speaking.feature.lightener.db.toAlternatives
import ir.speaking.feature.lightener.dto.UpsertTranslationRequest
import ir.speaking.feature.lightener.model.AlternativeTranslation
import ir.speaking.feature.lightener.model.Translation
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class TranslationRepositoryImpl : TranslationRepository {

    private fun TranslationDAO.toModel(): Translation =
        Translation(
            uid = id.value,
            userId = userId, // Assuming userId is directly a UUID in DAO
            sourceText = sourceText,
            translatedText = translatedText,
            alternatives = alternatives?.toAlternatives(), // Use the conversion helper
            example = example
        )

    private fun List<AlternativeTranslation>?.fromAlternatives(): Map<String, List<String>>? {
        if (this.isNullOrEmpty()) return null
        // Simple association, assumes unique types in the list
        return this.associate { it.type to it.translations }
    }

    override suspend fun create(userId: UUID, request: UpsertTranslationRequest): Translation =
        newSuspendedTransaction {
            TranslationDAO.new {
                this.userId = userId
                this.sourceText = request.sourceText
                this.translatedText = request.translatedText
                this.alternatives = request.alternatives.fromAlternatives()
                this.example = request.example
            }.toModel()
        }

    override suspend fun update(userId: UUID, request: UpsertTranslationRequest): Translation? {
        val targetUid = request.uid?.toUUIDOrNull()
            ?: throw AppException.BadRequest("Invalid or missing 'uid' for update.") // Use safe parsing and throw specific error

        return newSuspendedTransaction {
            // Find the DAO first to ensure it exists and potentially check ownership (optional)
            val dao = TranslationDAO.findById(targetUid)

            // Optional: Check if the user owns this item before updating
            if (dao == null || dao.userId != userId) {
                return@newSuspendedTransaction null // Or throw specific NotFound/Forbidden exception
                // throw AppException.NotFound("Lightener with id $targetUid not found or access denied.")
            }

            // Update the found DAO
            dao.apply {
                // Generally, userId should not be updated. If needed, uncomment below.
                // this.userId = userId // Usually fixed, but update if business logic allows

                this.sourceText = request.sourceText
                this.translatedText = request.translatedText
                this.alternatives = request.alternatives.fromAlternatives() // Convert from Request list to Map
                this.example = request.example
                // If you add `updatedAt`:
                // this.updatedAt = Clock.System.now()
            }

            // Flush changes explicitly if needed (often automatic within transaction)
            // flush() // Usually not required unless accessing updated state immediately within the same transaction block in complex scenarios

            dao.toModel() // Convert the updated DAO to the Model
        }
    }


    override suspend fun getPagedTranslation(
        userId: UUID,
        page: Int,
        searchText: String?,
        pageSize: Int
    ): PagedList<Translation> = newSuspendedTransaction {
        val query = TranslationTable.selectAll().where { TranslationTable.userId eq userId }
        val finalQuery = if (!searchText.isNullOrBlank()) {
            val pattern = "%${searchText.lowercase()}%"

            query.adjustWhere {
                (TranslationTable.sourceText.lowerCase() like pattern) or
                        (TranslationTable.translatedText.lowerCase() like pattern)
            }
        } else {
            query
        }

        val totalItems = finalQuery.count()

        val items = TranslationDAO.wrapRows( // Use DAO wrapper if you have one, otherwise map directly
            finalQuery
                .orderBy(TranslationTable.id to SortOrder.DESC) // Or other relevant column like createdAt
                .limit(pageSize).offset(start = ((page - 1) * pageSize).toLong())
        )
            .map { it.toModel() }

        PagedList(
            items = items,
            pagingMeta = PagingMeta(
                totalPages = if (pageSize > 0) ((totalItems + pageSize - 1) / pageSize).toInt() else 0,
                totalItems = totalItems,
                page = page,
            )
        )
    }

    override suspend fun deleteTranslation(userId: UUID, id: UUID): Boolean =
        newSuspendedTransaction {
            // Delete only if the ID matches AND the userId matches (ownership check)
            TranslationTable.deleteWhere {
                (TranslationTable.id eq id) and (TranslationTable.userId eq userId)
            } > 0 // Returns true if one or more rows were deleted
        }

    override suspend fun getBySourceText(sourceText: String, userId: UUID): Translation? {
        return newSuspendedTransaction {
            TranslationDAO.find { (TranslationTable.sourceText eq sourceText) and (TranslationTable.userId eq userId)}
                .limit(1)
                .firstOrNull()
                ?.toModel()
        }
    }
}