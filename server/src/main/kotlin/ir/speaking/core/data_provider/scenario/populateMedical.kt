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

fun populateMedical() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Health & Wellness"
            it[imageUrl] = "category/medical.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Booking a Doctor's Visit"
            it[persianTitle] = "گرفتن وقت دکتر"
            it[description] =
                "You're calling a medical clinic to schedule an appointment for a persistent headache. The receptionist will ask for your personal details and symptoms to find the right doctor and time for you. Be clear and specific."
            it[persianDescription] =
                "برای یک سردرد مداوم با یک کلینیک پزشکی تماس می‌گیری تا وقت ویزیت بگیری. منشی اطلاعات شخصی و علائمت را می‌پرسد تا پزشک و زمان مناسبی برایت پیدا کند. واضح و دقیق باش."
            it[aiRole] = "Clinic Receptionist"
            it[aiName] = "Sophia"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/medical/doctor_appointment.webp"
            it[points] = 16
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Provide your full name and date of birth for identification."
            it[persianDescription] = "نام کامل و تاریخ تولد خود را برای احراز هویت ارائه دهید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Clearly describe your headache, including when it started and how it feels."
            it[persianDescription] = "سردرد خود را به وضوح توصیف کنید، شامل اینکه از کی شروع شده و چه حسی دارد."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Discuss your availability and confirm a suitable appointment time."
            it[persianDescription] = "در مورد زمان‌های آزادتان صحبت کرده و یک وقت ملاقات مناسب را تأیید کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Post-Injury Therapy"
            it[persianTitle] = "تراپی پس از آسیب‌دیدگی"
            it[description] =
                "Following a recent sports injury, you are in a counseling session with your rehabilitation therapist, Linda. The goal is to openly discuss the emotional impact of the injury and work on strategies to rebuild your confidence for a full recovery."
            it[persianDescription] =
                "به دنبال یک آسیب ورزشی اخیر، در یک جلسه مشاوره با درمانگر توانبخشی خود، لیندا، هستی. هدف این است که آشکارا در مورد تأثیر عاطفی این آسیب صحبت کرده و روی استراتژی‌هایی برای بازسازی اعتماد به نفست جهت بهبودی کامل کار کنید."
            it[aiRole] = "Rehab Therapist"
            it[aiName] = "Linda"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/medical/injury_counseling.webp"
            it[points] = 22
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Explain how the injury has affected your daily life and athletic activities."
            it[persianDescription] = "توضیح دهید که این آسیب چگونه بر زندگی روزمره و فعالیت‌های ورزشی شما تأثیر گذاشته است."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Open up about any fears or emotional challenges you're facing during recovery."
            it[persianDescription] = "در مورد هرگونه ترس یا چالش عاطفی که در طول دوره بهبودی با آن روبرو هستید، صحبت کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask for advice on mental strategies to cope with the stress of recovery."
            it[persianDescription] = "برای راهکارهای ذهنی جهت مقابله با استرس بهبودی، درخواست راهنمایی کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Consulting a Pharmacist"
            it[persianTitle] = "مشاوره با داروساز"
            it[description] =
                "You're at the pharmacy seeking advice for a sore throat. The pharmacist will ask about your symptoms to recommend a suitable over-the-counter remedy, explaining its proper use and any precautions."
            it[persianDescription] =
                "برای گرفتن مشاوره در مورد گلودرد به داروخانه رفته‌اید. داروساز در مورد علائمت سؤال می‌کند تا یک داروی بدون نسخه مناسب را به تو پیشنهاد دهد و نحوه استفاده صحیح و اقدامات احتیاطی آن را توضیح دهد."
            it[aiRole] = "Pharmacist"
            it[aiName] = "Nora"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/medical/pharmacist_consultation.webp"
            it[points] = 18
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Describe your sore throat symptoms and how long you've had them."
            it[persianDescription] = "علائم گلودرد و مدت زمانی که این علائم را داشته‌اید، توصیف کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Ask the pharmacist about any potential side effects of the recommended medicine."
            it[persianDescription] = "در مورد عوارض جانبی احتمالی داروی توصیه‌شده از داروساز سؤال کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Confirm the correct dosage and how often you should take the medication."
            it[persianDescription] = "دوز صحیح و تعداد دفعات مصرف دارو را تأیید کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Your Annual Check-up"
            it[persianTitle] = "چکاپ سالانه شما"
            it[description] =
                "It's time for your yearly check-up with your family doctor. Dr. Miller will ask about your overall health, including lifestyle habits like diet, exercise, and sleep, to ensure you're on the right track."
            it[persianDescription] =
                "زمان چکاپ سالانه با پزشک خانواده‌ات فرا رسیده. دکتر میلر در مورد سلامت عمومی تو، از جمله عادات سبک زندگی مانند رژیم غذایی، ورزش و خواب، سؤال خواهد کرد تا مطمئن شود در مسیر درستی قرار داری."
            it[aiRole] = "Family Doctor"
            it[aiName] = "Dr. Miller"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/medical/annual_check_up.webp"
            it[points] = 15
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Discuss your typical diet and weekly exercise routine with the doctor."
            it[persianDescription] = "در مورد رژیم غذایی معمول و برنامه ورزشی هفتگی خود با دکتر صحبت کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Describe your sleep patterns and discuss your current stress levels or mental state."
            it[persianDescription] = "الگوهای خواب خود را توصیف کرده و در مورد سطح استرس فعلی یا وضعیت روحی خود صحبت کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Ask a question about a minor health concern or about preventative care."
            it[persianDescription] = "یک سؤال در مورد یک نگرانی جزئی سلامتی یا در مورد مراقبت‌های پیشگیرانه بپرسید."
            it[createdAt] = LocalDateTime.now()
        }
    }
}