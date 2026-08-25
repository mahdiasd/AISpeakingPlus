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

fun populateMusic() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Music"
            it[imageUrl] = "category/music.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Discovering Music Tastes"
            it[persianTitle] = "گپ و گفت موسیقی"
            it[description] = "You're catching up with your friend Layla over coffee. The conversation turns to music. It's the perfect moment to share your favorite genres, talk about artists you love, and discuss how music impacts your mood."
            it[persianDescription] = "داری با دوستت لیلا در یک کافه گپ می‌زنی که صحبت به موسیقی می‌کشه. الان بهترین فرصته تا در مورد سبک‌های مورد علاقه‌ات، هنرمندانی که دوست داری و تأثیری که موسیقی روی حال و هوات می‌گذاره، صحبت کنی."
            it[aiRole] = "Close Friend"
            it[aiName] = "Layla"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/music/favorite_genres.webp"
            it[points] = 15
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Tell your friend about your all-time favorite music genre and explain what you love about it."
            it[persianDescription] = "در مورد سبک موسیقی مورد علاقه‌ات به دوستت بگو و توضیح بده که چه چیزی را در آن دوست داری."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Recommend one artist and a must-listen song from them."
            it[persianDescription] = "یک هنرمند و یک آهنگ از او که حتماً باید شنیده شود را پیشنهاد بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Describe how listening to music can change your mood or help you through a tough day."
            it[persianDescription] = "توضیح بده که چگونه گوش دادن به موسیقی می‌تواند حال و هوای تو را تغییر دهد یا در یک روز سخت به تو کمک کند."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Buying Your First Guitar"
            it[persianTitle] = "خرید اولین گیتار"
            it[description] = "The dream of playing guitar starts today! You're in a music store, ready to buy your first instrument. Talk to the store assistant, Oliver, to get advice on the best beginner guitar—acoustic or electric—that fits your budget."
            it[persianDescription] = "رؤیای گیتار زدن از امروز شروع می‌شه! تو در یک فروشگاه موسیقی هستی و آماده‌ای تا اولین سازت رو بخری. با فروشنده، الیور، صحبت کن تا در مورد بهترین گیتار برای شروع—آکوستیک یا الکتریک—که با بودجه‌ات همخونی داشته باشه، راهنمایی بگیری."
            it[aiRole] = "Store Assistant"
            it[aiName] = "Oliver"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/music/first_guitar_buy.webp"
            it[points] = 18
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Start by telling the assistant you're a complete beginner and ask for a recommendation."
            it[persianDescription] = "با گفتن اینکه کاملاً مبتدی هستی شروع کن و از فروشنده پیشنهاد بخواه."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask about the main differences between acoustic and electric guitars for someone just starting out."
            it[persianDescription] = "در مورد تفاوت‌های اصلی گیتار آکوستیک و الکتریک برای کسی که تازه شروع کرده، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Mention your budget and ask about the best affordable options available."
            it[persianDescription] = "بودجه خود را ذکر کن و در مورد بهترین گزینه‌های مقرون‌به‌صرفه موجود بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Post-Concert Chat"
            it[persianTitle] = "گپ بعد از کنسرت"
            it[description] = "You and your friend are buzzing after a concert. During the show, the lead singer hit a noticeably off-key note. Now's the time to discuss it: Do live imperfections make a show more authentic, or do they ruin the experience?"
            it[persianDescription] = "تو و دوستت بعد از کنسرت حسابی هیجان‌زده‌اید. حین اجرا، خواننده اصلی یک نت را به وضوح فالش خواند. حالا وقتشه در موردش بحث کنید: آیا این نقص‌های اجرای زنده، آن را واقعی‌تر و جذاب‌تر می‌کند یا تجربه را خراب می‌کند؟"
            it[aiRole] = "Music Fan Friend"
            it[aiName] = "Sara"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/music/concert_off_key.webp"
            it[points] = 20
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Share your initial reaction when you heard the singer's off-key note."
            it[persianDescription] = "واکنش اولیه‌ات را وقتی نت فالش خواننده را شنیدی، به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Argue your point: do you think small mistakes are acceptable in a live performance?"
            it[persianDescription] = "نظر خود را مطرح کن: آیا فکر می‌کنی اشتباهات کوچک در یک اجرای زنده قابل قبول هستند؟"
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Besides that one moment, talk about the best and worst parts of the concert overall."
            it[persianDescription] = "فارغ از آن یک لحظه، در مورد بهترین و بدترین بخش‌های کلی کنسرت صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }
    }
}