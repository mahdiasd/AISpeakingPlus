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

fun populateResilienceAndHope() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Resilience & Hope"
            it[imageUrl] = "category/resilience.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Reach the Peak"
            it[persianTitle] = "رسیدن به قله"
            it[description] =
                "You are hiking up a steep mountain and feeling very tired. A fellow hiker, Shirin, walks beside you and encourages you. Together, you talk about not giving up and the joy of breathing the free, fresh air at the top."
            it[persianDescription] =
                "در حال بالا رفتن از یک کوه شیب‌دار هستی و احساس خستگی می‌کنی. یک کوهنورد دیگر به نام شیرین در کنارت قدم برمی‌دارد و تشویقت می‌کند. شما با هم درباره تسلیم نشدن و لذت تنفس هوای آزاد و تازه در قله صحبت می‌کنید."
            it[aiRole] = "Fellow Hiker"
            it[aiName] = "Shirin"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "scenario/resilience/hikers_reaching_mountain_peak.webp"
            it[points] = 20
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Tell Shirin that you are exhausted but determined to keep going."
            it[persianDescription] = "به شیرین بگو که خیلی خسته‌ای اما مصممی که به مسیر ادامه دهی."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Ask her what motivates her to climb such difficult paths."
            it[persianDescription] = "از او بپرس چه چیزی به او انگیزه می‌دهد تا از چنین مسیرهای سختی بالا برود."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Talk about how amazing the view and the feeling of freedom will be at the top."
            it[persianDescription] = "درباره اینکه منظره و حس آزادی در قله چقدر شگفت‌انگیز خواهد بود صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        /*-----------------------------------------------------*/

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Song of Tomorrow"
            it[persianTitle] = "ترانه فردا"
            it[description] =
                "In a dimly lit café, you hear someone playing the guitar. The singer, Nika, is performing a heartfelt original song about longing for an ordinary life, simple dreams, and a brighter tomorrow."
            it[persianDescription] =
                "در یک کافه‌ی دنج، صدای گیتار به گوشت می‌رسد. خواننده، نیکا، ترانه‌ای دلی و اورجینال درباره‌ی حسرت یک زندگی معمولی، رویاهای ساده و فردایی روشن‌تر می‌خواند."
            it[aiRole] = "Indie Musician"
            it[aiName] = "Nika"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/resilience/musician_singing_in_cafe.webp"
            it[starter] = Role.User
            it[points] = 18
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Approach Nika after her song, introduce yourself, and praise her beautiful voice."
            it[persianDescription] = "بعد از تمام شدن آهنگ به نیکا نزدیک شو، خودت را معرفی کن و از صدای زیبایش تعریف کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask her what inspired her to write a song about 'longing for an ordinary life'."
            it[persianDescription] = "از او بپرس چه چیزی الهام‌بخش او برای نوشتن ترانه‌ای درباره «حسرت یک زندگی معمولی» بوده است."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Discuss how music can act as a voice for people's hidden pain and hopes."
            it[persianDescription] = "در مورد اینکه چگونه موسیقی می‌تواند صدای دردها و امیدهای پنهان مردم باشد، صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        /*-----------------------------------------------------*/

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Roots of Tomorrow"
            it[persianTitle] = "ریشه‌های فردا"
            it[description] =
                "You are volunteering at a park to plant young trees. You work with Navid, a friendly gardener. He explains how these small saplings will grow strong roots and survive any storm to create a beautiful future."
            it[persianDescription] =
                "در یک پارک برای کاشت درختان جوان داوطلب شده‌ای. تو با نوید، یک باغبان خوش‌برخورد، کار می‌کنی. او توضیح می‌دهد که چطور این نهال‌های کوچک ریشه‌های قوی می‌دوانند و از هر طوفانی جان سالم به در می‌برند تا آینده‌ای زیبا بسازند."
            it[aiRole] = "Volunteer Gardener"
            it[aiName] = "Navid"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/resilience/volunteers_planting_trees_in_park.webp"
            it[points] = 22
            it[starter] = Role.User
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Introduce yourself to Navid and ask him how to plant the sapling correctly."
            it[persianDescription] = "خودت را به نوید معرفی کن و از او بپرس چطور باید نهال را درست بکاریم."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Agree with him and talk about how small actions today build a better tomorrow."
            it[persianDescription] = "با او موافقت کن و بگو که چطور کارهای کوچک امروز، فردای بهتری می‌سازند."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Share a positive wish you have for the tree as it grows."
            it[persianDescription] = "یک آرزوی مثبت که برای بزرگ شدن این درخت داری را به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        /*-----------------------------------------------------*/

        // سناریوی چهارم: خانه از پای‌بست (اشاره به لزوم تغییرات بنیادین، عبور از اصلاحات سطحی و ساختن دوباره)
        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Rebuilding the Foundation"
            it[persianTitle] = "خانه از پای‌بست"
            it[description] =
                "You are standing in front of an old, crumbling mansion with Kaveh, an experienced architect. He explains that simply painting the walls won't work anymore. The foundation is totally broken, and it must be completely rebuilt so people can finally live there in peace and happiness."
            it[persianDescription] =
                "تو به همراه کاوه، یک معمار باتجربه، روبروی یک عمارت قدیمی و در حال فروپاشی ایستاده‌ای. او توضیح می‌دهد که دیگر فقط رنگ کردن دیوارها فایده‌ای ندارد. پایه و فونداسیون کاملاً خراب است و باید از ریشه دوباره ساخته شود تا آدم‌ها بتوانند بالاخره در آرامش و خوشبختی آنجا زندگی کنند."
            it[aiRole] = "Experienced Architect"
            it[aiName] = "Kaveh"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/resilience/architect_looking_at_old_crumbling_house.webp"
            it[points] = 25
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Tell Kaveh you agree that superficial changes are no longer enough for this place."
            it[persianDescription] = "به کاوه بگو موافقی که تغییرات ظاهری و سطحی دیگر برای این مکان کافی نیست."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Discuss the difficult but necessary decision to tear down the broken structure."
            it[persianDescription] = "درباره تصمیم سخت اما ضروری برای تخریب این ساختارِ خراب صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Share your vision of the bright, beautiful home that will take its place."
            it[persianDescription] = "دیدگاهت را درباره خانه روشن و زیبایی که جای آن را خواهد گرفت، به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        /*-----------------------------------------------------*/

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "A Light in the Dark"
            it[persianTitle] = "نوری در تاریکی"
            it[description] =
                "You are walking through a quiet square in the evening. You see a grieving mother, Roya, lighting candles in silence to remember someone very young who left this world too soon. You step forward to share her pain."
            it[persianDescription] =
                "عصر هنگام در حال قدم زدن در یک میدان خلوت هستی. یک مادر داغدار به نام رویا را می‌بینی که در سکوت شمع روشن می‌کند تا یاد جوان عزیزی که خیلی زود از این دنیا رفت را گرامی بدارد. تو جلو می‌روی تا در درد او شریک شوی."
            it[aiRole] = "Grieving Mother"
            it[aiName] = "Roya"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/resilience/mother_lighting_candles_at_night.webp"
            it[points] = 25
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Approach her softly and offer your deepest condolences."
            it[persianDescription] = "به‌آرامی به او نزدیک شو و عمیق‌ترین تسلیت‌هایت را ابراز کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Ask if you can light a candle next to hers to honor their memory."
            it[persianDescription] = "بپرس آیا می‌توانی شمعی در کنار شمع او روشن کنی تا یادشان را گرامی بداری."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Tell her that her loved one will never be forgotten."
            it[persianDescription] = "به او بگو که عزیز از دست رفته‌اش هرگز فراموش نخواهد شد."
            it[createdAt] = LocalDateTime.now()
        }

    }

}