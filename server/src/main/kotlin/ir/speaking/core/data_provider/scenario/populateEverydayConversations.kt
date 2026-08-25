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


fun populateEverydayConversations() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Everyday Conversations"
            it[imageUrl] = "category/everyday_conversations.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Coffee Run"
            it[persianTitle] = "قهوه گرفتن"
            it[description] =
                "The aroma of fresh coffee fills the air! Step up to the counter and chat with the barista to order your perfect cup, just the way you like it."
            it[persianDescription] =
                "عطر قهوه‌ی تازه فضا رو پر کرده! برو جلوی پیشخوان و با باریستا گپ بزن تا نوشیدنی دلخواهت رو، همون‌جوری که دوست داری، سفارش بدی."
            it[aiRole] = "Barista"
            it[aiName] = "Emily"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/everyday_conversations/ordering_coffee_at_a_cafe.webp"
            it[points] = 12
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Curious about the menu? Ask the barista about the different coffee options and their ingredients."
            it[persianDescription] = "کنجکاوی در مورد منو؟ از باریستا در مورد انواع قهوه‌ها و محتویاتشون بپرس."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Time to order! Specify how you'd like your drink, like the size, type of milk, or if you want sugar."
            it[persianDescription] = "وقت سفارش دادنه! نوشیدنیت رو با جزئیاتی مثل اندازه، نوع شیر یا شکر دلخواهت سفارش بده."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Ready to pay? Ask for the total and complete your purchase."
            it[persianDescription] = "آماده پرداختی؟ مبلغ نهایی رو بپرس و سفارشت رو تکمیل کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Hallway Chat"
            it[persianTitle] = "گپ و گفت با همسایه"
            it[description] =
                "You've just run into your friendly neighbor, Jake, in the hallway. Seize this chance to have a quick and friendly chat!"
            it[persianDescription] =
                "همین الان تو راهرو به همسایه‌ی خوش‌برخوردت، جیک، برخوردی. از فرصت استفاده کن و یک گپ‌وگفت دوستانه و سریع باهاش داشته باش!"
            it[aiRole] = "Friendly Neighbor"
            it[aiName] = "Jake"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/everyday_conversations/small_talk_with_a_neighbor.webp"
            it[points] = 13
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Break the ice! Say hello to your neighbor and ask how they're doing."
            it[persianDescription] = "یخ صحبت رو باز کن! به همسایه‌ات سلام کن و حالش رو بپرس."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "A classic topic! Chat about the weather and how it's shaping your day."
            it[persianDescription] = "یک موضوع کلاسیک! درباره آب‌وهوا و تأثیری که روی روزت گذاشته صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "What's happening this weekend? Ask about their plans and tell them about yours."
            it[persianDescription] = "آخر هفته چه خبره؟ از برنامه‌هاش بپرس و برنامه‌های خودت رو هم بهش بگو."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Store Return"
            it[persianTitle] = "مرجوع کردن کالا"
            it[description] =
                "Oh no, the item you just bought is faulty. Head to customer service and explain the situation to get it sorted out."
            it[persianDescription] =
                "ای وای، کالایی که خریدی خرابه. به بخش خدمات مشتریان برو و مشکل رو توضیح بده تا برات حلش کنن."
            it[aiRole] = "Customer Service Representative"
            it[aiName] = "Sophia"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/everyday_conversations/returning_an_item_to_a_store.webp"
            it[points] = 16
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Explain the problem. Clearly describe what's wrong with the item and why it isn't working."
            it[persianDescription] = "مشکل رو توضیح بده. واضح بگو که کالا چه ایرادی داره و چرا کار نمی‌کنه."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Get the details. Inquire about the store's return policy and the expected timeline for a resolution."
            it[persianDescription] = "جزئیات رو بپرس. در مورد سیاست مرجوعی فروشگاه و زمان مورد انتظار برای حل مشکل سوال کن."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Decide on a solution. State whether you'd prefer a refund or an exchange and ask when to expect it."
            it[persianDescription] = "راه حل رو انتخاب کن. بگو که بازپرداخت وجه رو ترجیح میدی یا تعویض کالا و بپرس که چه زمانی انجام می‌شه."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "At the Fruit Stand"
            it[persianTitle] = "میوه‌فروشی"
            it[description] = "Time to buy some healthy snacks! You're at a local fruit stand. Talk to the seller to pick out the freshest fruits."
            it[persianDescription] = "وقت خریدن خوراکی‌های سالمه! تو یک میوه‌فروشی محلی هستی. با فروشنده صحبت کن تا تازه‌ترین میوه‌ها رو انتخاب کنی."
            it[aiRole] = "Fruit Seller"
            it[aiName] = "Anna"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/everyday_conversations/fruit_shopping.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Start the conversation by greeting the seller and asking about today's selection of fruits."
            it[persianDescription] = "با سلام کردن به فروشنده، مکالمه رو شروع کن و در مورد میوه‌های امروزشون بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Check the quality. Ask if the apples are fresh and inquire about their origin."
            it[persianDescription] = "کیفیت رو بررسی کن. بپرس که آیا سیب‌ها تازه هستن و محصول کجا هستن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Curious about the cost? Ask for the price of bananas per kilogram."
            it[persianDescription] = "کنجکاوی در مورد قیمت؟ قیمت هر کیلو موز رو بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Place your order for a certain amount of oranges and grapes."
            it[persianDescription] = "مقدار مشخصی پرتقال و انگور سفارش بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Wrap it up by confirming the total price and paying for your selection."
            it[persianDescription] = "با تأیید قیمت نهایی و پرداخت هزینه، خریدت رو تموم کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Lost in the City"
            it[persianTitle] = "گم‌شده در شهر"
            it[description] =
                "You're exploring a new city and can't seem to find your way. Spot a local and ask them for directions to a famous landmark."
            it[persianDescription] =
                "داری یک شهر جدید رو می‌گردی و انگار راهت رو گم کردی. یک فرد محلی پیدا کن و ازش مسیر یک مکان دیدنی معروف رو بپرس."
            it[aiRole] = "Local Resident"
            it[aiName] = "Carlos"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/everyday_conversations/asking_for_directions_in_the_city.webp"
            it[points] = 14
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Politely approach the person, say hello, and let them know you need help with directions."
            it[persianDescription] = "مؤدبانه به اون شخص نزدیک شو، سلام کن و بهش بگو برای پیدا کردن مسیر به کمک احتیاج داری."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Be specific. Ask clear and direct questions to understand the route to the landmark."
            it[persianDescription] = "دقیق باش. سوالات واضح و مستقیمی بپرس تا مسیر رسیدن به اون مکان دیدنی رو بفهمی."
            it[createdAt] = LocalDateTime.now()
        }
        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Consider your options. Ask for their recommendation: is it better to walk or use public transport?"
            it[persianDescription] = "گزینه‌هات رو بسنج. ازشون راهنمایی بخواه: بهتره پیاده بری یا از وسایل نقلیه عمومی استفاده کنی؟"
            it[createdAt] = LocalDateTime.now()
        }
    }
}