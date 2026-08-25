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

fun populateTechnologyAndInnovation() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Tech & Innovation"
            it[imageUrl] = "category/technology.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "New Phone Hype"
            it[persianTitle] = "بحث داغ گوشی جدید"
            it[description] = "You and your tech-enthusiast friend are buzzing about the latest smartphone. It's time to dive in and chat about all the cool new features, from the camera to the battery life, and decide if it's really worth the price."
            it[persianDescription] = "تو و دوست عشق تکنولوژی‌ات، نیما، حسابی برای جدیدترین گوشی هوشمند هیجان‌زده‌اید. وقتشه که شیرجه بزنید تو جزئیات و در مورد همه ویژگی‌های باحالش، از دوربین گرفته تا عمر باتری، گپ بزنید و ببینید واقعاً به قیمتش می‌ارزه یا نه."
            it[aiRole] = "Tech-savvy Friend"
            it[aiName] = "Nima"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/technology/latest_phone_chat.webp"
            it[points] = 14
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Share what you first thought when you saw the new phone."
            it[persianDescription] = "اولین چیزی که با دیدن گوشی جدیده به ذهنت رسید رو بهش بگو."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Compare it to the phone you have now. Is it a big jump?"
            it[persianDescription] = "با گوشی که الان داری مقایسه‌اش کن. به نظرت خیلی فرق کرده؟"
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Talk about whether you think it’s worth upgrading to."
            it[persianDescription] = "در مورد اینکه به نظرت ارزش داره به این گوشی جدید آپگرید کنی یا نه، صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Starting to Code"
            it[persianTitle] = "شروع کدنویسی"
            it[description] = "So, you want to learn to code? Awesome! You're meeting up with your developer friend, Sara, for some much-needed advice. Chat with her about where to start, what language to pick, and the best resources for a total beginner."
            it[persianDescription] = "پس بالاخره می‌خوای کدنویسی یاد بگیری؟ عالیه! با دوست برنامه‌نویس‌ات، سارا، قرار گذاشتی تا چندتا راهنمایی درست و حسابی بگیری. باهاش در مورد اینکه از کجا شروع کنی، چه زبانی رو انتخاب کنی و بهترین منابع برای یک مبتدی واقعی چیه، گپ بزن."
            it[aiRole] = "Experienced Developer Friend"
            it[aiName] = "Sara"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/technology/coding_beginner_guidance.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask her what programming language she thinks is best for beginners."
            it[persianDescription] = "ازش بپرس به نظرش چه زبان برنامه‌نویسی برای مبتدی‌ها از همه بهتره."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Tell her what you dream of building, like cool websites or your own app."
            it[persianDescription] = "بهش بگو که رؤیای ساختن چه چیزی رو داری، مثلاً یک وب‌سایت باحال یا اپلیکیشن خودت."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask about any free websites or courses she recommends for learning."
            it[persianDescription] = "در مورد وب‌سایت‌ها یا دوره‌های رایگانی که برای یادگیری پیشنهاد می‌ده، ازش بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Staying Safe Online"
            it[persianTitle] = "امنیت آنلاین"
            it[description] = "After a recent phishing scare at work, you're chatting with your tech-savvy colleague, Mona. It’s a good time to get some practical tips on how to create better passwords and spot those tricky scam emails."
            it[persianDescription] = "بعد از یک ترس و لرز اخیر از فیشینگ در شرکت، داری با همکار وارَد به تکنولوژی‌ات، مونا، گپ می‌زنی. الان وقت خوبیه که چندتا نکته کاربردی در مورد ساختن پسوردهای بهتر و تشخیص اون ایمیل‌های کلاهبرداری موذی بگیری."
            it[aiRole] = "IT-Savvy Colleague"
            it[aiName] = "Mona"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/technology/cybersecurity_work_discussion.webp"
            it[points] = 20
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Bring up the recent phishing email that went around the office."
            it[persianDescription] = "بحث رو با اشاره به اون ایمیل فیشینگ که اخیراً تو شرکت پخش شد، شروع کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Ask her for some simple tips to create stronger passwords."
            it[persianDescription] = "ازش چندتا راهکار ساده برای ساختن پسوردهای قوی‌تر بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Talk about how to tell if an email or a link looks suspicious."
            it[persianDescription] = "در مورد اینکه چطور بفهمیم یک ایمیل یا لینک مشکوکه، صحبت کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Picking a Laptop for Video Editing"
            it[persianTitle] = "انتخاب لپ‌تاپ برای تدوین ویدیو"
            it[description] = "You’re on the hunt for a powerful laptop for video editing and you're at an electronics store. Chat with the expert, Daniel, about what you need, your budget, and what specs like RAM and processors really matter."
            it[persianDescription] = "داری دنبال یک لپ‌تاپ قوی برای تدوین ویدیو می‌گردی و الان تو یک فروشگاه لوازم الکترونیکی هستی. با کارشناس فروشگاه، دانیال، در مورد نیازهایت، بودجه‌ات و اینکه چه مشخصاتی مثل رم و پردازنده واقعاً مهمه، صحبت کن."
            it[aiRole] = "Store Tech Expert"
            it[aiName] = "Daniel"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/technology/laptop_editing_advice.webp"
            it[points] = 17
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Explain the kind of video editing you do and the software you use."
            it[persianDescription] = "توضیح بده که چه نوع تدوین ویدیویی انجام می‌دی و از چه نرم‌افزارهایی استفاده می‌کنی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Ask what the most important specs are for smooth video editing."
            it[persianDescription] = "بپرس که مهم‌ترین مشخصات برای یک تدوین ویدیوی روان چیه."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Point out two models you're considering and ask for his recommendation."
            it[persianDescription] = "به دو مدلی که در نظر داری اشاره کن و ازش بخواه راهنماییت کنه."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Smart Device Help"
            it[persianTitle] = "کمک برای دستگاه هوشمند"
            it[description] = "You bought a new smart thermostat, but getting it set up is proving to be a headache. You've called tech support for help. Explain the problem to the agent, Alex, and get your new gadget working."
            it[persianDescription] = "یک ترموستات هوشمند جدید خریدی اما راه‌اندازیش حسابی دردسر شده. برای کمک با پشتیبانی فنی تماس گرفتی. مشکل رو برای کارشناس، الکس، توضیح بده تا بالاخره این گجت جدیدت کار بیفته."
            it[aiRole] = "Tech Support Agent"
            it[aiName] = "Alex"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/technology/smart_home_setup.webp"
            it[points] = 16
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Tell him exactly what's going wrong with the setup."
            it[persianDescription] = "دقیقاً بهش بگو کجای کار راه‌اندازی مشکل داره."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Ask how to get the device connected to your Wi-Fi network."
            it[persianDescription] = "بپرس که چطور دستگاه رو به شبکه وای‌فای خونه وصل کنی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Ask if it works with voice assistants like Alexa or Google Assistant."
            it[persianDescription] = "بپرس که آیا با دستیارهای صوتی مثل الکسا یا دستیار گوگل کار می‌کنه."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario6Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "The Future of AI"
            it[persianTitle] = "آینده هوش مصنوعی"
            it[description] = "You're at a tech meetup and start a fascinating chat with an AI researcher, Leila. Dive into the big questions: What does AI mean for our privacy, our jobs, and what rules should we have for it in the future?"
            it[persianDescription] = "در یک دورهمی تکنولوژی هستی و یک گفتگوی جذاب را با لیلا، یک پژوهشگر هوش مصنوعی، شروع می‌کنی. شیرجه بزن تو سوالات بزرگ: هوش مصنوعی چه معنایی برای حریم خصوصی و شغل‌های ما داره و در آینده چه قوانینی باید براش بذاریم؟"
            it[aiRole] = "AI Ethics Researcher"
            it[aiName] = "Leila"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/technology/ai_ethics_debate.webp"
            it[points] = 24
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Share your biggest concern about AI and personal privacy."
            it[persianDescription] = "بزرگ‌ترین نگرانیت رو در مورد هوش مصنوعی و حریم خصوصی به اشتراک بذار."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Talk about how you think AI might change the job market."
            it[persianDescription] = "در مورد اینکه فکر می‌کни هوش مصنوعی چطور ممکنه بازار کار رو تغییر بده، صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Ask her what kind of rules or regulations she thinks are needed for AI."
            it[persianDescription] = "ازش بپرس به نظرش چه نوع قوانین و مقرراتی برای هوش مصنوعی لازمه."
            it[createdAt] = LocalDateTime.now()
        }
    }
}