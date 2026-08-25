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

fun populateBusinessAndWork() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Business & Work"
            it[imageUrl] = "category/business.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Scheduling a Meeting"
            it[persianTitle] = "تنظیم یک جلسه کاری"
            it[description] =
                "You and your colleague, Sarah, need to sync up on a project. Your task is to call her and schedule a 30-minute online meeting for this week. Be professional and efficient in finding a time that works for you both."
            it[persianDescription] =
                "لازمه که تو و همکارت، سارا، درباره یک پروژه هماهنگ بشید. وظیفه‌ی تو اینه که باهاش تماس بگیری و یک جلسه آنلاین ۳۰ دقیقه‌ای برای همین هفته تنظیم کنی. حرفه‌ای و کارآمد باش تا زمانی رو پیدا کنی که برای هر دوتون مناسب باشه."
            it[aiRole] = "Colleague"
            it[aiName] = "Sarah"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/business/scheduling_business_meeting.webp"
            it[points] = 15
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Initiate the call and state your purpose: to schedule a project sync-up."
            it[persianDescription] = "تماس را برقرار کن و هدفت را واضح بیان کن: تنظیم یک جلسه برای هماهنگی پروژه."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Propose two specific time slots and ask for her availability."
            it[persianDescription] = "دو بازه زمانی مشخص پیشنهاد بده و در مورد زمان آزاد او سوال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Be flexible. If she's unavailable, negotiate a new time and confirm the final details."
            it[persianDescription] = "انعطاف‌پذیر باش. اگر زمان‌های پیشنهادی مناسب نبود، برای یک زمان جدید مذاکره و جزئیات نهایی را تأیید کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Requesting an Extension"
            it[persianTitle] = "درخواست تمدید مهلت"
            it[description] =
                "You're facing a tight deadline for a report due this Friday. Due to unexpected workload, you need more time. Call your supervisor, Ms. Allen, to professionally request an extension. Be clear, respectful, and prepared to suggest a new deadline."
            it[persianDescription] =
                "برای گزارشی که باید این جمعه تحویل بدی، با محدودیت زمانی مواجهی. به خاطر حجم کاری پیش‌بینی‌نشده، به زمان بیشتری احتیاج داری. با مدیرت، خانم آلن، تماس بگیر تا به صورت حرفه‌ای درخواست تمدید مهلت کنی. شفاف، محترمانه و آماده باش تا مهلت جدیدی پیشنهاد دهی."
            it[aiRole] = "Supervisor"
            it[aiName] = "Ms. Allen"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/business/requesting_a_deadline_extension.webp"
            it[points] = 18
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "After a polite greeting, clearly state that you'd like to discuss the report deadline."
            it[persianDescription] = "بعد از یک سلام محترمانه، به وضوح بگو که می‌خواهی در مورد مهلت تحویل گزارش صحبت کنی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Briefly explain the reasons for the delay and take responsibility for the situation."
            it[persianDescription] = "دلایل تأخیر را به طور خلاصه توضیح بده و مسئولیت شرایط را بپذیر."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Propose a new, realistic deadline and ask if that would be acceptable."
            it[persianDescription] = "یک مهلت جدید و واقع‌بینانه پیشنهاد بده و بپرس که آیا این زمان قابل قبول است."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Conducting a Review"
            it[persianTitle] = "ارائه بازخورد عملکرد"
            it[description] =
                "As a team leader, it's time to conduct a performance review with your team member, Daniel. Your goal is to provide balanced feedback: highlight his strengths, offer constructive advice for one area of improvement, and motivate him for the future."
            it[persianDescription] =
                "به عنوان مدیر تیم، زمان ارائه بازخورد عملکرد به عضو تیمت، دانیال، فرا رسیده. هدف تو ارائه بازخوردی متعادل است: نقاط قوتش را برجسته کن، یک توصیه سازنده برای بهبود ارائه بده و به او برای آینده انگیزه بده."
            it[aiRole] = "Employee"
            it[aiName] = "Daniel"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/business/giving_a_performance_review_as_a_manager.webp"
            it[points] = 22
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Begin the meeting with a positive tone and clearly state its purpose."
            it[persianDescription] = "جلسه را با لحنی مثبت شروع کن و هدف آن را به وضوح بیان کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Acknowledge his contributions by mentioning two specific achievements or strengths."
            it[persianDescription] = "با اشاره به دو دستاورد یا نقطه قوت مشخص، از مشارکت‌های او قدردانی کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Offer constructive feedback on one area for improvement, along with a helpful suggestion."
            it[persianDescription] = "یک بازخورد سازنده در زمینه‌ای که نیاز به بهبود دارد، همراه با یک پیشنهاد مفید ارائه بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Conclude with encouragement and open the floor for any questions he might have."
            it[persianDescription] = "گفتگو را با یک پیام دلگرم‌کننده به پایان برسان و از او بپرس اگر سوالی دارد."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "The Job Interview"
            it[persianTitle] = "مصاحبه شغلی"
            it[description] =
                "This is it—the job interview for a position you're excited about. You'll be speaking with the hiring manager, James. Your challenge is to answer his questions confidently, clearly, and professionally."
            it[persianDescription] =
                "خودشه! مصاحبه‌ی شغلی برای موقعیتی که بهش علاقه داری. تو با مدیر استخدام، جیمز، صحبت خواهی کرد. چالش تو اینه که به سوالاتش با اعتماد به نفس، شفاف و حرفه‌ای پاسخ بدی."
            it[aiRole] = "Interviewer"
            it[aiName] = "James"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/business/job_interview_preparation_with_a_mentor.webp"
            it[starter] = Role.Model
            it[points] = 20
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Make a strong first impression with a polite greeting and thank him for the opportunity."
            it[persianDescription] = "یک تأثیر اولیه قوی با سلامی مؤدبانه بگذار و برای این فرصت از او تشکر کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Deliver a concise 'Tell me about yourself' by highlighting your professional background."
            it[persianDescription] = "با برجسته کردن سوابق حرفه‌ای خود، یک معرفی مختصر و مفید از خودت ارائه بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] =
                "Showcase your problem-solving skills by describing a challenging project and how you successfully navigated it."
            it[persianDescription] =
                "مهارت حل مسئله خود را با توصیف یک پروژه چالش‌برانگیز و نحوه مدیریت موفق آن به نمایش بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Demonstrate your interest by asking an insightful question about the company culture or future goals."
            it[persianDescription] = "علاقه خود را با پرسیدن یک سوال هوشمندانه درباره فرهنگ شرکت یا اهداف آینده نشان بده."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Closing a Sale"
            it[persianTitle] = "نهایی کردن فروش"
            it[description] =
                "You are a salesperson in a crucial call with a potential client, Emily. Your mission is to highlight the value of your product, address her questions, and confidently guide the conversation toward closing the deal."
            it[persianDescription] =
                "تو به عنوان یک فروشنده در یک تماس حیاتی با مشتری بالقوه، امیلی، هستی. مأموریت تو اینه که ارزش محصولت رو برجسته کنی، به سوالاتش پاسخ بدی و با اعتماد به نفس گفتگو را به سمت نهایی کردن فروش هدایت کنی."
            it[aiRole] = "Potential Client"
            it[aiName] = "Emily"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/business/closing_a_sales_deal.webp"
            it[starter] = Role.User
            it[points] = 24
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Begin the call professionally and briefly introduce the product's core benefit."
            it[persianDescription] = "تماس را حرفه‌ای شروع کن و مزیت اصلی محصول را به طور خلاصه معرفی کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Highlight two key features and explain how they solve a problem for the client."
            it[persianDescription] = "دو ویژگی کلیدی را برجسته کن و توضیح بده که چگونه مشکلی از مشتری حل می‌کنند."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Listen to her questions carefully and confidently address any concerns she might have."
            it[persianDescription] = "به سوالات او با دقت گوش کن و با اعتماد به نفس به هرگونه نگرانی‌اش پاسخ بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Ask for the sale directly and be ready to confirm the next steps."
            it[persianDescription] = "مستقیماً درخواست خرید را مطرح کن و برای تأیید مراحل بعدی آماده باش."
            it[createdAt] = LocalDateTime.now()
        }
    }
}