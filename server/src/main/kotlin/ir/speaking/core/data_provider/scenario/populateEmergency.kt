package ir.speaking.core.data_provider.scenario

import ir.speaking.core.utils.now
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

fun populateEmergency() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Emergency"
            it[imageUrl] = "category/emergency.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Reporting a Fire"
            it[persianTitle] = "گزارش آتش‌سوزی"
            it[description] = "There's an emergency. You smell smoke and see flames coming from another apartment. You must immediately call emergency services. Stay calm and provide clear, accurate information to the fire department operator."
            it[persianDescription] = "یک وضعیت اضطراری پیش آمده. بوی دود را حس می‌کنی و شعله‌های آتش را از آپارتمان دیگری می‌بینی. باید فوراً با خدمات اورژانس تماس بگیری. آرامشت را حفظ کن و اطلاعاتی دقیق و واضح به اپراتور آتش‌نشانی بده."
            it[aiRole] = "Fire Emergency Operator"
            it[aiName] = "Officer Blake"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/emergency/building_fire.webp"
            it[points] = 22
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Immediately state the nature of the emergency: a building fire."
            it[persianDescription] = "فوراً ماهیت وضعیت اضطراری را اعلام کن: آتش‌سوزی در ساختمان."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Provide the complete address, including the floor and apartment number if you know it."
            it[persianDescription] = "آدرس کامل را ارائه بده، شامل طبقه و شماره آپارتمان اگر می‌دانی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Inform the operator if you know whether people are inside the building, especially near the fire."
            it[persianDescription] = "به اپراتور اطلاع بده که آیا می‌دانی افرادی داخل ساختمان، به خصوص نزدیک آتش، هستند یا نه."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Describe the visible size of the flames and the amount of smoke."
            it[persianDescription] = "اندازه قابل مشاهده شعله‌ها و مقدار دود را توصیف کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Calling for an Ambulance"
            it[persianTitle] = "درخواست آمبولانس"
            it[description] = "A friend has suddenly collapsed with severe chest pain. He's conscious but needs urgent medical help. Your task is to call for an ambulance and clearly describe the situation to the medical operator."
            it[persianDescription] = "دوستت به خاطر درد شدید قفسه سینه ناگهان روی زمین افتاده. هوشیار است اما به کمک فوری پزشکی نیاز دارد. وظیفه تو این است که با اورژانس تماس بگیری و وضعیت را به وضوح برای اپراتور پزشکی شرح دهی."
            it[aiRole] = "Medical Emergency Operator"
            it[aiName] = "Jessica"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/emergency/medical_emergency.webp"
            it[points] = 24
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "State clearly that you need an ambulance for a medical emergency."
            it[persianDescription] = "به وضوح بیان کن که برای یک مورد اورژانس پزشکی به آمبولانس نیاز داری."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Describe what happened to your friend and specify that the chest pain started suddenly."
            it[persianDescription] = "شرح بده چه اتفاقی برای دوستت افتاده و مشخص کن که درد قفسه سینه ناگهانی شروع شده است."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Report on his current condition: his breathing, consciousness, and ability to speak."
            it[persianDescription] = "وضعیت فعلی او را گزارش بده: تنفس، سطح هوشیاری و توانایی صحبت کردن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Provide any known medical history, such as his age or pre-existing health conditions."
            it[persianDescription] = "هرگونه سابقه پزشکی که می‌دانی، مانند سن یا بیماری‌های زمینه‌ای او را ارائه بده."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Helping a Lost Child"
            it[persianTitle] = "کمک به کودک گمشده"
            it[description] = "While shopping, you find a young child who is alone, crying, and clearly lost. You've responsibly brought her to the mall security office. Now, you need to explain the situation to the security officer."
            it[persianDescription] = "هنگام خرید، کودک خردسالی را پیدا می‌کنی که تنها، گریان و کاملاً گم‌شده به نظر می‌رسد. تو مسئولانه او را به دفتر حراست مرکز خرید آورده‌ای. حالا باید وضعیت را برای مأمور حراست توضیح دهی."
            it[aiRole] = "Mall Security Officer"
            it[aiName] = "Mr. Harris"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/emergency/lost_child.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Report that you've found a lost child and she is with you now at the security office."
            it[persianDescription] = "گزارش بده که یک کودک گمشده پیدا کرده‌ای و او الان همراه تو در دفتر حراست است."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Describe the exact location in the mall where you found the child."
            it[persianDescription] = "مکان دقیقی که کودک را در مرکز خرید پیدا کردی، شرح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Share any information the child gave you, such as her name or what her parents look like."
            it[persianDescription] = "هر اطلاعاتی که کودک به تو داده، مانند نامش یا مشخصات ظاهری والدینش را به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Witnessing an Accident"
            it[persianTitle] = "گزارش تصادف"
            it[description] = "You have just witnessed a car accident. Your responsibility is to call the police and report it accurately. You'll need to provide critical details about the location, vehicles, and potential injuries."
            it[persianDescription] = "تو همین الان شاهد یک تصادف رانندگی بوده‌ای. مسئولیت تو این است که با پلیس تماس بگیری و آن را به درستی گزارش کنی. باید جزئیات حیاتی در مورد مکان، خودروها و مصدومان احتمالی را ارائه دهی."
            it[aiRole] = "Police Dispatcher"
            it[aiName] = "Officer Kelly"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/emergency/car_accident.webp"
            it[points] = 20
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Clearly state that you are calling to report a car accident you just witnessed."
            it[persianDescription] = "به وضوح اعلام کن که برای گزارش یک تصادف رانندگی که همین الان شاهدش بودی، تماس گرفته‌ای."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Provide the precise location, using cross-streets or prominent landmarks."
            it[persianDescription] = "موقعیت دقیق را با استفاده از تقاطع خیابان‌ها یا مکان‌های شاخص نزدیک، اعلام کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "State the number of vehicles involved and how many people you see."
            it[persianDescription] = "تعداد خودروهای درگیر و تعداد افرادی که می‌بینی را اعلام کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Report any visible injuries, noting if medical assistance seems urgently needed."
            it[persianDescription] = "هرگونه آسیب‌دیدگی قابل مشاهده را گزارش بده و ذکر کن که آیا به نظر می‌رسد کمک‌های پزشکی فوری لازم است."
            it[createdAt] = LocalDateTime.now()
        }

    }
}