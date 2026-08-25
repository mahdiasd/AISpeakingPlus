package ir.speaking.core.data_provider.challenge

import ir.speaking.core.utils.now
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

fun populateChallenges() {
    transaction {
        val now = java.time.LocalDate.now()

        val challenge1Start = now
        val challenge1End = challenge1Start.plusDays(5)
        val challenge1Id = ChallengeTable.insertAndGetId {
            it[title] = "Secure Your New Apartment"
            it[persianTitle] = "آپارتمان جدیدت را بگیر"
            it[description] =
                "The Challenge: You've found a promising apartment and are meeting the landlord. Your mission is to ask all the right questions, negotiate the best possible terms, and convince them you're the perfect tenant."
            it[persianDescription] =
                "چالش: یک آپارتمان عالی پیدا کرده‌ای و با صاحب‌خانه قرار ملاقات داری. مأموریت تو اینه که تمام سؤالات درست رو بپرسی، بر سر بهترین شرایط ممکن مذاکره کنی و او را متقاعد کنی که تو مستأجر ایده‌آل هستی."
            it[aiRole] = "Landlord"
            it[aiName] = "Margaret"
            it[gender] = Gender.Woman
            it[points] = 40
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge1Start.toKotlinLocalDate()
            it[endDate] = challenge1End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "challenge/apartment_viewing_and_rental.webp"
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Ask if utilities like water, electricity, and internet are included in the rent."
            it[persianDescription] = "بپرس که آیا هزینه‌های جانبی مثل آب، برق و اینترنت در اجاره لحاظ شده‌اند یا نه."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Inquire about the lease duration, security deposit, and the official move-in date."
            it[persianDescription] = "در مورد مدت قرارداد، مبلغ ودیعه و تاریخ دقیق تحویل آپارتمان سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Clarify the building's rules on pets, guests, and noise."
            it[persianDescription] = "قوانین ساختمان در مورد حیوانات خانگی، مهمان و سر و صدا را روشن کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Present yourself as a reliable tenant by describing your stable job and quiet lifestyle."
            it[persianDescription] = "با توصیف شغل ثابت و سبک زندگی آرومت، خودت را به عنوان یک مستأجر قابل اعتماد معرفی کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Attempt to negotiate a slightly lower rent or ask for an appliance upgrade before you agree to sign."
            it[persianDescription] = "تلاش کن تا اجاره را کمی پایین بیاوری یا قبل از امضای قرارداد، درخواست ارتقای یکی از وسایل خانه را بدهی."
            it[createdAt] = LocalDateTime.now()
        }

        val challenge2Start = challenge1End.plusDays(1)
        val challenge2End = challenge2Start.plusDays(5)
        val challenge2Id = ChallengeTable.insertAndGetId {
            it[title] = "Find Your Lost Pet"
            it[persianTitle] = "حیوان خانگی‌ات را پیدا کن"
            it[description] =
                "The Challenge: Your pet is missing, and your heart is racing. You need to call the local animal shelter, stay calm, and provide clear information to maximize your chances of being reunited."
            it[persianDescription] =
                "چالش: حیوان خانگی‌ات گم شده و قلبت تند می‌زنه. باید با پناهگاه حیوانات محلی تماس بگیری، آرامشت رو حفظ کنی و اطلاعات واضحی بدی تا شانس پیدا کردنش رو به حداکثر برسونی."
            it[aiRole] = "Animal Shelter Staff"
            it[aiName] = "Sarah"
            it[gender] = Gender.Woman
            it[points] = 35
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge2Start.toKotlinLocalDate()
            it[endDate] = challenge2End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "challenge/reporting_a_lost_pet.webp"
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Provide a detailed description of your pet: breed, color, size, and any unique markings."
            it[persianDescription] = "یک توصیف دقیق از حیوان خانگی‌ات بده: نژاد، رنگ، اندازه و هرگونه علامت منحصر به فرد."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "State exactly when and where you last saw your pet."
            it[persianDescription] = "دقیقاً بگو آخرین بار کی و کجا حیوان خانگی‌ات را دیدی."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Give your contact information and ask how the shelter notifies owners of found pets."
            it[persianDescription] = "اطلاعات تماست را بده و بپرس که پناهگاه چگونه به صاحبان حیوانات پیدا شده اطلاع می‌دهد."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Ask for their advice on making 'Missing Pet' flyers and the best places to post them."
            it[persianDescription] = "برای ساختن آگهی «حیوان گمشده» و بهترین مکان‌ها برای نصب آن، از آن‌ها راهنمایی بخواه."
            it[createdAt] = LocalDateTime.now()
        }

        val challenge3Start = challenge2End.plusDays(1)
        val challenge3End = challenge3Start.plusDays(5)
        val challenge3Id = ChallengeTable.insertAndGetId {
            it[title] = "Ship a Fragile Package Internationally"
            it[persianTitle] = "ارسال بسته شکستنی به خارج"
            it[description] =
                "The Challenge: You need to send a delicate gift overseas. Navigate the complexities of international shipping by talking to the postal clerk. Your goal is to find the best balance of speed, cost, and safety."
            it[persianDescription] =
                "چالش: باید یک هدیه ظریف و شکستنی را به خارج از کشور بفرستی. با صحبت کردن با کارمند پست، از پیچیدگی‌های ارسال بین‌المللی عبور کن. هدف تو پیدا کردن بهترین تعادل بین سرعت، هزینه و امنیت است."
            it[aiRole] = "Postal Clerk"
            it[aiName] = "David"
            it[gender] = Gender.Man
            it[points] = 40
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge3Start.toKotlinLocalDate()
            it[endDate] = challenge3End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "challenge/international_package_shipping.webp"
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Describe the item you're shipping and ask for the different delivery options and their estimated arrival times."
            it[persianDescription] = "بسته‌ای که می‌فرستی را توصیف کن و در مورد گزینه‌های مختلف ارسال و زمان تخمینی رسیدنشان بپرس."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Inquire about purchasing insurance and how to track the package."
            it[persianDescription] = "در مورد خرید بیمه و نحوه رهگیری بسته سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Discuss the customs declaration form and ask what information is required."
            it[persianDescription] = "در مورد فرم اظهارنامه گمرکی صحبت کن و بپرس چه اطلاعاتی لازم است."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Ask about the best way to package a fragile item to prevent damage."
            it[persianDescription] = "در مورد بهترین روش بسته‌بندی یک کالای شکستنی برای جلوگیری از آسیب دیدن، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Compare the final costs and choose the most suitable shipping method."
            it[persianDescription] = "هزینه‌های نهایی را مقایسه کن و مناسب‌ترین روش ارسال را انتخاب کن."
            it[createdAt] = LocalDateTime.now()
        }

        val challenge4Start = challenge3End.plusDays(1)
        val challenge4End = challenge4Start.plusDays(5)
        val challenge4Id = ChallengeTable.insertAndGetId {
            it[title] = "Plan the Perfect Group Trip"
            it[persianTitle] = "برنامه‌ریزی سفر گروهی عالی"
            it[description] =
                "The Challenge: It's time to plan a getaway with your friend, Alex! But you both have different ideas. Your mission is to discuss, negotiate, and agree on all the key details: destination, dates, budget, and activities."
            it[persianDescription] =
                "چالش: وقتشه که یک سفر با دوستت، الکس، برنامه‌ریزی کنی! اما هر دوتون ایده‌های متفاوتی دارید. مأموریت تو اینه که بحث کنی، مذاکره کنی و بر سر تمام جزئیات کلیدی توافق کنی: مقصد، تاریخ، بودجه و فعالیت‌ها."
            it[aiRole] = "Friend"
            it[aiName] = "Alex"
            it[gender] = Gender.Man
            it[points] = 35
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge4Start.toKotlinLocalDate()
            it[endDate] = challenge4End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "challenge/plan_group_trip.webp"
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Propose a destination and give two strong reasons why it would be a great trip."
            it[persianDescription] = "یک مقصد پیشنهاد بده و دو دلیل محکم بیار که چرا سفر خوبی خواهد بود."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Suggest travel dates and be ready to find a compromise that works for both of you."
            it[persianDescription] = "تاریخ‌های سفر را پیشنهاد بده و آماده باش تا به یک توافقی برسی که برای هر دوتان خوب باشد."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Discuss a realistic budget and agree on a daily spending limit."
            it[persianDescription] = "در مورد یک بودجه واقع‌بینانه صحبت کنید و بر سر سقف هزینه روزانه توافق کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Suggest two different activities (e.g., one relaxing, one adventurous) to do on the trip."
            it[persianDescription] = "دو فعالیت متفاوت (مثلاً یکی آرامش‌بخش، یکی ماجراجویانه) برای انجام دادن در طول سفر پیشنهاد بده."
            it[createdAt] = LocalDateTime.now()
        }

        val challenge5Start = challenge4End.plusDays(1)
        val challenge5End = challenge5Start.plusDays(5)
        val challenge5Id = ChallengeTable.insertAndGetId {
            it[title] = "Ace Your Visa Interview"
            it[persianTitle] = "در مصاحبه ویزا موفق شو"
            it[description] =
                "The Challenge: This is the final step—your visa interview. You're speaking with a consular officer. Answer every question clearly and confidently to prove your intent and secure your visa."
            it[persianDescription] =
                "چالش: این آخرین مرحله است—مصاحبه ویزای تو. در حال صحبت با یک افسر کنسولی هستی. به هر سؤال با وضوح و اعتماد به نفس پاسخ بده تا نیت خود را ثابت کرده و ویزایت را بگیری."
            it[aiRole] = "Consular Officer"
            it[aiName] = "Emily"
            it[gender] = Gender.Woman
            it[points] = 40
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge5Start.toKotlinLocalDate()
            it[endDate] = challenge5End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "challenge/visa_interview.webp"
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Clearly and concisely state the purpose of your travel."
            it[persianDescription] = "هدف سفرت را به طور واضح و مختصر بیان کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Confidently explain your financial situation and how you will fund your trip."
            it[persianDescription] = "با اعتماد به نفس وضعیت مالی و نحوه تأمین هزینه‌های سفرت را توضیح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Describe your strong ties to your home country, such as your job, family, or property."
            it[persianDescription] = "وابستگی‌های قوی خود به کشورت، مانند شغل، خانواده یا دارایی‌هایت را شرح بده."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Answer questions about your planned itinerary, including where you will stay and what you will do."
            it[persianDescription] = "به سؤالات مربوط به برنامه سفرت، شامل اینکه کجا اقامت خواهی داشت و چه کارهایی انجام خواهی داد، پاسخ بده."
            it[createdAt] = LocalDateTime.now()
        }

        val challenge6Start = challenge5End.plusDays(1)
        val challenge6End = challenge6Start.plusDays(5)
        val challenge6Id = ChallengeTable.insertAndGetId {
            it[title] = "Open a New Bank Account"
            it[persianTitle] = "افتتاح یک حساب بانکی جدید"
            it[description] =
                "The Challenge: You've just moved to a new area and need to open a bank account. Your task is to talk to the bank representative, understand your options, and complete the process successfully."
            it[persianDescription] =
                "چالش: به یک منطقه جدید نقل مکان کرده‌ای و نیاز به افتتاح حساب بانکی داری. وظیفه تو این است که با نماینده بانک صحبت کنی، گزینه‌هایت را بفهمی و فرآیند را با موفقیت به پایان برسانی."
            it[aiRole] = "Bank Representative"
            it[aiName] = "Michael"
            it[gender] = Gender.Man
            it[points] = 30
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge6Start.toKotlinLocalDate()
            it[endDate] = challenge6End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "challenge/open_bank_account.webp"
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Ask about the different types of checking and savings accounts they offer."
            it[persianDescription] = "در مورد انواع مختلف حساب‌های جاری و پس‌اندازی که ارائه می‌دهند، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Confirm which documents you need to provide, such as ID and proof of address."
            it[persianDescription] = "تأیید کن که چه مدارکی مانند کارت شناسایی و گواهی آدرس باید ارائه دهی."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Inquire about account fees, interest rates, and any minimum balance requirements."
            it[persianDescription] = "در مورد کارمزد حساب، نرخ سود و هرگونه شرط حداقل موجودی، سؤال کن."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Choose an account type and confirm you'd like to proceed with opening it."
            it[persianDescription] = "یک نوع حساب انتخاب کن و تأیید کن که می‌خواهی فرآیند افتتاح آن را ادامه دهی."
            it[createdAt] = LocalDateTime.now()
        }
    }
}