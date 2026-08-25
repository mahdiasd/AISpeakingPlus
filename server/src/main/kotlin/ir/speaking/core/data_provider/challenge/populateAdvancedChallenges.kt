package ir.speaking.core.data_provider.challenge

import ir.speaking.core.utils.now
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.challenge.challenge.db.ChallengeTable
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

fun populateAdvancedChallenges() {
    transaction {
        var currentStart = LocalDate.now()
        
        // ==========================================
        // Challenge 1: Crisis Management (Professional/Media)
        // ==========================================
        val challenge1End = currentStart.plusDays(5)
        val challenge1Id = ChallengeTable.insertAndGetId {
            it[title] = "Press Conference: Crisis Management"
            it[persianTitle] = "کنفرانس خبری: مدیریت بحران"
            it[description] =
                "The Challenge: You are the PR Director of a major tech company. A data breach has leaked user data. You must face a tough journalist, defend your company's reputation, admit fault transparently without inviting lawsuits, and outline immediate security upgrades."
            it[persianDescription] =
                "چالش: شما مدیر روابط عمومی یک شرکت بزرگ فناوری هستید. نشت اطلاعات باعث لو رفتن داده‌های کاربران شده است. باید با یک خبرنگار سرسخت روبرو شوید، از شهرت شرکت دفاع کنید، بدون ایجاد دردسر حقوقی به اشتباه اعتراف کنید و ارتقاهای امنیتی فوری را شرح دهید."
            it[aiRole] = "Investigative Journalist"
            it[aiName] = "Veronica"
            it[gender] = Gender.Woman
            it[points] = 80
            it[createdAt] = LocalDateTime.now()
            it[startDate] = currentStart.toKotlinLocalDate()
            it[endDate] = challenge1End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[imageUrl] = "challenge/crisis_management.webp" // نیاز به تصویر جدید
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Acknowledge the breach immediately but emphasize that sensitive financial data remained secure."
            it[persianDescription] = "فوراً به نشت اطلاعات اعتراف کنید اما تأکید کنید که داده‌های مالی حساس امن باقی مانده‌اند."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Deflect the hostile question about the CEO's resignation by pivoting to the new response team."
            it[persianDescription] = "سوال خصمانه درباره استعفای مدیرعامل را با تغییر بحث به سمت تیم واکنش جدید، منحرف کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Outline three concrete technical steps the company is taking to prevent recurrence."
            it[persianDescription] = "سه اقدام فنی مشخص که شرکت برای جلوگیری از تکرار این اتفاق انجام می‌دهد را شرح دهید."
            it[createdAt] = LocalDateTime.now()
        }
        
        ChallengeTaskTable.insert {
            it[challengeId] = challenge1Id.value
            it[description] = "Offer a sincere apology to the affected users and announce a compensation plan."
            it[persianDescription] = "صادقانه از کاربران آسیب‌دیده عذرخواهی کنید و یک طرح جبران خسارت را اعلام نمایید."
            it[createdAt] = LocalDateTime.now()
        }

        // ==========================================
        // Challenge 2: Civic Engagement (Town Hall Debate)
        // ==========================================
        // تنظیم زمان‌بندی (فرض بر ادامه منطق قبلی)
        val challenge2Start = currentStart.plusDays(6)
        val challenge2End = challenge2Start.plusDays(5)

        val challenge2Id = ChallengeTable.insertAndGetId {
            it[title] = "The Town Hall Debate"
            it[persianTitle] = "مناظره در تالار شهر"
            it[description] =
                "The Challenge: You are a resident attending a heated town hall meeting. The city council plans to demolish a historic community park to build a luxury shopping mall. You are speaking to Councilor Evans. Your goal is to dismantle their arguments logically, highlighting the long-term social and environmental costs over short-term economic gains."
            it[persianDescription] =
                "چالش: شما شهروندی هستید که در یک جلسه پرتنش شورای شهر شرکت کرده‌اید. شورا قصد دارد یک پارک تاریخی محلی را برای ساخت یک مرکز خرید لوکس تخریب کند. شما با عضو شورا، آقای ایوانز، صحبت می‌کنید. هدف شما این است که استدلال‌های آن‌ها را با منطق رد کنید و هزینه‌های بلندمدت اجتماعی و زیست‌محیطی را در برابر سود اقتصادی کوتاه‌مدت برجسته کنید."
            it[aiRole] = "City Councilor"
            it[aiName] = "Councilor Evans"
            it[gender] = Gender.Man
            it[points] = 55
            it[createdAt] = LocalDateTime.now()
            it[startDate] = challenge2Start.toKotlinLocalDate()
            it[endDate] = challenge2End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "challenge/town_hall_meeting.webp" // نیاز به تصویر مرتبط
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Acknowledge the city's need for revenue, but pivot firmly to the irreversible loss of urban green space."
            it[persianDescription] = "نیاز شهر به درآمد را تأیید کنید، اما بحث را قاطعانه به سمت از دست دادن غیرقابل‌بازگشت فضای سبز شهری تغییر دهید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Question the validity of the developer's environmental impact assessment, citing specific overlooked factors (e.g., air quality, noise pollution)."
            it[persianDescription] = "اعتبار ارزیابی زیست‌محیطی سازندگان را با ذکر فاکتورهای نادیده گرفته شده (مانند کیفیت هوا، آلودگی صوتی) زیر سوال ببرید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge2Id.value
            it[description] = "Propose a sustainable alternative solution, such as gentrifying abandoned warehouses instead of destroying the park."
            it[persianDescription] = "یک راه حل جایگزین پایدار پیشنهاد دهید، مانند نوسازی انبارهای متروکه به جای تخریب پارک."
            it[createdAt] = LocalDateTime.now()
        }


        // ==========================================
        // Challenge 3: Insurance Claim Dispute (Legal/Civic)
        // ==========================================
        currentStart = challenge2End.plusDays(1)
        val challenge3End = currentStart.plusDays(5)
        val challenge3Id = ChallengeTable.insertAndGetId {
            it[title] = "Disputing a Denied Claim"
            it[persianTitle] = "اعتراض به رد خسارت بیمه"
            it[description] =
                "The Challenge: Your car insurance claim was denied due to alleged 'negligence'. Call the claims adjuster, refute their findings with evidence, citing policy clauses, and threaten escalation if a fair settlement isn't reached."
            it[persianDescription] =
                "چالش: درخواست خسارت بیمه خودروی شما به دلیل «سهل‌انگاری» رد شده است. با ارزیاب خسارت تماس بگیرید، یافته‌های آن‌ها را با مدرک رد کنید، به بندهای بیمه‌نامه استناد کنید و تهدید کنید که در صورت عدم توافق منصفانه، موضوع را به مراجع بالاتر ارجاع می‌دهید."
            it[aiRole] = "Claims Adjuster"
            it[aiName] = "Mr. Vance"
            it[gender] = Gender.Man
            it[points] = 60
            it[createdAt] = LocalDateTime.now()
            it[startDate] = currentStart.toKotlinLocalDate()
            it[endDate] = challenge3End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_4.webp"
            it[imageUrl] = "challenge/insurance_dispute.webp" // نیاز به تصویر جدید
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Reference the specific claim number and state clearly that you are formally appealing the decision."
            it[persianDescription] = "به شماره پرونده خاص اشاره کنید و به وضوح اعلام کنید که رسماً به تصمیم اعتراض دارید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Present evidence (photos/police report) that contradicts the claim of negligence."
            it[persianDescription] = "شواهدی (عکس/گزارش پلیس) ارائه دهید که ادعای سهل‌انگاری را نقض می‌کند."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Cite the 'comprehensive coverage' clause of your policy to support your argument."
            it[persianDescription] = "برای حمایت از استدلال خود، به بند «پوشش کامل» بیمه‌نامه استناد کنید."
            it[createdAt] = LocalDateTime.now()
        }
        
        ChallengeTaskTable.insert {
            it[challengeId] = challenge3Id.value
            it[description] = "Firmly state that you will contact the Ombudsman if the decision is not reversed."
            it[persianDescription] = "قاطعانه بیان کنید که اگر تصمیم تغییر نکند، با بازرس ویژه تماس خواهید گرفت."
            it[createdAt] = LocalDateTime.now()
        }

        // ==========================================
        // Challenge 4: Executive Negotiation (Business)
        // ==========================================
        currentStart = challenge3End.plusDays(1)
        val challenge4End = currentStart.plusDays(5)
        val challenge4Id = ChallengeTable.insertAndGetId {
            it[title] = "Negotiating Executive Compensation"
            it[persianTitle] = "مذاکره حقوق و مزایای مدیریتی"
            it[description] =
                "The Challenge: You are being hired as a CTO. The base salary is fine, but you need to negotiate the equity package (stock options) and severance terms. Persuade the CEO that your long-term value justifies a higher stake in the company."
            it[persianDescription] =
                "چالش: شما به عنوان مدیر ارشد فنی (CTO) استخدام می‌شوید. حقوق پایه خوب است، اما باید در مورد بسته سهام (Stock Options) و شرایط پایان همکاری مذاکره کنید. مدیرعامل را متقاعد کنید که ارزش بلندمدت شما، سهم بیشتری از شرکت را توجیه می‌کند."
            it[aiRole] = "CEO"
            it[aiName] = "Elara"
            it[gender] = Gender.Woman
            it[points] = 65
            it[createdAt] = LocalDateTime.now()
            it[startDate] = currentStart.toKotlinLocalDate()
            it[endDate] = challenge4End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[imageUrl] = "challenge/executive_negotiation.webp" // نیاز به تصویر جدید
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Express enthusiasm for the vision but state that the current equity offer doesn't reflect market standards for a Series B startup."
            it[persianDescription] = "اشتیاق خود را برای چشم‌انداز شرکت ابراز کنید اما بگویید که پیشنهاد سهام فعلی با استانداردهای بازار برای یک استارتاپ سری B همخوانی ندارد."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Argue that your technical expertise will directly increase the company's valuation, justifying a higher percentage."
            it[persianDescription] = "استدلال کنید که تخصص فنی شما مستقیماً ارزش شرکت را افزایش می‌دهد و درصد بالاتری را توجیه می‌کند."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge4Id.value
            it[description] = "Negotiate specific terms for 'severance pay' in case of a change in control (acquisition)."
            it[persianDescription] = "در مورد شرایط خاص «حق سنوات» در صورت تغییر کنترل شرکت (تصاحب توسط شرکت دیگر) مذاکره کنید."
            it[createdAt] = LocalDateTime.now()
        }

        // ==========================================
        // Challenge 5: Conflict Mediation (Social/Psychology)
        // ==========================================
        currentStart = challenge4End.plusDays(1)
        val challenge5End = currentStart.plusDays(5)
        val challenge5Id = ChallengeTable.insertAndGetId {
            it[title] = "Mediating a Family Dispute"
            it[persianTitle] = "میانجی‌گری در اختلاف خانوادگی"
            it[description] =
                "The Challenge: Your siblings are fighting over selling your parents' old house. One wants to sell for money, the other wants to keep it for sentimental reasons. Act as the mediator to de-escalate emotions and propose a logical compromise."
            it[persianDescription] =
                "چالش: خواهر و برادر شما بر سر فروش خانه قدیمی والدینتان دعوا می‌کنند. یکی می‌خواهد برای پول بفروشد، دیگری به دلایل احساسی می‌خواهد آن را نگه دارد. به عنوان میانجی عمل کنید تا احساسات را فروکش کرده و یک مصالحه منطقی پیشنهاد دهید."
            it[aiRole] = "Emotional Sibling"
            it[aiName] = "Ben"
            it[gender] = Gender.Man
            it[points] = 45
            it[createdAt] = LocalDateTime.now()
            it[startDate] = currentStart.toKotlinLocalDate()
            it[endDate] = challenge5End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[imageUrl] = "challenge/family_mediation.webp" // نیاز به تصویر جدید
            it[starter] = Role.User
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Validate Ben's feelings about the memories attached to the house to lower his defensiveness."
            it[persianDescription] = "احساسات بن در مورد خاطرات وابسته به خانه را تایید کنید تا حالت تدافعی او را کاهش دهید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Gently introduce the reality of maintenance costs and taxes associated with keeping the property."
            it[persianDescription] = "به آرامی واقعیت هزینه‌های نگهداری و مالیات‌های مرتبط با حفظ ملک را مطرح کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge5Id.value
            it[description] = "Propose a creative solution (e.g., renting it out, or keeping it for 2 more years) that addresses both needs."
            it[persianDescription] = "یک راه حل خلاقانه (مانند اجاره دادن، یا نگه داشتن برای ۲ سال دیگر) پیشنهاد دهید که نیازهای هر دو طرف را برطرف کند."
            it[createdAt] = LocalDateTime.now()
        }

        // ==========================================
        // Challenge 6: Ethical Dilemma (Workplace)
        // ==========================================
        currentStart = challenge5End.plusDays(1)
        val challenge6End = currentStart.plusDays(5)
        val challenge6Id = ChallengeTable.insertAndGetId {
            it[title] = "Reporting Unethical Behavior"
            it[persianTitle] = "گزارش رفتار غیراخلاقی"
            it[description] =
                "The Challenge: You discovered your direct manager is inflating sales figures to get a bonus. You are meeting with the Ethics Officer. You need to report this delicately but firmly, ensuring you have protection against retaliation."
            it[persianDescription] =
                "چالش: متوجه شده‌اید که مدیر مستقیمتان ارقام فروش را برای دریافت پاداش بزرگ‌نمایی می‌کند. شما با مسئول اخلاق شرکت جلسه دارید. باید این موضوع را با ظرافت اما قاطعانه گزارش دهید و از حفاظت خود در برابر تلافی اطمینان حاصل کنید."
            it[aiRole] = "Ethics Officer"
            it[aiName] = "Ms. Kaling"
            it[gender] = Gender.Woman
            it[points] = 60
            it[createdAt] = LocalDateTime.now()
            it[startDate] = currentStart.toKotlinLocalDate()
            it[endDate] = challenge6End.toKotlinLocalDate()
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[imageUrl] = "challenge/ethics_report.webp" // نیاز به تصویر جدید
            it[starter] = Role.Model
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "State that you are coming forward with sensitive information and request strict confidentiality."
            it[persianDescription] = "اعلام کنید که برای ارائه اطلاعات حساس آمده‌اید و درخواست محرمانگی شدید دارید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Present the discrepancies you found in the sales report objectively, without making personal attacks."
            it[persianDescription] = "تناقضاتی که در گزارش فروش پیدا کرده‌اید را به صورت بی‌طرفانه و بدون حمله شخصی ارائه دهید."
            it[createdAt] = LocalDateTime.now()
        }

        ChallengeTaskTable.insert {
            it[challengeId] = challenge6Id.value
            it[description] = "Explicitly ask about the company's whistleblower protection policy regarding retaliation."
            it[persianDescription] = "صریحاً در مورد سیاست حفاظت از افشاگران شرکت در خصوص اقدامات تلافی‌جویانه سوال کنید."
            it[createdAt] = LocalDateTime.now()
        }
    }
}