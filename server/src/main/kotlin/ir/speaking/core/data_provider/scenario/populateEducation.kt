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

fun populateEducationAndStudy() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Academic Life"
            it[imageUrl] = "category/education.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Full Course Inquiry"
            it[persianTitle] = "پیگیری کلاس تکمیل ظرفیت"
            it[description] = "The course you need to take this semester is already full. You're now meeting with the department manager, Dr. Peterson, to see if there's any way to get a spot or find a suitable alternative. Be polite and persuasive."
            it[persianDescription] = "درسی که این ترم باید برداری، تکمیل ظرفیت شده. حالا با مدیر گروه، دکتر پترسون، جلسه داری تا ببینی راهی برای گرفتن یک جای خالی در کلاس هست یا نه. مؤدب و متقاعدکننده باش."
            it[aiRole] = "Department Manager"
            it[aiName] = "Dr. Peterson"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/education/course_enrollment.webp"
            it[points] = 16
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Politely greet Dr. Peterson and clearly state the full course you're trying to enroll in."
            it[persianDescription] = "محترمانه به دکتر پترسون سلام کن و به وضوح بگو برای کدام درس تکمیل ظرفیت شده تلاش می‌کنی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Explain why taking this specific course is crucial for your academic plan."
            it[persianDescription] = "توضیح بده که چرا برداشتن این درس برای برنامه تحصیلی تو ضروری است."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Inquire about the possibility of being added to a waitlist or if any exceptions can be made."
            it[persianDescription] = "در مورد امکان اضافه شدن به لیست انتظار یا در نظر گرفتن استثنا سوال کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Discussing a Grade"
            it[persianTitle] = "صحبت در مورد نمره"
            it[description] = "You were surprised by a lower-than-expected grade on a recent exam and believe there might be a misunderstanding. You've scheduled a meeting with your professor, Ms. Collins, to discuss your grade respectfully and understand the feedback."
            it[persianDescription] = "از نمره‌ای که در امتحان اخیرت گرفتی و پایین‌تر از حد انتظارت بوده، غافلگیر شدی و فکر می‌کنی سوءتفاهمی پیش آمده. با استادت، خانم کالینز، جلسه‌ای گذاشتی تا محترمانه در مورد نمره‌ات صحبت کنی و بازخورد او را بهتر بفهمی."
            it[aiRole] = "Professor"
            it[aiName] = "Ms. Collins"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/education/grade_protest.webp"
            it[points] = 20
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Start the conversation by respectfully expressing your concern about your exam grade."
            it[persianDescription] = "گفتگو را با ابراز محترمانه نگرانی‌ات در مورد نمره امتحانت شروع کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Refer to a specific question on the exam and briefly explain your reasoning for the answer you gave."
            it[persianDescription] = "به یک سوال مشخص در برگه امتحانی اشاره کن و استدلالت برای پاسخی که دادی را به طور خلاصه شرح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Politely ask if she would be willing to review that specific section of your exam again."
            it[persianDescription] = "مؤدبانه بپرس آیا امکانش هست که آن بخش مشخص از امتحان تو را مجدداً بازبینی کند."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Inquire about the next steps and when you can expect an update."
            it[persianDescription] = "در مورد مراحل بعدی و اینکه چه زمانی می‌توانی منتظر پاسخ باشی، سوال کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Conversation Practice"
            it[persianTitle] = "تمرین مکالمه"
            it[description] = "It's time for your one-on-one English lesson with your teacher, Jason. He's ready to chat about a variety of interesting topics to help you improve your fluency. Relax, express your ideas, and have a great conversation!"
            it[persianDescription] = "وقت کلاس خصوصی انگلیسی با معلمت، جیسون، رسیده. او آماده است تا در مورد موضوعات جالب مختلفی با تو گپ بزند تا به روان‌تر شدن مکالمه‌ات کمک کند. راحت باش، ایده‌هایت را بیان کن و از یک گفتگوی عالی لذت ببر!"
            it[aiRole] = "Language Teacher"
            it[aiName] = "Jason"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/education/english_practice.webp"
            it[points] = 14
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Share your thoughts about your favorite hobby or pastime."
            it[persianDescription] = "نظرت را در مورد سرگرمی یا فعالیت مورد علاقه‌ات به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Describe a memorable trip you've taken and what made it special."
            it[persianDescription] = "یک سفر خاطره‌انگیز که داشتی و چیزی که آن را خاص کرده بود، توصیف کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Talk about a book or movie that recently impressed you."
            it[persianDescription] = "در مورد یک کتاب یا فیلم که اخیراً تو را تحت تأثیر قرار داده صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Discuss your future goals or aspirations."
            it[persianDescription] = "درباره اهداف یا آرزوهای آینده‌ات گفتگو کن."
            it[createdAt] = LocalDateTime.now()
        }
    }
}