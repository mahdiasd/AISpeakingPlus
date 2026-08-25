package ir.aispeaking.domain.fake_data

import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.challenge.Challenge
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.challenge.ChallengeTask
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.plan.Plan
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.model.purchase.PurchaseStatus
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.domain.model.scenario.ScenarioTask
import ir.aispeaking.domain.model.user.Gender
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.utils.MyUtils
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.Instant

object FakeData {
    fun provideCategories(): ImmutableList<Category> {
        return persistentListOf(
            Category(
                id = "1",
                name = "فناوری",
                imageUrl = "/static/category/technology.png",
                createdAt = Instant.parse("2023-01-15T10:00:00Z")
            ),
            Category(
                id = "2",
                name = "سلامتی",
                imageUrl = "/static/category/health.png",
                createdAt = Instant.parse("2023-02-20T14:30:00Z")
            ),
            Category(
                id = "3",
                name = "سفر",
                imageUrl = "/static/category/travel.png",
                createdAt = Instant.parse("2023-03-10T09:15:00Z")
            )
            // Add more categories as needed
            // ...
        )
    }


    fun providePurchases(): ImmutableList<Purchase> {
        return listOf(
            Purchase(
                id = "id",
                userId = "id",
                plan = providePlans().first(),
                discountCodeId = null,
                purchaseDate = Clock.System.now(),
                expiryDate = null,
                amountPaid = "100000",
                status = PurchaseStatus.SUCCESS,
                token = null,
                createdAt = Clock.System.now()
            )
        ).toImmutableList()
    }

    fun providePlans(): ImmutableList<Plan> {
        return listOf(
            Plan(
                id = MyUtils.generateStringUUID(),
                title = "خرید اشتراک یک ماهه",
                name = "برنزی",
                description = "1 month access",
                price = "45000",
                cafeBazaarId = "cbz_basic",
                discountedPrice = "30000",
                dayDuration = 1,
                createdAt = Instant.parse("2024-01-01T00:00:00Z")
            ),
            Plan(
                id = MyUtils.generateStringUUID(),
                name = "نقره ای",
                title = "سه ماهه",
                description = "1 month access",
                price = "135000",
                cafeBazaarId = "cbz_basic",
                discountedPrice = "100000",
                dayDuration = 1,
                createdAt = Instant.parse("2024-01-01T00:00:00Z")
            ),
            Plan(
                id = MyUtils.generateStringUUID(),
                title = "شش ماهه",
                name = "طلایی",
                description = "1 month access",
                price = "275000",
                cafeBazaarId = "cbz_basic",
                discountedPrice = "200000",
                dayDuration = 1,
                createdAt = Instant.parse("2024-01-01T00:00:00Z")
            ),


            ).toImmutableList()
    }

    fun provideUsers(): ImmutableList<User> {
        val firstNames =
            listOf("امیر", "رضا", "علی", "مهسا", "نرگس", "سارا", "محمد", "فاطمه", "زهرا", "میلاد")
        val lastNames = listOf(
            "احمدی",
            "محمدی",
            "جعفری",
            "رضایی",
            "کاظمی",
            "عباسی",
            "حسینی",
            "صمدی",
            "کریمی",
            "نوری"
        )
        val avatars = listOf("G-1", "G-2", "G-3", "G-4")
        val languageLevels = LanguageLevel.entries

        fun randomGender(): Gender? =
            if (Random.nextBoolean()) Gender.entries.toTypedArray().random() else null

        fun randomMobile(): String = "09" + List(9) { Random.nextInt(0, 10) }.joinToString("")
        fun randomScore(): Int = Random.nextInt(0, 1000)
        fun randomAge(): Int = Random.nextInt(18, 60)

        return List(5) {
            User(
                uid = "UID${it + 1}",
                nickName = "User${it + 1}",
                firstName = firstNames.random(),
                lastName = lastNames.random(),
                mobile = randomMobile(),
                gender = randomGender(),
                languageLevel = languageLevels.random(),
                age = randomAge(),
                score = randomScore(),
                avatar = avatars.random()
            )
        }.toImmutableList()
    }

    fun provideTasks(): ImmutableList<ScenarioTask> {
        return listOf(
            ScenarioTask(
                id = "meliore",
                scenarioId = "id",
                description = "oratio",
                createdAt = null,
                persianDescription = "",
                finished = false
            )
        ).toImmutableList()
    }

    fun provideScenarios(): ImmutableList<Scenario> {
        return listOf(
            Scenario(
                id = "1",
                tasks = listOf<ScenarioTask>().toImmutableList(),
                categoryId = "health",
                title = "مدیریت استرس در محل کار",
                description = "در این سناریو، شما یاد می‌گیرید چگونه با استرس محیط کار مقابله کنید و بهره‌وری خود را افزایش دهید.",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiName = "Sara",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 100,
                createdAt = Clock.System.now(),
                persianTitle = "",
                persianDescription = "",
                aiGender = Gender.Man,
                starter = Role.User,
                isChallenge = false
            ),
            Scenario(
                id = "2",
                tasks = listOf<ScenarioTask>().toImmutableList(),
                categoryId = "finance",
                title = "مدیریت هوشمند سرمایه",
                description = "آموزش اصول اولیه سرمایه‌گذاری و مدیریت مالی شخصی برای افزایش درآمد و پس‌انداز.",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiName = "استاد محمدی",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 150,
                persianTitle = "",
                persianDescription = "",
                aiGender = Gender.Man,
                starter = Role.User,
                isChallenge = false,
                createdAt = Clock.System.now()
            ),
            Scenario(
                id = "3",
                tasks = listOf<ScenarioTask>().toImmutableList(),
                categoryId = "education",
                title = "تکنیک‌های مطالعه مؤثر",
                description = "روش‌های کاربردی برای افزایش تمرکز و یادگیری بهتر در زمان مطالعه و آمادگی برای امتحانات.",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiName = "مشاور رضایی",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 120,
                persianTitle = "",
                persianDescription = "",
                aiGender = Gender.Man,
                starter = Role.User,
                isChallenge = false,
                createdAt = Clock.System.now()
            ),
            Scenario(
                id = "4",
                tasks = listOf<ScenarioTask>().toImmutableList(),
                categoryId = "lifestyle",
                title = "برنامه‌ریزی روزانه موفق",
                description = "اصول مدیریت زمان و برنامه‌ریزی روزانه برای دستیابی به اهداف شخصی و حرفه‌ای.",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiName = "مربی نیکخواه",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 80,
                persianTitle = "",
                persianDescription = "",
                aiGender = Gender.Man,
                starter = Role.User,
                isChallenge = false,
                createdAt = Clock.System.now()
            ),
            Scenario(
                id = "5",
                tasks = listOf<ScenarioTask>().toImmutableList(),
                categoryId = "health",
                title = "تغذیه سالم در محل کار",
                description = "راهنمای عملی برای داشتن تغذیه سالم و متعادل در محیط کار و افزایش سطح انرژی روزانه.",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiName = "دکتر احمدی",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 90,
                persianTitle = "",
                persianDescription = "",
                aiGender = Gender.Man,
                starter = Role.User,
                isChallenge = false,
                createdAt = Clock.System.now()
            )
        ).toImmutableList()
    }

    fun provideScenarioSummaries(): ImmutableList<ScenarioSummary> {
        return listOf(
            ScenarioSummary(
                id = "5",
                title = "تغذیه سالم در محل کار",
                imageUrl =  "/static/avatar/ai_avatar_1.png",
                aiAvatar =  "/static/avatar/ai_avatar_1.png",
                score = 15
            )
        ).toImmutableList()
    }

    fun provideChallengeSummary(): ChallengeSummary {
        return ChallengeSummary(
            id = "placerat",
            title = "mi",
            imageUrl = null,
            description = "diam",
            aiAvatar = null,
            score = 5274
        )
    }

    fun provideScenarioDetail(): ScenarioDetail {
        return ScenarioDetail(
            scenario = provideScenarios().first(),
            userHaveSubscription = true,
            progress = null
        )
    }

    fun provideUserChats(): ImmutableList<Chat.User> {
        return listOf(
            Chat.User(
                message = "recommend options based on budget and location, and help with the booking process",
                status = ChatStatus.Sending
            ),
            Chat.User(
                message = "Hi",
                status = ChatStatus.Answered("")
            ),
            Chat.User(
                message = "recommend options based on budget and location, and help with the booking process",
                status = ChatStatus.Answered("grammar is not ok")
            ),
            Chat.User(
                message = "recommend options based on budget and location, and help with the booking process",
                status = ChatStatus.Failed
            )
        ).toImmutableList()
    }

    fun provideAiChats(): ImmutableList<Chat.Ai> {
        return listOf(
            Chat.Ai(
                message = "Synthesizes speech from text for immediate playback or to create a sound file.\n" +
                        "TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE in the queries elements of their manifest",
                translatedMessage = "",
                voiceState = AiVoiceState.Stopped,
                finishTaskIndexes = listOf<Int>().toImmutableList()
            ),
            Chat.Ai(
                message = "Hello",
                translatedMessage = "",
                voiceState = AiVoiceState.Stopped,
                finishTaskIndexes = listOf<Int>().toImmutableList()
            ),
        ).toImmutableList()
    }

    fun provideUsersSummary(): ImmutableList<UserSummary> {
        return listOf(
            UserSummary(
                uid = MyUtils.generateStringUUID(),
                score = 4406,
                nickName = "Laken",
                avatar = "B-2"
            ),
            UserSummary(
                uid = MyUtils.generateStringUUID(),
                score = 4406,
                nickName = "Laken",
                avatar = "G-2"
            ),
            UserSummary(
                uid = MyUtils.generateStringUUID(),
                score = 4406,
                nickName = "Laken",
                avatar = "G-3"
            ),
            UserSummary(
                uid = MyUtils.generateStringUUID(),
                score = 4406,
                nickName = "Laken",
                avatar = "B-1"
            ),
        ).toImmutableList()
    }

    fun provideDailyWord(): DailyWord {
        return DailyWord(
            uid = MyUtils.generateStringUUID(),
            word = "Hello",
            options = listOf("سلام", "غم", "چطوری", "خداحافظ").toImmutableList(),
            answerIndex = 0,
            createdAt = Clock.System.now().toString(),
            expiredAt = Clock.System.now().toString(),
            wordProgress = null,
            points = 10
        )
    }

    fun provideChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                uid = "challenge1",
                title = "Learn Kotlin Basics",
                description = "Master the fundamentals of Kotlin programming language.",
                imageUrl = "https://example.com/kotlin.png",
                aiName = "KotlinBot",
                aiAvatar = "https://example.com/ai_avatar_kotlin.png",
                points = 100,
                createdAt = Clock.System.now(),
                tasks = listOf(
                    ChallengeTask(
                        id = "task1",
                        description = "Understand Kotlin syntax and variables.",
                        persianDescription = ""
                    ),
                    ChallengeTask(
                        id = "task2",
                        description = "Learn about Kotlin functions and lambdas.",
                        persianDescription = ""
                    )
                )
            ),
            Challenge(
                uid = "challenge2",
                title = "Jetpack Compose Mastery",
                description = "Become proficient in building UIs with Jetpack Compose.",
                imageUrl = "https://example.com/jetpack_compose.png",
                aiName = "ComposeAI",
                aiAvatar = "https://example.com/ai_avatar_compose.png",
                points = 200,
                createdAt = Clock.System.now(),
                tasks = listOf(
                    ChallengeTask(
                        id = "task3",
                        description = "Build your first composable function.",
                        persianDescription = ""
                    ),
                    ChallengeTask(
                        id = "task4",
                        description = "Learn state management in Jetpack Compose.",
                        persianDescription = ""
                    )
                )
            ),
            Challenge(
                uid = "challenge3",
                title = "Android App Development",
                description = "Develop your first Android app using Kotlin and Jetpack libraries.",
                imageUrl = null,
                aiName = "AndroidAssistant",
                aiAvatar = null,
                points = 300,
                createdAt = Clock.System.now(),
                tasks = listOf(
                    ChallengeTask(
                        id = "task5",
                        description = "Set up your Android development environment.",
                        persianDescription = ""
                    ),
                    ChallengeTask(
                        id = "task6",
                        description = "Create a simple to-do app.",
                        persianDescription = ""
                    )
                )
            )
        )
    }

}