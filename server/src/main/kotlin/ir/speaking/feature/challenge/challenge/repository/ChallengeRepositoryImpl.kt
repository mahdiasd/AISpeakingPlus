package ir.speaking.feature.challenge.challenge.repository

import ir.speaking.core.utils.now
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.core.utils.toLocalDate
import ir.speaking.core.utils.toUUID
import ir.speaking.feature.challenge.challenge.db.ChallengeDAO
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.challenge.db.toModel
import ir.speaking.feature.challenge.challenge.dto.SaveChallengeRequest
import ir.speaking.feature.challenge.challenge.model.Challenge
import ir.speaking.feature.challenge.task.db.ChallengeTaskDAO
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.challenge.task.db.toModel
import ir.speaking.feature.chat.model.toRole
import ir.speaking.feature.user.model.toGender
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.koin.core.annotation.Single
import java.util.*

@Single
class ChallengeRepositoryImpl() : ChallengeRepository {

    override suspend fun readLastActive(): Challenge? =
        suspendTransaction {
            val today = java.time.LocalDate.now().toKotlinLocalDate()
            val challengeDao = ChallengeDAO.find {
                (ChallengeTable.startDate lessEq today) and (ChallengeTable.endDate greaterEq today)
            }.orderBy(ChallengeTable.startDate to SortOrder.DESC)
                .orderBy(ChallengeTable.createdAt to SortOrder.DESC)
                .limit(1)
                .firstOrNull() ?: return@suspendTransaction null

            val tasks = ChallengeTaskDAO.find {
                ChallengeTaskTable.challengeId eq challengeDao.id.value
            }.map { it.toModel() }

            challengeDao.toModel().copy(tasks = tasks)
        }

    override suspend fun readLast(): Challenge? =
        suspendTransaction {
            val challengeDao = ChallengeDAO.all()
                .orderBy(ChallengeTable.createdAt to SortOrder.DESC)
                .limit(1)
                .firstOrNull() ?: return@suspendTransaction null

            val tasks = ChallengeTaskDAO.find {
                ChallengeTaskTable.challengeId eq challengeDao.id.value
            }.map { it.toModel() }

            challengeDao.toModel().copy(tasks = tasks)
        }

    override suspend fun get(id: String): Challenge? = suspendTransaction {
        val challengeDao = ChallengeDAO.findById(id.toUUID()) ?: return@suspendTransaction null

        val tasks = ChallengeTaskDAO.find {
            ChallengeTaskTable.challengeId eq id.toUUID()
        }.map { it.toModel() }

        challengeDao.toModel().copy(tasks = tasks)
    }

    override suspend fun create(saveChallengeRequest: SaveChallengeRequest): Challenge = suspendTransaction {
        val challenge = ChallengeDAO.new {
            title = saveChallengeRequest.title
            persianTitle = saveChallengeRequest.persianTitle
            description = saveChallengeRequest.description
            persianDescription = saveChallengeRequest.persianDescription
            imageUrl = saveChallengeRequest.imageUrl
            aiName = saveChallengeRequest.aiName
            aiAvatar = saveChallengeRequest.aiAvatar
            points = saveChallengeRequest.points
            gender = saveChallengeRequest.gender.toGender()
            aiRole = saveChallengeRequest.role
            starter = saveChallengeRequest.starter.toRole()
            startDate = saveChallengeRequest.startDate.toLocalDate()
            endDate = saveChallengeRequest.endDate.toLocalDate()
            createdAt = LocalDateTime.now()
        }

        saveChallengeRequest.tasks.forEach {
            ChallengeTaskDAO.new {
                challengeId = challenge.id.value
                description = it.description
                persianDescription = it.persianDescription
                createdAt = LocalDateTime.now()
            }.toModel()
        }
        challenge.toModel()
    }

    override suspend fun update(saveChallengeRequest: SaveChallengeRequest): Challenge? = suspendTransaction {
        ChallengeDAO.findByIdAndUpdate(saveChallengeRequest.uid!!.toUUID(), block = {
            it.title = saveChallengeRequest.title
            it.description = saveChallengeRequest.description
            it.persianTitle = saveChallengeRequest.persianTitle
            it.persianDescription = saveChallengeRequest.persianDescription
            it.imageUrl = saveChallengeRequest.imageUrl
            it.aiName = saveChallengeRequest.aiName
            it.aiRole = saveChallengeRequest.role
            it.gender = saveChallengeRequest.gender.toGender()
            it.aiAvatar = saveChallengeRequest.aiAvatar
            it.starter = saveChallengeRequest.starter.toRole()
            it.points = saveChallengeRequest.points
            it.startDate = saveChallengeRequest.startDate.toLocalDate()
            it.endDate = saveChallengeRequest.endDate.toLocalDate()
        })?.toModel()
    }

    override suspend fun delete(id: UUID): Boolean = suspendTransaction {
        ChallengeDAO.findById(id)?.let {
            it.delete()
            true
        } ?: false
    }
}
