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


fun populateCultureAndArt() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "Art & Culture"
            it[imageUrl] = "category/art.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Gallery Impressions"
            it[persianTitle] = "گشتی در گالری"
            it[description] =
                "You're stepping into a modern art gallery for the first time. The curator, Sophia, greets you, ready to guide you through halls filled with bold art. Every corner invites you to explore, question, and feel."
            it[persianDescription] =
                "برای اولین بار پا به یک گالری هنر مدرن می‌گذاری. سوفیا، راهنمای گالری، به استقبالت می‌آید تا تو را در میان سالن‌هایی پر از آثار هنری جسورانه راهنمایی کند. هر گوشه‌ای تو را به کشف، پرسش و احساس دعوت می‌کند."
            it[aiRole] = "Gallery Curator"
            it[aiName] = "Sophia"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "scenario/art/visiting_an_art_gallery_with_a_curator.webp"
            it[points] = 20
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Start by warmly greeting the curator and telling her you're excited to be there."
            it[persianDescription] = "با سلامی گرم به راهنما، مکالمه را شروع کن و به او بگو که از دیدن گالری هیجان‌زده‌ای."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Find a piece that catches your eye and ask about the story or inspiration behind it."
            it[persianDescription] = "اثری که چشمت را گرفته پیدا کن و در مورد داستان یا منبع الهام آن بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] = "Express your personal connection. Share how a piece makes you feel."
            it[persianDescription] = "ارتباط شخصی‌ات با آثار را به اشتراک بگذار. بگو که یک اثر خاص چه حسی در تو ایجاد می‌کند."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Culture Exchange"
            it[persianTitle] = "تبادل فرهنگی"
            it[description] =
                "You've just met Liam, a native English speaker, for a language exchange. It's the perfect chance to share stories about unique traditions, colorful festivals, and the flavors that define your cultures."
            it[persianDescription] =
                "تو همین الان با لیام، یک انگلیسی‌زبان، برای تبادل زبان آشنا شدی. این یک فرصت عالی برای به اشتراک گذاشتن داستان‌هایی از سنت‌های خاص، فستیوال‌های رنگارنگ و طعم‌هایی است که فرهنگ شما را می‌سازند."
            it[aiRole] = "Language Partner"
            it[aiName] = "Liam"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "scenario/art/discussing_cultural_traditions_in_a_language_exchange.webp"
            it[starter] = Role.User
            it[points] = 18
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Introduce yourself and express your curiosity about his culture."
            it[persianDescription] = "خودت را معرفی کن و کنجکاوی‌ات را در مورد فرهنگ او ابراز کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Talk about a major holiday in your country and the traditions that make it special."
            it[persianDescription] = "در مورد یکی از تعطیلات مهم کشورت و آداب و رسومی که آن را خاص می‌کند، صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] = "Ask Liam about a unique tradition or a famous dish from his country."
            it[persianDescription] = "از لیام در مورد یک سنت خاص یا یک غذای معروف کشورش بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Director's Interview"
            it[persianTitle] = "مصاحبه با کارگردان"
            it[description] =
                "Your short film just premiered at an international festival! Amid flashing cameras and an eager audience, a journalist approaches you for an interview about your passion and vision."
            it[persianDescription] =
                "فیلم کوتاه تو همین الان در یک جشنواره بین‌المللی اکران شد! در میان فلاش دوربین‌ها و تماشاگران مشتاق، یک خبرنگار برای مصاحبه درباره اشتیاق و دیدگاهت به سراغت می‌آید."
            it[aiRole] = "Professional Journalist"
            it[aiName] = "Rachel"
            it[gender] = Gender.Woman
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "scenario/art/attending_a_film_festival_interview.webp"
            it[points] = 25
            it[starter] = Role.Model
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Introduce yourself confidently and provide a captivating one-line summary of your film."
            it[persianDescription] = "با اعتماد به نفس خودت را معرفی کن و خلاصه‌ای یک خطی و جذاب از فیلمت ارائه بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Explain the central theme or message you wanted to convey with your film."
            it[persianDescription] = "تم اصلی یا پیامی که می‌خواستی با فیلمت منتقل کنی را توضیح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Talk about a filmmaker or artistic movement that has deeply influenced your work."
            it[persianDescription] = "درباره یک فیلمساز یا جنبش هنری که عمیقاً روی کارت تأثیر گذاشته صحبت کن."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] = "Share your vision for the future of independent filmmaking."
            it[persianDescription] = "دیدگاهت را در مورد آینده‌ی فیلم‌سازی مستقل به اشتراک بگذار."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Echoes of the Past"
            it[persianTitle] = "پژواک گذشته"
            it[description] =
                "You're wandering through a museum of ancient civilizations. Your guide, Daniel, is revealing the fascinating stories behind relics touched by people thousands of years ago."
            it[persianDescription] =
                "در یک موزه تمدن‌های باستانی قدم می‌زنی. راهنمای تو، دانیال، در حال رونمایی از داستان‌های شگفت‌انگیز پشت آثاری است که هزاران سال پیش توسط انسان‌ها لمس شده‌اند."
            it[aiRole] = "Museum Guide"
            it[aiName] = "Daniel"
            it[gender] = Gender.Man
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "scenario/art/talking_to_a_museum_guide_about_ancient_artifacts.webp"
            it[starter] = Role.Model
            it[points] = 22
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Point to an artifact and ask about its origins and historical period."
            it[persianDescription] = "به یک اثر باستانی اشاره کن و در مورد منشأ و دوره‌ی تاریخی آن بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Get curious about its purpose. Ask how it was used in daily life."
            it[persianDescription] = "در مورد کاربردش کنجکاو شو. بپرس که در زندگی روزمره چطور از آن استفاده می‌کردند."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Make a connection. Compare the artifact to a similar object or tradition from your culture."
            it[persianDescription] = "یک ارتباط برقرار کن. آن اثر را با یک شیء یا سنت مشابه در فرهنگ خودت مقایسه کن."
            it[createdAt] = LocalDateTime.now()
        }
    }
}