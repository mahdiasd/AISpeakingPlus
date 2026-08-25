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

fun populateSportsAndFitness() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Sports & Fitness"
            it[imageUrl] = "category/sports.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Joining a New Gym"
            it[persianTitle] = "عضویت در باشگاه"
            it[description] = "You're ready to start your fitness journey and have just walked into a new gym. Talk to the staff member, Kevin, to learn about membership plans and find one that matches your personal fitness goals."
            it[persianDescription] = "آماده‌ای تا سفر تندرستی‌ات را شروع کنی و همین الان وارد یک باشگاه جدید شدی. با کارمند باشگاه، کوین، صحبت کن تا در مورد پلن‌های عضویت اطلاعات بگیری و گزینه‌ای را پیدا کنی که با اهداف ورزشی شخصی تو هماهنگ باشد."
            it[aiRole] = "Gym Staff"
            it[aiName] = "Kevin"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/sports/gym_signup.webp"
            it[points] = 14
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Ask about the different membership plans and their costs."
            it[persianDescription] = "در مورد پلن‌های مختلف عضویت و هزینه‌هایشان سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Tell him about your fitness goals (e.g., build muscle, lose weight) and the days you can work out."
            it[persianDescription] = "در مورد اهداف ورزشی‌ات (مثلاً عضله‌سازی، کاهش وزن) و روزهایی که می‌توانی تمرین کنی به او بگو."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Post-Match Analysis"
            it[persianTitle] = "تحلیل بعد از بازی"
            it[description] = "The World Cup final was incredible! You're meeting your friend Reza for coffee, and the energy is still high. It's time to break down the match, from the most thrilling goals to the standout players."
            it[persianDescription] = "فینال جام جهانی فوق‌العاده بود! با دوستت رضا در کافه قرار گذاشتی و هنوز هیجان بازی در اوج است. وقتشه که بازی را تحلیل کنید، از هیجان‌انگیزترین گل‌ها گرفته تا بازیکنان برجسته."
            it[aiRole] = "Football Fan Friend"
            it[aiName] = "Reza"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/sports/world_cup_chat.webp"
            it[points] = 16
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Share your overall opinion of the final match and whether the best team won."
            it[persianDescription] = "نظر کلی خود را در مورد بازی فینال و اینکه آیا بهترین تیم برنده شد، به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Talk about the player who impressed you the most during the game."
            it[persianDescription] = "در مورد بازیکنی که در طول بازی بیشترین تأثیر را روی تو گذاشت صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Describe where you watched the match and how you celebrated the victory."
            it[persianDescription] = "توضیح بده که مسابقه را کجا تماشا کردی و چگونه پیروزی را جشن گرفتی."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Buying Tennis Gear"
            it[persianTitle] = "خرید تجهیزات تنیس"
            it[description] = "You've decided to start playing tennis and need the right gear. You're at a sports store talking to an assistant, Jasmine. Get her expert advice to choose the perfect racket and shoes for a beginner."
            it[persianDescription] = "تصمیم گرفتی تنیس را شروع کنی و به تجهیزات مناسب نیاز داری. در یک فروشگاه ورزشی در حال صحبت با فروشنده، جاسمین، هستی. از مشاوره تخصصی او استفاده کن تا راکت و کفش عالی برای یک مبتدی انتخاب کنی."
            it[aiRole] = "Store Assistant"
            it[aiName] = "Jasmine"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/sports/sports_gear_purchase.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Tell the assistant you're new to tennis and need a racket and shoes."
            it[persianDescription] = "به فروشنده بگو که در تنیس تازه‌کار هستی و به راکت و کفش نیاز داری."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Ask about the difference between rackets for beginners versus advanced players."
            it[persianDescription] = "در مورد تفاوت بین راکت‌های مبتدیان و بازیکنان حرفه‌ای سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Based on her advice, confirm which racket model and shoe size is right for you."
            it[persianDescription] = "بر اساس توصیه او، تأیید کن که کدام مدل راکت و سایز کفش برای تو مناسب است."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "First Yoga Class"
            it[persianTitle] = "اولین کلاس یوگا"
            it[description] = "You're trying yoga for the first time and have arrived a bit early. It's a great chance to chat with the instructor, Samantha, to understand the basics and ensure you have a safe and comfortable first class."
            it[persianDescription] = "می‌خواهی برای اولین بار یوگا را امتحان کنی و کمی زودتر به کلاس رسیدی. این یک فرصت عالی برای گپ زدن با مربی، سامانتا، است تا با اصول اولیه آشنا شوی و از یک کلاس اول ایمن و راحت مطمئن شوی."
            it[aiRole] = "Yoga Instructor"
            it[aiName] = "Samantha"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/sports/yoga_class_intro.webp"
            it[points] = 20
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Introduce yourself as a beginner and ask what you should wear or bring to class."
            it[persianDescription] = "خودت را به عنوان یک مبتدی معرفی کن و بپرس چه لباسی باید بپوشی یا چه چیزی به کلاس بیاوری."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Let her know about any old injuries or physical limitations you have."
            it[persianDescription] = "او را از هرگونه آسیب‌دیدگی قدیمی یا محدودیت فیزیکی که داری، مطلع کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Ask if the class is suitable for someone with absolutely no yoga experience."
            it[persianDescription] = "بپرس آیا این کلاس برای کسی که مطلقاً هیچ تجربه یوگا ندارد، مناسب است."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Planning a Hike"
            it[persianTitle] = "برنامه‌ریزی برای کوه‌پیمایی"
            it[description] = "You've joined a local hiking group and are excited for the next adventure. Talk with the group organizer, Tom, to get all the details for the upcoming trip, from the trail's difficulty to the essential gear you'll need."
            it[persianDescription] = "به یک گروه کوهنوردی محلی پیوسته‌ای و برای ماجراجویی بعدی هیجان‌زده‌ای. با برگزارکننده گروه، تام، صحبت کن تا تمام جزئیات سفر پیش رو را، از سختی مسیر گرفته تا تجهیزات ضروری، به دست آوری."
            it[aiRole] = "Group Organizer"
            it[aiName] = "Tom"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/sports/hiking_trip_plan.webp"
            it[points] = 22
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Ask about the trail's length, estimated time, and difficulty level."
            it[persianDescription] = "در مورد طول مسیر، زمان تخمینی و سطح دشواری آن سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Double-check the essential gear you must bring, like water, snacks, and specific clothing."
            it[persianDescription] = "تجهیزات ضروری که باید بیاوری، مانند آب، خوراکی و لباس‌های خاص را دوباره بررسی کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Describe your current fitness level and ask if the hike is suitable for you."
            it[persianDescription] = "سطح آمادگی بدنی فعلی خود را توصیف کن و بپرس آیا این کوه‌پیمایی برای تو مناسب است."
            it[createdAt] = LocalDateTime.now()
        }
    }
}