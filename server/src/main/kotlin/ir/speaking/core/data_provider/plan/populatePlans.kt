package ir.speaking.core.data_provider.plan

import ir.speaking.feature.plan.db.PlanTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun populatePlans() {
    transaction {
        PlanTable.insert {
            it[title] = "اشتراک 3 روزه هدیه"
            it[name] = "هدیه"
            it[description] =
                "دسترسی به تمامی امکانات برنامه، شامل گفتگو با هوش مصنوعی در سناریوها، چلنج ها و شرکت در مسابقه ی کلمات."
            it[price] = "0"
            it[discountedPrice] = ""
            it[cafeBazaarId] = ""
            it[dayDuration] = 1
            it[visibility] = false
            it[createdAt] = Clock.System.now()
        }

        PlanTable.insert {
            it[title] = "اشتراک یک ماهه"
            it[name] = "برنزی"
            it[description] =
                "دسترسی به تمامی امکانات برنامه، شامل گفتگو با هوش مصنوعی در سناریوها، چلنج ها و شرکت در مسابقه ی کلمات."
            it[price] = "45000"
            it[discountedPrice] = ""
            it[cafeBazaarId] = "30_day"
            it[visibility] = true
            it[dayDuration] = 31
            it[createdAt] = Clock.System.now()
        }

        PlanTable.insert {
            it[title] = "اشتراک سه ماهه"
            it[name] = "نقره ای"
            it[description] = "دسترسی به تمامی لغات و امکانات برنامه به مدت سه ماه با ۱۵٪ تخفیف ویژه"
            it[price] = "135000"
            it[discountedPrice] = ""
            it[visibility] = true
            it[cafeBazaarId] = "90_day"
            it[dayDuration] = 31 * 3
            it[createdAt] = Clock.System.now()
        }

        PlanTable.insert {
            it[title] = "اشتراک شش ماهه"
            it[name] = "طلایی"
            it[description] = "دسترسی به تمامی لغات و امکانات برنامه به مدت شش ماه با ۲۵٪ تخفیف ویژه"
            it[price] = "270000"
            it[visibility] = true
            it[discountedPrice] = ""
            it[cafeBazaarId] = "180_day"
            it[dayDuration] = 31 * 6
            it[createdAt] = Clock.System.now()
        }
    }
}