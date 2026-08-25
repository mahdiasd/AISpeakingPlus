package ir.speaking.core.data_provider.user

import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun populateUsers() {
    transaction {
        val fixedUsers = listOf(
            Triple("Mohammad", "Rezaei", Gender.Man),
            Triple("Fatemeh", "Karimi", Gender.Woman),
            Triple("Hussein", "Alavi", Gender.Man),
            Triple("Maryam", "Hosseini", Gender.Woman),
            Triple("Reza", "Kazemi", Gender.Man),
            Triple("Narges", "Mousavi", Gender.Woman),
            Triple("Ahmad", "Zarei", Gender.Man),
            Triple("Sara", "Maleki", Gender.Woman),
            Triple("Mahdi", "Asadollahpour", Gender.Man),
            Triple("Reyhaneh", "Golmn", Gender.Woman)
        )

        val avatars = listOf(
            "B-5", "G-11", "B-8", "G-4", "B-5",
            "G-6", "B-7", "G-8", "B-5", "G-11"
        )

        val languageLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        val clockNow = Clock.System.now()

        for (i in fixedUsers.indices) {
            val (firstName, lastName, genderEnum) = fixedUsers[i]
            val nickName = "$firstName $lastName"
            val mobile = "0910000000$i"
            val gender = genderEnum.name
            val age = 20 + i
            val score = 50 + i * 10
            val languageLevel = languageLevels[i % languageLevels.size]
            val active = true
            val avatar = avatars[i]

            UserTable.insert {
                it[this.nickName] = nickName
                it[this.firstName] = firstName
                it[this.lastName] = lastName
                it[this.mobile] = mobile
                it[this.gender] = gender
                it[this.age] = age
                it[this.score] = score
                it[this.languageLevel] = languageLevel
                it[this.active] = active
                it[this.avatar] = avatar
                it[this.createdAt] = clockNow
                it[this.updatedAt] = clockNow
            }
        }
    }
}
