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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Populates additional scenarios for existing categories.
 * This script looks up categories by name to ensure compatibility with the production database.
 */
fun populateNewScenarios() {
    transaction {
        // Helper function to find category ID by name
        fun getCategoryId(categoryName: String) = CategoryTable
            .selectAll().where { CategoryTable.name eq categoryName }
            .singleOrNull()
            ?.get(CategoryTable.id)

        // ==========================================
        // 1. Art & Culture
        // ==========================================
        getCategoryId("Art & Culture")?.let { cId ->
            // Scenario 1: Pottery Workshop
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Pottery Workshop Basics"
                it[persianTitle] = "مبانی کارگاه سفالگری"
                it[description] = "You've signed up for a pottery class. The instructor, Clara, is teaching you the basics of working with clay. Listen to her instructions and ask questions to get your hands dirty!"
                it[persianDescription] = "در یک کلاس سفالگری ثبت‌نام کرده‌اید. مربی، کلارا، اصول اولیه کار با گل رس را به شما آموزش می‌دهد. به دستورالعمل‌های او گوش دهید و سوال بپرسید تا کار را شروع کنید!"
                it[aiRole] = "Pottery Instructor"
                it[aiName] = "Clara"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_3.webp"
                it[imageUrl] = "scenario/art/pottery_class.webp"
                it[points] = 18
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask about the best type of clay for beginners to use."
                it[persianDescription] = "در مورد بهترین نوع گل رس برای استفاده مبتدیان سوال کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask for guidance on how to center the clay on the wheel."
                it[persianDescription] = "برای نحوه مرکز کردن گل روی چرخ سفالگری راهنمایی بخواهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Inquire about the firing process and when your piece will be ready."
                it[persianDescription] = "در مورد فرآیند پخت در کوره و زمان آماده شدن اثرتان سوال کنید."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 2: Graffiti Debate
            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Graffiti or Art?"
                it[persianTitle] = "گرافیتی یا هنر؟"
                it[description] = "You are looking at a large, colorful mural in the city center with a local artist, Leo. Engage in a debate about whether street art enhances the city or counts as vandalism."
                it[persianDescription] = "شما به همراه یک هنرمند محلی به نام لئو، به یک نقاشی دیواری بزرگ و رنگارنگ در مرکز شهر نگاه می‌کنید. وارد بحثی شوید که آیا هنر خیابانی شهر را زیباتر می‌کند یا خرابکاری محسوب می‌شود."
                it[aiRole] = "Local Artist"
                it[aiName] = "Leo"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/art/street_art_debate.webp"
                it[points] = 22
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Express your honest opinion about the mural you are looking at."
                it[persianDescription] = "نظر صادقانه خود را در مورد نقاشی دیواری که می‌بینید بیان کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Ask Leo what message he thinks the artist was trying to convey."
                it[persianDescription] = "از لئو بپرسید فکر می‌کند هنرمند سعی داشته چه پیامی را منتقل کند."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Discuss the fine line between artistic expression and illegal vandalism."
                it[persianDescription] = "در مورد مرز باریک بین بیان هنری و خرابکاری غیرقانونی بحث کنید."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 3: Book Club
            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Character Analysis"
                it[persianTitle] = "تحلیل شخصیت داستانی"
                it[description] = "You've joined a book club meeting led by Emma. The group is discussing the main character's decisions in the latest novel. Share your interpretation and analyze the plot twist."
                it[persianDescription] = "شما به جلسه باشگاه کتابخوانی که توسط اِما اداره می‌شود پیوسته‌اید. گروه در حال بحث درباره تصمیمات شخصیت اصلی در رمان اخیر است. تفسیر خود را به اشتراک بگذارید و پیچش داستانی را تحلیل کنید."
                it[aiRole] = "Book Club Leader"
                it[aiName] = "Emma"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/art/book_club.webp"
                it[points] = 20
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Describe the protagonist's main motivation in the story."
                it[persianDescription] = "انگیزه اصلی قهرمان داستان را شرح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Share your thoughts on the unexpected plot twist at the end."
                it[persianDescription] = "نظرات خود را در مورد پیچش داستانی غیرمنتظره در پایان کتاب به اشتراک بگذارید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Politely agree or disagree with Emma's interpretation of the theme."
                it[persianDescription] = "مودبانه با تفسیر اِما از درونمایه کتاب موافقت یا مخالفت کنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 2. Business & Work
        // ==========================================
        getCategoryId("Business & Work")?.let { cId ->
            // Scenario 1: Elevator Pitch
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "The Elevator Pitch"
                it[persianTitle] = "ارائه آسانسوری"
                it[description] = "You've bumped into a potential investor, Mr. Sterling, at a networking event. You have 2 minutes to pitch your business idea effectively and secure a follow-up meeting."
                it[persianDescription] = "شما در یک رویداد شبکه‌سازی به طور اتفاقی با یک سرمایه‌گذار بالقوه، آقای استرلینگ، برخورد کرده‌اید. ۲ دقیقه فرصت دارید تا ایده کسب‌وکار خود را به طور مؤثر ارائه دهید و یک جلسه پیگیری بگیرید."
                it[aiRole] = "Investor"
                it[aiName] = "Mr. Sterling"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/business/elevator_pitch.webp"
                it[points] = 25
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Introduce yourself and hook the investor with a compelling opening statement."
                it[persianDescription] = "خود را معرفی کنید و با یک جمله آغازین جذاب، سرمایه‌گذار را جذب کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Clearly explain the problem your business solves and your unique solution."
                it[persianDescription] = "به وضوح مشکلی که کسب‌وکار شما حل می‌کند و راه‌حل منحصر به فردتان را توضیح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask for his contact information or a specific time for a follow-up meeting."
                it[persianDescription] = "اطلاعات تماس او یا زمان مشخصی را برای یک جلسه پیگیری درخواست کنید."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 2: Conflict Resolution
            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Resolving Conflict"
                it[persianTitle] = "حل اختلاف تیمی"
                it[description] = "Tensions are high between your department and marketing. You are meeting with the Marketing Lead, Sarah, to mediate the situation. Your goal is to de-escalate the conflict and find a compromise."
                it[persianDescription] = "تنش بین دپارتمان شما و بازاریابی بالا گرفته است. شما با مسئول بازاریابی، سارا، جلسه‌ای دارید تا وضعیت را میانجی‌گری کنید. هدف شما کاهش تنش و یافتن یک مصالحه است."
                it[aiRole] = "Marketing Lead"
                it[aiName] = "Sarah"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/business/conflict_resolution.webp"
                it[points] = 22
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "State your perspective on the issue calmly and objectively."
                it[persianDescription] = "دیدگاه خود را در مورد مسئله به آرامی و بی‌طرفانه بیان کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Acknowledge Sarah's valid points to show you are listening."
                it[persianDescription] = "نکات معتبر سارا را تایید کنید تا نشان دهید که گوش می‌دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Propose a compromise or a collaborative solution to move forward."
                it[persianDescription] = "یک مصالحه یا راه حل مشترک برای پیشرفت کار پیشنهاد دهید."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 3: Salary Negotiation
            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Salary Negotiation"
                it[persianTitle] = "مذاکره حقوق"
                it[description] = "You've received a job offer, but the salary is lower than expected. Call the HR Manager, David, to negotiate a better package based on your experience and market rates."
                it[persianDescription] = "پیشنهاد شغلی دریافت کرده‌اید، اما حقوق آن کمتر از حد انتظار است. با مدیر منابع انسانی، دیوید، تماس بگیرید تا بر اساس تجربه خود و نرخ بازار، برای بسته پیشنهادی بهتری مذاکره کنید."
                it[aiRole] = "HR Manager"
                it[aiName] = "David"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/business/salary_negotiation.webp"
                it[points] = 24
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Express gratitude for the offer and excitement about the role."
                it[persianDescription] = "از پیشنهاد و هیجان خود برای این نقش قدردانی کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Present data or reasons why your experience justifies a higher salary."
                it[persianDescription] = "داده‌ها یا دلایلی ارائه دهید که چرا تجربه شما حقوق بالاتری را توجیه می‌کند."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Propose a specific counter-offer number or ask for other benefits."
                it[persianDescription] = "یک رقم مشخص به عنوان پیشنهاد متقابل ارائه دهید یا مزایای دیگری درخواست کنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 3. Academic Life
        // ==========================================
        getCategoryId("Academic Life")?.let { cId ->
            // Scenario 1
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Study Group Strategy"
                it[persianTitle] = "استراتژی گروه مطالعه"
                it[description] = "Finals are approaching. Discuss with your classmate, Ben, how to organize a study group. Decide on the schedule, topics to cover, and where to meet."
                it[persianDescription] = "امتحانات نهایی نزدیک است. با همکلاسی خود، بن، درباره نحوه سازماندهی یک گروه مطالعه صحبت کنید. در مورد برنامه زمانی، مباحثی که باید پوشش داده شود و محل ملاقات تصمیم بگیرید."
                it[aiRole] = "Classmate"
                it[aiName] = "Ben"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/education/study_group.webp"
                it[points] = 14
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Propose a regular meeting time that works for both of you."
                it[persianDescription] = "یک زمان ملاقات منظم که برای هر دوی شما مناسب باشد پیشنهاد دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Suggest dividing the topics so each person teaches a section."
                it[persianDescription] = "پیشنهاد دهید مباحث را تقسیم کنید تا هر نفر بخشی را آموزش دهد."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Agree on a quiet location, like the library or a cafe."
                it[persianDescription] = "بر سر یک مکان ساکت، مانند کتابخانه یا کافه توافق کنید."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 2
            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Research Assistance"
                it[persianTitle] = "کمک برای تحقیق"
                it[description] = "You are working on a thesis and need specific resources. Talk to the university librarian, Mrs. Gable, to find academic journals and access the digital archives."
                it[persianDescription] = "شما روی پایان‌نامه کار می‌کنید و به منابع خاصی نیاز دارید. با کتابدار دانشگاه، خانم گابل، صحبت کنید تا مجلات علمی را پیدا کرده و به آرشیو دیجیتال دسترسی پیدا کنید."
                it[aiRole] = "Librarian"
                it[aiName] = "Mrs. Gable"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_1.webp"
                it[imageUrl] = "scenario/education/library_help.webp"
                it[points] = 15
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Clearly explain your research topic and what kind of information you need."
                it[persianDescription] = "موضوع تحقیق خود و نوع اطلاعاتی که نیاز دارید را به وضوح توضیح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Ask how to access the university's online database from home."
                it[persianDescription] = "بپرسید چگونه می‌توانید از خانه به پایگاه داده آنلاین دانشگاه دسترسی داشته باشید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Inquire if she can recommend any specific journals or authors."
                it[persianDescription] = "بپرسید آیا او می‌تواند مجلات یا نویسندگان خاصی را توصیه کند."
                it[createdAt] = LocalDateTime.now()
            }

            // Scenario 3
            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Scholarship Interview"
                it[persianTitle] = "مصاحبه بورسیه تحصیلی"
                it[description] = "You have been shortlisted for a prestigious scholarship. Answer the committee member, Dr. Aris, questions about your achievements, financial need, and future goals confidently."
                it[persianDescription] = "شما در لیست نهایی یک بورسیه معتبر قرار گرفته‌اید. به سوالات عضو کمیته، دکتر آریس، در مورد دستاوردها، نیاز مالی و اهداف آینده خود با اعتماد به نفس پاسخ دهید."
                it[aiRole] = "Committee Member"
                it[aiName] = "Dr. Aris"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/education/scholarship_interview.webp"
                it[points] = 22
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Highlight your most significant academic achievement so far."
                it[persianDescription] = "مهم‌ترین دستاورد تحصیلی خود تا کنون را برجسته کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Explain why you need financial support for your studies."
                it[persianDescription] = "توضیح دهید که چرا برای تحصیلات خود به حمایت مالی نیاز دارید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Describe how this scholarship will help you achieve your career goals."
                it[persianDescription] = "شرح دهید که این بورسیه چگونه به شما در رسیدن به اهداف شغلی‌تان کمک می‌کند."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 4. Emergency
        // ==========================================
        getCategoryId("Emergency")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Reporting a Theft"
                it[persianTitle] = "گزارش سرقت"
                it[description] = "You are at the police station to report that your backpack was stolen in the park. Give the officer, Sergeant Miller, all the necessary details for the report."
                it[persianDescription] = "شما در اداره پلیس هستید تا گزارش دهید کوله‌پشتی‌تان در پارک دزدیده شده است. تمام جزئیات لازم را به افسر، گروهبان میلر، برای گزارش بدهید."
                it[aiRole] = "Police Officer"
                it[aiName] = "Sgt. Miller"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/emergency/reporting_theft.webp"
                it[points] = 18
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "State exactly when and where the theft occurred."
                it[persianDescription] = "دقیقاً بیان کنید که سرقت در چه زمانی و کجا اتفاق افتاده است."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Describe the stolen bag (color, brand) and its contents."
                it[persianDescription] = "کیف دزدیده شده (رنگ، برند) و محتویات آن را توصیف کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask for a reference number for insurance purposes."
                it[persianDescription] = "برای مقاصد بیمه، درخواست شماره پیگیری (Reference number) کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Stuck in an Elevator"
                it[persianTitle] = "گیر کردن در آسانسور"
                it[description] = "The elevator has stopped between floors! You are speaking to the emergency operator through the intercom. Remain calm and answer their questions to get help."
                it[persianDescription] = "آسانسور بین طبقات متوقف شده است! شما از طریق آیفون داخلی با اپراتور اضطراری صحبت می‌کنید. آرام بمانید و به سوالات او پاسخ دهید تا کمک بگیرید."
                it[aiRole] = "Emergency Operator"
                it[aiName] = "Operator"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/emergency/elevator_stuck.webp"
                it[points] = 16
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Report the issue and your approximate location (building floor)."
                it[persianDescription] = "مشکل و موقعیت تقریبی خود (طبقه ساختمان) را گزارش دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "State how many people are in the elevator with you."
                it[persianDescription] = "اعلام کنید چند نفر همراه شما در آسانسور هستند."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Confirm if anyone is injured or panicking."
                it[persianDescription] = "تایید کنید که آیا کسی آسیب دیده یا وحشت‌زده است."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Severe Allergic Reaction"
                it[persianTitle] = "واکنش شدید آلرژیک"
                it[description] = "Your friend is having a severe allergic reaction (anaphylaxis) after eating peanuts. Call 911 immediately. Provide the dispatcher with critical information and ask for instructions."
                it[persianDescription] = "دوست شما پس از خوردن بادام‌زمینی دچار واکنش شدید آلرژیک (آنافلاکسی) شده است. فوراً با ۹۱۱ تماس بگیرید. اطلاعات حیاتی را به مسئول اعزام بدهید و دستورالعمل بخواهید."
                it[aiRole] = "911 Dispatcher"
                it[aiName] = "Officer Sarah"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_1.webp"
                it[imageUrl] = "scenario/emergency/allergy_reaction.webp"
                it[points] = 25
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "State clearly that someone is having a severe allergic reaction."
                it[persianDescription] = "به وضوح اعلام کنید که شخصی دچار واکنش شدید آلرژیک شده است."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Identify the allergen (peanuts) and report difficulty breathing."
                it[persianDescription] = "ماده حساسیت‌زا (بادام‌زمینی) را شناسایی کرده و مشکل تنفسی را گزارش دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask if you should administer their EpiPen while waiting."
                it[persianDescription] = "بپرسید آیا باید در حین انتظار از EpiPen (آمپول ضد حساسیت) آن‌ها استفاده کنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 5. Everyday Conversations
        // ==========================================
        getCategoryId("Everyday Conversations")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "The New Look"
                it[persianTitle] = "ظاهر جدید"
                it[description] = "You're at the salon for a haircut. Explain to the stylist, Mark, exactly what kind of style you want, how much length to take off, and ask for his professional opinion."
                it[persianDescription] = "برای کوتاه کردن مو به سالن رفته‌اید. دقیقاً به آرایشگر، مارک، توضیح دهید چه مدل مویی می‌خواهید، چقدر از طول آن کوتاه کند و نظر حرفه‌ای او را بخواهید."
                it[aiRole] = "Stylist"
                it[aiName] = "Mark"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/everyday_conversations/haircut.webp"
                it[points] = 16
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Describe the hairstyle you want (e.g., layers, bob, trim)."
                it[persianDescription] = "مدل مویی که می‌خواهید (مثلاً لایه‌لایه، مصری، نوک‌گیری) را توصیف کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Show a reference photo (describe it) and ask if it suits your face shape."
                it[persianDescription] = "یک عکس نمونه نشان دهید (توصیف کنید) و بپرسید آیا به فرم صورت شما می‌آید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask about which styling products are best for your hair type."
                it[persianDescription] = "بپرسید چه محصولات حالت‌دهنده‌ای برای نوع موی شما بهتر هستند."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Dinner Plans"
                it[persianTitle] = "برنامه شام"
                it[description] = "You want to catch up with your friend, Alice. Call her to invite her to dinner, decide on a type of cuisine, and agree on a time and place."
                it[persianDescription] = "می‌خواهید با دوستتان، آلیس، دیداری تازه کنید. با او تماس بگیرید تا برای شام دعوتش کنید، در مورد نوع غذا تصمیم بگیرید و بر سر زمان و مکان توافق کنید."
                it[aiRole] = "Friend"
                it[aiName] = "Alice"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/everyday_conversations/dinner_invite.webp"
                it[points] = 14
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Suggest going out for dinner this weekend to catch up."
                it[persianDescription] = "پیشنهاد دهید این آخر هفته برای شام بیرون بروید و گپ بزنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Propose a specific cuisine (e.g., Italian or Sushi) and ask if she likes it."
                it[persianDescription] = "یک نوع غذای خاص (مثلاً ایتالیایی یا سوشی) پیشنهاد دهید و بپرسید آیا دوست دارد."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Confirm the day and time to meet."
                it[persianDescription] = "روز و ساعت قرار را نهایی کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "The Noisy Neighbor"
                it[persianTitle] = "همسایه پر سر و صدا"
                it[description] = "It's late at night, and your neighbor, Chris, is playing loud music. Go to his door and politely ask him to turn it down because you have work early in the morning."
                it[persianDescription] = "دیر وقت است و همسایه‌تان، کریس، موسیقی با صدای بلند پخش می‌کند. دم در خانه‌اش بروید و مؤدبانه از او بخواهید صدا را کم کند چون فردا صبح زود باید سر کار بروید."
                it[aiRole] = "Neighbor"
                it[aiName] = "Chris"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/everyday_conversations/noisy_neighbor.webp"
                it[points] = 20
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Politely knock and apologize for the interruption."
                it[persianDescription] = "مؤدبانه در بزنید و بابت وقفه ایجاد شده عذرخواهی کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Explain that the music is too loud and you can hear it in your apartment."
                it[persianDescription] = "توضیح دهید که صدای موسیقی خیلی بلند است و در آپارتمان شما شنیده می‌شود."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask him to lower the volume because you need to sleep."
                it[persianDescription] = "از او بخواهید صدا را کم کند چون نیاز دارید بخوابید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 6. Health & Wellness
        // ==========================================
        getCategoryId("Health & Wellness")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Dental Emergency"
                it[persianTitle] = "اورژانس دندانپزشکی"
                it[description] = "You have a severe toothache and need an urgent appointment. Call the dental clinic receptionist, Mark, describe your pain, and find the earliest available slot."
                it[persianDescription] = "دندان‌درد شدیدی دارید و به نوبت فوری نیاز دارید. با منشی کلینیک دندانپزشکی، مارک، تماس بگیرید، درد خود را توصیف کنید و اولین نوبت خالی را بگیرید."
                it[aiRole] = "Dental Receptionist"
                it[aiName] = "Mark"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/medical/dentist_appointment.webp"
                it[points] = 19
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Describe the type of pain (e.g., throbbing, sharp, constant)."
                it[persianDescription] = "نوع درد را توصیف کنید (مثلاً ضربان‌دار، تیز، مداوم)."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Mention if you have sensitivity to hot or cold food."
                it[persianDescription] = "اشاره کنید که آیا به غذای گرم یا سرد حساسیت دارید یا خیر."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask if they can squeeze you in for an emergency appointment today."
                it[persianDescription] = "بپرسید آیا می‌توانند امروز به شما یک نوبت اضطراری بدهند."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Dietary Consultation"
                it[persianTitle] = "مشاوره تغذیه"
                it[description] = "You want to improve your eating habits. Discuss your current diet and health goals with a nutritionist, Dr. Green, to create a better meal plan."
                it[persianDescription] = "می‌خواهید عادات غذایی خود را بهبود ببخشید. با دکتر گرین، متخصص تغذیه، درباره رژیم فعلی و اهداف سلامتی خود صحبت کنید تا برنامه غذایی بهتری تنظیم کنید."
                it[aiRole] = "Nutritionist"
                it[aiName] = "Dr. Green"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_1.webp"
                it[imageUrl] = "scenario/medical/nutritionist.webp"
                it[points] = 20
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Describe your typical daily meals and snacks."
                it[persianDescription] = "وعده‌های غذایی و میان‌وعده‌های معمول روزانه خود را شرح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "State your main goal (e.g., more energy, weight loss, building muscle)."
                it[persianDescription] = "هدف اصلی خود را بیان کنید (مثلاً انرژی بیشتر، کاهش وزن، عضله‌سازی)."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Ask for tips on meal prepping or healthy snacking."
                it[persianDescription] = "برای آماده‌سازی وعده‌های غذایی یا میان‌وعده‌های سالم راهنمایی بخواهید."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Choosing Glasses"
                it[persianTitle] = "انتخاب عینک"
                it[description] = "You need a new pair of glasses. Talk to the optician, Sam, about frames that suit your face shape and options for lenses, like blue light protection."
                it[persianDescription] = "به یک عینک جدید نیاز دارید. با عینک‌ساز، سم، درباره فریم‌هایی که به صورتتان می‌آید و گزینه‌های عدسی، مثل محافظ نور آبی، صحبت کنید."
                it[aiRole] = "Optician"
                it[aiName] = "Sam"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_3.webp"
                it[imageUrl] = "scenario/medical/optician.webp"
                it[points] = 16
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask for recommendations on frames that fit your face shape."
                it[persianDescription] = "برای فریم‌هایی که مناسب فرم صورت شما هستند، پیشنهاد بخواهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Inquire about lenses with blue light filters for computer work."
                it[persianDescription] = "درباره عدسی‌هایی با فیلتر نور آبی برای کار با کامپیوتر پرس‌وجو کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Check if your insurance covers the cost of the frames."
                it[persianDescription] = "بررسی کنید که آیا بیمه شما هزینه فریم‌ها را پوشش می‌دهد یا خیر."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 7. IELTS Exam
        // ==========================================
        getCategoryId("IELTS Exam")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Speaking Part 2: Role Model"
                it[persianTitle] = "اسپیکینگ پارت ۲: الگوی زندگی"
                it[description] = "The examiner gives you a topic card: 'Describe a person you admire'. You have 1-2 minutes to speak. Structure your monologue to cover who they are, how you know them, and why they inspire you."
                it[persianDescription] = "ممتحن به شما یک کارت موضوع می‌دهد: «شخصی را که تحسین می‌کنید توصیف کنید». شما ۱ تا ۲ دقیقه فرصت صحبت دارید. مونولوگ خود را طوری ساختار دهید که بگویید او کیست، چطور او را می‌شناسید و چرا الهام‌بخش شماست."
                it[aiRole] = "IELTS Examiner"
                it[aiName] = "Mr. Davies"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/ielts_exam/part2_role_model.webp"
                it[points] = 70
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Introduce the person and explain your relationship to them."
                it[persianDescription] = "شخص مورد نظر را معرفی کنید و رابطه خود را با او توضیح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Describe their personality and key achievements."
                it[persianDescription] = "شخصیت و دستاوردهای کلیدی آن‌ها را توصیف کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Explain specifically why this person is a role model for you."
                it[persianDescription] = "به طور خاص توضیح دهید چرا این شخص الگوی شماست."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Speaking Part 3: Environment"
                it[persianTitle] = "اسپیکینگ پارت ۳: محیط زیست"
                it[description] = "Following Part 2, engage in a deep discussion about environmental issues. Analyze causes of pollution, discuss individual vs. government responsibility, and predict future trends."
                it[persianDescription] = "در ادامه پارت ۲، وارد بحثی عمیق درباره مسائل زیست‌محیطی شوید. علل آلودگی را تحلیل کنید، درباره مسئولیت فردی در مقابل دولت بحث کنید و روندهای آینده را پیش‌بینی کنید."
                it[aiRole] = "IELTS Examiner"
                it[aiName] = "Ms. Stone"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_1.webp"
                it[imageUrl] = "scenario/ielts_exam/part3_environment.webp"
                it[points] = 75
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Discuss the main causes of pollution in modern cities."
                it[persianDescription] = "درباره علل اصلی آلودگی در شهرهای مدرن بحث کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Evaluate whether individuals or governments should do more to protect the planet."
                it[persianDescription] = "ارزیابی کنید که آیا افراد باید بیشتر برای حفاظت از سیاره تلاش کنند یا دولت‌ها."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Speculate on how environmental problems might change in the next 50 years."
                it[persianDescription] = "گمانه‌زنی کنید که مشکلات زیست‌محیطی در ۵۰ سال آینده چگونه ممکن است تغییر کنند."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Listening Strategy: Maps"
                it[persianTitle] = "استراتژی لیسنینگ: نقشه‌ها"
                it[description] = "Discuss strategies for the 'Map Labeling' task in IELTS Listening. Talk about identifying landmarks, understanding directional language, and predicting answers."
                it[persianDescription] = "درباره استراتژی‌های تسک «برچسب‌گذاری نقشه» در لیسنینگ آیلتس بحث کنید. در مورد شناسایی نقاط شاخص، درک زبان جهت‌دهی و پیش‌بینی پاسخ‌ها صحبت کنید."
                it[aiRole] = "IELTS Tutor"
                it[aiName] = "Mr. Clark"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_3.webp"
                it[imageUrl] = "scenario/ielts_exam/listening_maps.webp"
                it[points] = 65
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Explain the importance of identifying where you 'start' on the map."
                it[persianDescription] = "اهمیت شناسایی نقطه «شروع» روی نقشه را توضیح دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "List common directional phrases used in these tasks (e.g., 'opposite', 'adjacent')."
                it[persianDescription] = "عبارات رایج جهت‌دهی که در این تسک‌ها استفاده می‌شوند (مثل روبرو، مجاور) را نام ببرید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Discuss how to use the preparation time effectively before the audio starts."
                it[persianDescription] = "درباره نحوه استفاده مؤثر از زمان آماده‌سازی قبل از شروع فایل صوتی بحث کنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 8. Music
        // ==========================================
        getCategoryId("Music")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Auditioning for a Band"
                it[persianTitle] = "تست گروه موسیقی"
                it[description] = "A local band is looking for a new member. You are talking to the band leader, Mike. Introduce yourself, talk about your musical experience, and set up an audition."
                it[persianDescription] = "یک گروه موسیقی محلی دنبال عضو جدید است. شما با رهبر گروه، مایک، صحبت می‌کنید. خودتان را معرفی کنید، از تجربه موسیقی‌تان بگویید و برای تست قرار بگذارید."
                it[aiRole] = "Band Leader"
                it[aiName] = "Mike"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/music/band_audition.webp"
                it[points] = 20
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Introduce yourself and mention which instrument you play."
                it[persianDescription] = "خودتان را معرفی کنید و بگویید چه سازی می‌نوازید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask about the genre of music the band plays and their influences."
                it[persianDescription] = "درباره سبک موسیقی گروه و تاثیرپذیری‌های آن‌ها سوال کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Arrange a time and place for a jam session or audition."
                it[persianDescription] = "زمانی و مکانی را برای جلسه تمرینی یا تست هماهنگ کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "The Perfect Playlist"
                it[persianTitle] = "پلی‌لیست عالی"
                it[description] = "You are planning a party with your friend, Chloe. Work together to curate the perfect playlist. Discuss genres, suggest specific tracks, and agree on the overall vibe."
                it[persianDescription] = "شما و دوستتان، کلوئی، در حال برنامه‌ریزی یک مهمانی هستید. با هم کار کنید تا یک پلی‌لیست عالی بسازید. در مورد سبک‌ها بحث کنید، آهنگ‌های خاص پیشنهاد دهید و بر سر حال و هوای کلی توافق کنید."
                it[aiRole] = "Friend"
                it[aiName] = "Chloe"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/music/playlist_planning.webp"
                it[points] = 16
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Suggest a specific vibe for the party (e.g., chill, upbeat, 80s)."
                it[persianDescription] = "یک حال و هوای خاص برای مهمانی پیشنهاد دهید (مثلاً آرام، پرانرژی، دهه ۸۰)."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Recommend a must-have song and explain why it fits."
                it[persianDescription] = "یک آهنگ ضروری را پیشنهاد دهید و توضیح دهید چرا مناسب است."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Politely veto or agree with one of Chloe's song choices."
                it[persianDescription] = "مودبانه با یکی از انتخاب‌های کلوئی مخالفت یا موافقت کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Music Theory 101"
                it[persianTitle] = "تئوری موسیقی ۱۰۱"
                it[description] = "You want to learn how to read music. Ask your music teacher, Mr. Piano, about the basics. Discuss scales, time signatures, and how to start practicing."
                it[persianDescription] = "می‌خواهید یاد بگیرید چطور نت‌خوانی کنید. از معلم موسیقی خود، آقای پیانو، درباره اصول اولیه بپرسید. در مورد گام‌ها، کسر میزان و نحوه شروع تمرین بحث کنید."
                it[aiRole] = "Music Teacher"
                it[aiName] = "Mr. Piano"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/music/music_theory.webp"
                it[points] = 18
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask about the difference between major and minor scales."
                it[persianDescription] = "درباره تفاوت گام‌های ماژور و مینور سوال کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Inquire what a time signature tells a musician."
                it[persianDescription] = "بپرسید کسر میزان چه چیزی را به نوازنده می‌گوید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask for simple exercises to practice reading sheet music."
                it[persianDescription] = "درخواست تمرین‌های ساده برای یادگیری نت‌خوانی کنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 9. Sports & Fitness
        // ==========================================
        getCategoryId("Sports & Fitness")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Marathon Prep"
                it[persianTitle] = "آمادگی ماراتن"
                it[description] = "You've signed up for your first marathon! Talk to Coach Sarah about the crucial final weeks of training, nutrition strategies, and what gear to wear on race day."
                it[persianDescription] = "برای اولین ماراتن خود ثبت‌نام کرده‌اید! با مربی سارا در مورد هفته‌های حیاتی آخر تمرین، استراتژی‌های تغذیه و تجهیزاتی که باید در روز مسابقه بپوشید، صحبت کنید."
                it[aiRole] = "Running Coach"
                it[aiName] = "Coach Sarah"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/sports/marathon_prep.webp"
                it[points] = 22
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask about the 'tapering' schedule before the race."
                it[persianDescription] = "درباره برنامه «کاهش فشار تمرین» قبل از مسابقه سوال کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Discuss what to eat in the week leading up to the marathon."
                it[persianDescription] = "درباره اینکه در هفته منتهی به ماراتن چه بخورید بحث کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask for advice on selecting the right running shoes and socks."
                it[persianDescription] = "برای انتخاب کفش و جوراب مناسب دویدن راهنمایی بخواهید."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Swimming Technique"
                it[persianTitle] = "تکنیک شنا"
                it[description] = "You are at the pool trying to improve your freestyle stroke. Ask the swim instructor, David, for tips on breathing, body position, and efficiency in the water."
                it[persianDescription] = "در استخر هستید و سعی دارید کرال سینه خود را بهتر کنید. از مربی شنا، دیوید، نکاتی درباره تنفس، وضعیت بدن و کارایی در آب بخواهید."
                it[aiRole] = "Swim Instructor"
                it[aiName] = "David"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/sports/swimming_lesson.webp"
                it[points] = 18
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Ask how to coordinate your breathing with your strokes."
                it[persianDescription] = "بپرسید چطور تنفس خود را با حرکات دست هماهنگ کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Discuss how to keep your legs from sinking."
                it[persianDescription] = "درباره اینکه چطور از پایین رفتن پاها جلوگیری کنید بحث کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Request a specific drill to practice on your own."
                it[persianDescription] = "یک تمرین خاص برای انجام دادن به تنهایی درخواست کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Pre-Game Strategy"
                it[persianTitle] = "استراتژی قبل از بازی"
                it[description] = "It's moments before a big basketball game. As a key player, talk to the team captain, Alex, to discuss the opponent's weaknesses and boost team morale."
                it[persianDescription] = "لحظاتی قبل از یک بازی بزرگ بسکتبال است. به عنوان یک بازیکن کلیدی، با کاپیتان تیم، الکس، صحبت کنید تا نقاط ضعف حریف را بررسی کرده و روحیه تیم را بالا ببرید."
                it[aiRole] = "Team Captain"
                it[aiName] = "Alex"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/sports/pre_game_huddle.webp"
                it[points] = 20
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Identify a key weakness in the opposing team's defense."
                it[persianDescription] = "یک نقطه ضعف کلیدی در دفاع تیم حریف را شناسایی کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Suggest a play or tactic to exploit that weakness."
                it[persianDescription] = "یک نقشه یا تاکتیک برای استفاده از آن نقطه ضعف پیشنهاد دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Say something encouraging to motivate the captain and the team."
                it[persianDescription] = "حرفی دلگرم‌کننده برای انگیزه دادن به کاپیتان و تیم بزنید."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 10. Tech & Innovation
        // ==========================================
        getCategoryId("Tech & Innovation")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Internet Woes"
                it[persianTitle] = "مشکلات اینترنت"
                it[description] = "Your home internet keeps dropping out. Call the ISP technical support agent, Ben, to describe the issue, troubleshoot the router, and request a technician if needed."
                it[persianDescription] = "اینترنت خانه شما مدام قطع می‌شود. با پشتیبان فنی، بن، تماس بگیرید تا مشکل را شرح دهید، مودم را عیب‌یابی کنید و در صورت نیاز درخواست تکنیسین دهید."
                it[aiRole] = "ISP Tech Support"
                it[aiName] = "Ben"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_5.webp"
                it[imageUrl] = "scenario/technology/internet_support.webp"
                it[points] = 18
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Describe the connectivity issue (e.g., slow speed, frequent disconnection)."
                it[persianDescription] = "مشکل اتصال (مثلاً سرعت پایین، قطع شدن مکرر) را توصیف کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Confirm the status of the lights on your router."
                it[persianDescription] = "وضعیت چراغ‌های روی مودم خود را تایید کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask to schedule a technician visit if the remote fix fails."
                it[persianDescription] = "اگر اصلاح از راه دور جواب نداد، درخواست اعزام تکنیسین کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "The VR Experience"
                it[persianTitle] = "تجربه واقعیت مجازی"
                it[description] = "You just tried a high-end VR headset. Discuss the experience with your friend, Tina. Describe the immersion, any motion sickness, and whether you think it's the future of gaming."
                it[persianDescription] = "شما به تازگی یک هدست واقعیت مجازی پیشرفته را امتحان کرده‌اید. تجربه خود را با دوستتان، تینا، در میان بگذارید. غوطه‌وری، حالت تهوع احتمالی و اینکه آیا فکر می‌کنید این آینده بازی‌هاست را توصیف کنید."
                it[aiRole] = "Gamer Friend"
                it[aiName] = "Tina"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_2.webp"
                it[imageUrl] = "scenario/technology/vr_experience.webp"
                it[points] = 16
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Describe how realistic and immersive the VR world felt."
                it[persianDescription] = "توصیف کنید که دنیای واقعیت مجازی چقدر واقعی و غوطه‌ور کننده بود."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Mention if you experienced any motion sickness or discomfort."
                it[persianDescription] = "اشاره کنید که آیا دچار حالت تهوع یا ناراحتی شدید یا خیر."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Discuss whether the high price tag is worth it for gaming."
                it[persianDescription] = "بحث کنید که آیا قیمت بالای آن برای بازی کردن می‌ارزد یا نه."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "App Brainstorming"
                it[persianTitle] = "ایده‌پردازی اپلیکیشن"
                it[description] = "You have a great idea for a new mobile app. Pitch it to a software developer, Ryan. Explain the core problem it solves, the target audience, and ask about technical feasibility."
                it[persianDescription] = "ایده عالی برای یک اپلیکیشن موبایل جدید دارید. آن را به رایان، توسعه‌دهنده نرم‌افزار، ارائه دهید. مشکل اصلی که حل می‌کند و مخاطبان هدف را توضیح دهید و درباره امکان‌سنجی فنی بپرسید."
                it[aiRole] = "Software Developer"
                it[aiName] = "Ryan"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/technology/app_idea.webp"
                it[points] = 24
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Pitch the core problem your app solves in one sentence."
                it[persianDescription] = "مشکل اصلی که اپلیکیشن شما حل می‌کند را در یک جمله ارائه دهید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Describe who the target audience is for this app."
                it[persianDescription] = "توصیف کنید که مخاطبان هدف این اپلیکیشن چه کسانی هستند."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Ask Ryan if the features you want are technically difficult to build."
                it[persianDescription] = "از رایان بپرسید که آیا ساخت ویژگی‌های مورد نظر شما از نظر فنی دشوار است."
                it[createdAt] = LocalDateTime.now()
            }
        }

        // ==========================================
        // 11. Travel & Adventure
        // ==========================================
        getCategoryId("Travel & Adventure")?.let { cId ->
            val s1 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Car Rental Inquiry"
                it[persianTitle] = "اجاره خودرو"
                it[description] = "You need to rent a car for a road trip. Talk to the rental agent, Paul. Discuss the car size you need, insurance coverage, and the fuel policy before booking."
                it[persianDescription] = "برای یک سفر جاده‌ای نیاز به اجاره خودرو دارید. با مسئول اجاره، پل، صحبت کنید. قبل از رزرو، در مورد اندازه خودرو، پوشش بیمه و سیاست سوخت بحث کنید."
                it[aiRole] = "Rental Agent"
                it[aiName] = "Paul"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_3.webp"
                it[imageUrl] = "scenario/travel/car_rental.webp"
                it[points] = 20
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Request a specific type of car (e.g., compact, SUV) based on your needs."
                it[persianDescription] = "بر اساس نیازتان درخواست یک نوع خودرو خاص (مثلاً جمع‌وجور، شاسی‌بلند) کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Ask what is covered by the basic insurance plan."
                it[persianDescription] = "بپرسید که طرح بیمه پایه چه مواردی را پوشش می‌دهد."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s1.value
                it[description] = "Confirm the fuel policy (e.g., full-to-full) and return time."
                it[persianDescription] = "سیاست سوخت (مثلاً تحویل با باک پر) و زمان بازگرداندن را تایید کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s2 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Booking a Boat Tour"
                it[persianTitle] = "رزرو تور قایق"
                it[description] = "You want to explore the coast by boat. Chat with the tour operator, Maria, to find out about the tour duration, what's included (like lunch), and where to meet."
                it[persianDescription] = "می‌خواهید ساحل را با قایق بگردید. با برگزارکننده تور، ماریا، گپ بزنید تا از مدت زمان تور، موارد شامل شده (مثل ناهار) و محل ملاقات مطلع شوید."
                it[aiRole] = "Tour Operator"
                it[aiName] = "Maria"
                it[gender] = Gender.Woman
                it[aiAvatar] = "avatar/ai_avatar_1.webp"
                it[imageUrl] = "scenario/travel/boat_tour.webp"
                it[points] = 16
                it[starter] = Role.User
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Ask how long the boat tour lasts and the route it takes."
                it[persianDescription] = "بپرسید تور قایق چقدر طول می‌کشد و چه مسیری را طی می‌کند."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Check if lunch or drinks are included in the ticket price."
                it[persianDescription] = "بررسی کنید که آیا ناهار یا نوشیدنی در قیمت بلیط لحاظ شده است."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s2.value
                it[description] = "Confirm the exact meeting point and time for departure."
                it[persianDescription] = "محل دقیق ملاقات و زمان حرکت را تایید کنید."
                it[createdAt] = LocalDateTime.now()
            }

            val s3 = ScenarioTable.insertAndGetId {
                it[categoryId] = cId.value
                it[title] = "Border Control"
                it[persianTitle] = "کنترل مرزی"
                it[description] = "You have just landed in a new country. Answering the immigration officer's questions clearly is crucial. State your purpose of visit, length of stay, and where you'll be staying."
                it[persianDescription] = "شما تازه وارد یک کشور جدید شده‌اید. پاسخ دادن واضح به سوالات افسر مهاجرت حیاتی است. هدف سفر، مدت اقامت و محل اقامت خود را بیان کنید."
                it[aiRole] = "Immigration Officer"
                it[aiName] = "Officer Jones"
                it[gender] = Gender.Man
                it[aiAvatar] = "avatar/ai_avatar_4.webp"
                it[imageUrl] = "scenario/travel/border_control.webp"
                it[points] = 25
                it[starter] = Role.Model
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Clearly state the purpose of your visit (tourism, business, etc.)."
                it[persianDescription] = "هدف سفر خود (گردشگری، کاری و غیره) را به وضوح بیان کنید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Tell the officer exactly how long you intend to stay."
                it[persianDescription] = "دقیقاً به افسر بگویید قصد دارید چه مدت بمانید."
                it[createdAt] = LocalDateTime.now()
            }
            ScenarioTaskTable.insert {
                it[scenarioId] = s3.value
                it[description] = "Provide the address of your hotel or accommodation."
                it[persianDescription] = "آدرس هتل یا محل اقامت خود را ارائه دهید."
                it[createdAt] = LocalDateTime.now()
            }
        }
    }
}