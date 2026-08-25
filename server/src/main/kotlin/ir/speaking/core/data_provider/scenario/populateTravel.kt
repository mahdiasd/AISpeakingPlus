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

fun populateTravelAndTourism() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Travel & Adventure"
            it[imageUrl] = "category/travel.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Checking into Your Hotel"
            it[persianTitle] = "ورود به هتل"
            it[description] = "After a long trip, you've finally arrived in the city! You're at the front desk of a charming hotel. Talk to the receptionist, Lily, to find the perfect room for your stay."
            it[persianDescription] = "بعد از یک سفر طولانی، بالاخره به شهر رسیدی! الان پشت میز پذیرش یک هتل دوست‌داشتنی هستی. با مسئول پذیرش، لیلی، صحبت کن تا اتاق عالی برای اقامتت پیدا کنی."
            it[aiRole] = "Hotel Receptionist"
            it[aiName] = "Lily"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/travel/hotel_room_booking.webp"
            it[points] = 14
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Ask about the available room options and their rates."
            it[persianDescription] = "در مورد گزینه‌های اتاق موجود و قیمت‌هایشان سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Mention your preferences, like a quiet room or one with a nice view."
            it[persianDescription] = "ترجیحاتت را، مانند یک اتاق ساکت یا اتاقی با منظره خوب، بیان کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "State how many nights you'll be staying and confirm your booking."
            it[persianDescription] = "تعداد شب‌های اقامتت را بگو و رزروت را نهایی کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Lost in a New City"
            it[persianTitle] = "گم‌شده در شهری جدید"
            it[description] = "You're exploring a beautiful but unfamiliar city and have lost your way. You spot a friendly local, Marco, near a historic fountain. Approach him and ask for directions to your destination."
            it[persianDescription] = "داری در یک شهر زیبا اما ناآشنا گشت‌وگذار می‌کنی و راهت را گم کرده‌ای. یک فرد محلی و خوش‌برخورد به نام مارکو را نزدیک یک فواره تاریخی می‌بینی. به سمت او برو و مسیر رسیدن به مقصدت را بپرس."
            it[aiRole] = "Helpful Local"
            it[aiName] = "Marco"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/travel/asking_city_directions.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Politely get his attention and explain that you're lost."
            it[persianDescription] = "مؤدبانه توجهش را جلب کن و توضیح بده که گم شده‌ای."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Tell him where you're trying to go and ask for the easiest way to get there."
            it[persianDescription] = "به او بگو کجا می‌خواهی بروی و ساده‌ترین راه رسیدن به آنجا را بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "While you're at it, ask if there are any cool spots or cafes nearby."
            it[persianDescription] = "حالا که فرصتش هست، بپرس که آیا جای دیدنی یا کافه باحالی در این نزدیکی هست."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Handling a Flight Delay"
            it[persianTitle] = "مواجهه با تأخیر پرواز"
            it[description] = "You're stuck at the airport due to a long flight delay. It's time to approach the airline's service desk. Speak with the representative, Julia, to understand the situation and explore your options."
            it[persianDescription] = "به خاطر تأخیر طولانی پرواز در فرودگاه گیر افتاده‌ای. وقتشه که به باجه خدمات شرکت هواپیمایی بری. با نماینده شرکت، جولیا، صحبت کن تا از وضعیت باخبر بشی و گزینه‌های پیش رویت را بررسی کنی."
            it[aiRole] = "Airline Representative"
            it[aiName] = "Julia"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/travel/flight_delay_complaint.webp"
            it[points] = 20
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Provide your flight number and explain your frustration with the delay."
            it[persianDescription] = "شماره پروازت را بگو و ناراحتی‌ات را از این تأخیر توضیح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Ask for the reason for the delay and inquire about compensation policies."
            it[persianDescription] = "دلیل تأخیر را بپرس و در مورد سیاست‌های جبران خسارت سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Request information about the next available flight or accommodation options."
            it[persianDescription] = "در مورد پرواز بعدی موجود یا گزینه‌های اقامتی اطلاعات بخواه."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "At the Tourist Center"
            it[persianTitle] = "در مرکز اطلاعات گردشگری"
            it[description] = "You've just arrived in a new city and your first stop is the tourist information center. Chat with the helpful representative, Emma, to get insider tips on the best attractions, local events, and how to get around."
            it[persianDescription] = "همین الان به یک شهر جدید رسیدی و اولین توقفت مرکز اطلاعات گردشگری است. با نماینده خوش‌برخورد مرکز، اِما، گپ بزن تا نکات خودمانی در مورد بهترین جاذبه‌ها، رویدادهای محلی و نحوه گشت‌وگذار در شهر را بگیری."
            it[aiRole] = "Tourism Representative"
            it[aiName] = "Emma"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/travel/tourist_center_inquiry.webp"
            it[points] = 17
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Tell her how many days you're in town and ask for the 'must-see' attractions."
            it[persianDescription] = "به او بگو چند روز در شهر هستی و در مورد جاذبه‌هایی که «حتماً باید دید» سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Ask about any cool local events or festivals happening during your stay."
            it[persianDescription] = "در مورد هر رویداد یا جشنواره محلی باحالی که در طول اقامتت برگزار می‌شود، بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Inquire about the best way to travel around the city, like by bus or metro."
            it[persianDescription] = "در مورد بهترین راه برای گشت‌وگذار در شهر، مثل اتوبوس یا مترو، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Hostel Stories"
            it[persianTitle] = "داستان‌های هاستل"
            it[description] = "You're relaxing at a backpacker hostel and strike up a conversation with another traveler, Jade. She's curious about your past journeys and is looking for inspiration for her next trip."
            it[persianDescription] = "در یک هاستل داری استراحت می‌کنی و با یک مسافر دیگر به نام جید، سر صحبت را باز می‌کنی. او در مورد سفرهای گذشته‌ات کنجکاو است و دنبال ایده برای سفر بعدی‌اش می‌گردد."
            it[aiRole] = "Curious Backpacker"
            it[aiName] = "Jade"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/travel/sharing_travel_tales.webp"
            it[points] = 16
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Share a story from your favorite travel destination."
            it[persianDescription] = "داستانی از مقصد سفر مورد علاقه‌ات تعریف کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Give her a friendly warning or a tip about a challenge you faced on a trip."
            it[persianDescription] = "به او یک هشدار دوستانه یا نکته‌ای در مورد چالشی که در یکی از سفرهایت با آن روبرو شدی، بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Recommend a must-try local food or activity from one of your travels."
            it[persianDescription] = "یک غذای محلی یا فعالیتی که حتماً باید امتحان شود را از یکی از سفرهایت پیشنهاد بده."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario6Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "My Luggage is Missing!"
            it[persianTitle] = "چمدونم گم شده!"
            it[description] = "That sinking feeling—you're at the baggage claim, and your luggage is nowhere to be found. Approach the airport staff member, Sam, to report your missing bag and start the recovery process."
            it[persianDescription] = "اون حس ناخوشایند—در بخش تحویل بار هستی و از چمدانت هیچ خبری نیست. به سراغ کارمند فرودگاه، سَم، برو تا چمدان گمشده‌ات را گزارش دهی و فرآیند پیگیری را شروع کنی."
            it[aiRole] = "Airport Staff"
            it[aiName] = "Sam"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/travel/lost_luggage_recovery.webp"
            it[points] = 24
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Describe your missing suitcase (color, size, brand) and provide your flight details."
            it[persianDescription] = "چمدان گمشده‌ات (رنگ، اندازه، برند) را توصیف کن و جزئیات پروازت را ارائه بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Ask about the process for tracking it down and how long it might take."
            it[persianDescription] = "در مورد فرآیند پیگیری و اینکه چقدر ممکن است طول بکشد، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario6Id.value
            it[description] = "Inquire about any compensation for essential items you need to buy."
            it[persianDescription] = "در مورد هرگونه جبران خسارت برای وسایل ضروری که باید بخری، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario7Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Bargaining at the Market"
            it[persianTitle] = "چانه‌زنی در بازار"
            it[description] = "You're exploring a vibrant street market and a beautiful handmade souvenir catches your eye. It's time to engage in a friendly negotiation with the vendor, Abdul, to agree on a fair price."
            it[persianDescription] = "داری در یک بازار خیابانی پرجنب‌وجوش می‌گردی که یک سوغاتی دست‌ساز زیبا چشمت را می‌گیرد. وقتشه که وارد یک چانه‌زنی دوستانه با فروشنده، عبدل، بشی تا بر سر یک قیمت منصفانه توافق کنید."
            it[aiRole] = "Street Vendor"
            it[aiName] = "Abdul"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/travel/market_vendor_bargain.webp"
            it[points] = 10
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario7Id.value
            it[description] = "Show interest in an item and ask for the price."
            it[persianDescription] = "به یک کالا ابراز علاقه کن و قیمتش را بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario7Id.value
            it[description] = "Politely suggest that the price is a bit high and make a reasonable counteroffer."
            it[persianDescription] = "مؤدبانه بگو که قیمت کمی بالاست و یک پیشنهاد متقابل منطقی بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario7Id.value
            it[description] = "Find a price you both agree on and complete the purchase with a smile."
            it[persianDescription] = "به یک قیمت مورد توافق برسید و خرید را با لبخند نهایی کن."
            it[createdAt] = LocalDateTime.now()
        }
    }
}