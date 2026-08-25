package ir.speaking.core.data_provider.word

import ir.speaking.core.utils.now
import ir.speaking.feature.word.word.DailyWordTable
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun populateDailyWords() {

    val startDate = java.time.LocalDateTime.now()
    var index = 0
    transaction {
        DailyWordTable.insert {
            it[this.word] = "Abandon"
            it[this.options] = listOf("خیرخواه", "موافقت کردن", "ترک کردن", "کوشا")
            it[this.answerIndex] = 2
            it[this.points] = 15
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Benevolent"
            it[this.options] = listOf("شاداب", "خیرخواه", "بدخواه", "ثروتمند")
            it[this.answerIndex] = 1
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Concur"
            it[this.options] = listOf("تکرار کردن", "موافقت کردن", "سردرگم شدن", "غر زدن")
            it[this.answerIndex] = 1
            it[this.points] = 16
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Diligent"
            it[this.options] = listOf("بی‌خیال", "کوشا", "بی‌دقت", "خسته")
            it[this.answerIndex] = 1
            it[this.points] = 17
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Eloquent"
            it[this.options] = listOf("بی‌کلام", "محدود", "فصیح", "تنبل")
            it[this.answerIndex] = 2
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Fluctuate"
            it[this.options] = listOf("دوست داشتن", "ثابت بودن", "نوسان داشتن", "تنفر داشتن")
            it[this.answerIndex] = 2
            it[this.points] = 17
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Gregarious"
            it[this.options] = listOf("خجالتی", "اجتماعی", "گمشده", "تنها")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Hypothetical"
            it[this.options] = listOf("فرضی", "قدیمی", "واقعی", "خطرناک")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Impartial"
            it[this.options] = listOf("پرخاشگر", "جانبدار", "بی‌طرف", "عجول")
            it[this.answerIndex] = 2
            it[this.points] = 17
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Juxtapose"
            it[this.options] = listOf("از هم جدا کردن", "در کنار هم قرار دادن", "تغییر دادن", "پنهان کردن")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Kindle"
            it[this.options] = listOf("روشن کردن", "خاموش کردن", "جمع کردن", "کم رنگ کردن")
            it[this.answerIndex] = 0
            it[this.points] = 16
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Lucid"
            it[this.options] = listOf("شفاف", "خواب‌آلود", "بی‌معنی", "سخت")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Mitigate"
            it[this.options] = listOf("شدید کردن", "کاهش دادن", "پنهان کردن", "تشویق کردن")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Nostalgia"
            it[this.options] = listOf("آینده‌نگری", "دلتنگی", "خوشحالی", "افسردگی")
            it[this.answerIndex] = 1
            it[this.points] = 15
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Obfuscate"
            it[this.options] = listOf("روشن کردن", "مبهم کردن", "تشویق کردن", "تسریع کردن")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Pragmatic"
            it[this.options] = listOf("احساسی", "عمل‌گرا", "ذهنی", "خیالی")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Quaint"
            it[this.options] = listOf("عجیب و جالب", "خسته‌کننده", "مدرن", "غیرقابل تحمل")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Resilient"
            it[this.options] = listOf("انعطاف‌پذیر", "ناامید", "سخت‌گیر", "خشن")
            it[this.answerIndex] = 0
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Scrutinize"
            it[this.options] = listOf("نظارت کردن", "موشکافی کردن", "رها کردن", "دوست داشتن")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Tenacious"
            it[this.options] = listOf("سرسخت", "بی‌تفاوت", "خونسرد", "شکست خورده")
            it[this.answerIndex] = 0
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ubiquitous"
            it[this.options] = listOf("کمیاب", "فراگیر", "قدیمی", "مخفی")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Vindicate"
            it[this.options] = listOf("تبرئه کردن", "محکوم کردن", "تحقیر کردن", "متهم کردن")
            it[this.answerIndex] = 0
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Wary"
            it[this.options] = listOf("بی‌احتیاط", "محتاط", "تنبل", "سریع")
            it[this.answerIndex] = 1
            it[this.points] = 17
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Zealous"
            it[this.options] = listOf("بی‌تفاوت", "پرحرارت", "کسل", "خونسرد")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ambiguous"
            it[this.options] = listOf("مبهم", "واضح", "کوتاه", "بلند")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Amiable"
            it[this.options] = listOf("خوش‌برخورد", "عصبانی", "بی‌احساس", "جدی")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Candid"
            it[this.options] = listOf("صادق", "دروغگو", "تنبل", "خجالتی")
            it[this.answerIndex] = 0
            it[this.points] = 17
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Debilitate"
            it[this.options] = listOf("تضعیف کردن", "تقویت کردن", "افزایش دادن", "افسرده کردن")
            it[this.answerIndex] = 0
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ephemeral"
            it[this.options] = listOf("دائمی", "زودگذر", "کامل", "مهم")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Facetious"
            it[this.options] = listOf("جدی", "شوخ‌طبع", "خسته‌کننده", "ساده‌لوح")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Meticulous"
            it[this.options] = listOf("بی‌دقت", "دقیق", "سرسری", "تنبل")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Aberration"
            it[this.options] = listOf("انحراف", "ثبات", "پیشرفت", "تکرار")
            it[this.answerIndex] = 0
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Capricious"
            it[this.options] = listOf("ثابت ‌قدم", "دمدمی", "منظم", "پیش ‌بینی‌ پذیر")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Debacle"
            it[this.options] = listOf("موفقیت", "فاجعه", "جشن", "پیشرفت")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ebullient"
            it[this.options] = listOf("پرشور", "غمگین", "بی‌تفاوت", "خسته")
            it[this.answerIndex] = 0
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Fastidious"
            it[this.options] = listOf("بی‌دقت", "وسواسی", "ساده", "بی‌تفاوت")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Garrulous"
            it[this.options] = listOf("کم‌حرف", "پرحرف", "خجالتی", "ساکت")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Harbinger"
            it[this.options] = listOf("پایان", "پیش‌درآمد", "دشمن", "دوست")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ineffable"
            it[this.options] = listOf("غیرقابل توصیف", "واضح", "ساده", "قابل درک")
            it[this.answerIndex] = 0
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Laconic"
            it[this.options] = listOf("پرحرف", "مختصر", "پیچیده", "طولانی")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Mellifluous"
            it[this.options] = listOf("خشن", "دلنشین", "ناهنجار", "ساکت")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Nebulous"
            it[this.options] = listOf("واضح", "مبهم", "سخت", "روشن")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Ostentatious"
            it[this.options] = listOf("ساده", "پرزرق و برق", "مخفی", "متواضع")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Pernicious"
            it[this.options] = listOf("مفید", "زیان‌بار", "بی‌ضرر", "کمک‌کننده")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Quixotic"
            it[this.options] = listOf("واقع‌بینانه", "خیال‌پردازانه", "عملی", "منطقی")
            it[this.answerIndex] = 1
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Recalcitrant"
            it[this.options] = listOf("سرکش", "مطیع", "آرام", "همکار")
            it[this.answerIndex] = 0
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Sycophant"
            it[this.options] = listOf("دوست واقعی", "چاپلوس", "منتقد", "رهبر")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Taciturn"
            it[this.options] = listOf("کم‌حرف", "پرحرف", "اجتماعی", "شوخ")
            it[this.answerIndex] = 0
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Veracious"
            it[this.options] = listOf("راستگو", "دروغگو", "مبهم", "خجالتی")
            it[this.answerIndex] = 0
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Winsome"
            it[this.options] = listOf("جذاب", "زشت", "بی‌تفاوت", "ترسناک")
            it[this.answerIndex] = 0
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Zenith"
            it[this.options] = listOf("اوج", "پایین", "شروع", "پایان")
            it[this.answerIndex] = 0
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Alacrity"
            it[this.options] = listOf("شتاب", "تنبلی", "تاخیر", "بی‌تفاوتی")
            it[this.answerIndex] = 0
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Belligerent"
            it[this.options] = listOf("صلح‌جو", "جنگجو", "دوستانه", "آرام")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Cacophony"
            it[this.options] = listOf("هماهنگی", "ناهنجاری صوتی", "سکوت", "ملودی")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Diffident"
            it[this.options] = listOf("با اعتماد به نفس", "کم‌رو", "پرجنب و جوش", "اجتماعی")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Enervate"
            it[this.options] = listOf("تقویت کردن", "تضعیف کردن", "انرژی دادن", "تحریک کردن")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Furtive"
            it[this.options] = listOf("آشکار", "پنهانی", "صریح", "شفاف")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Grandiloquent"
            it[this.options] = listOf("ساده", "پرطمطراق", "مختصر", "بی‌تکلف")
            it[this.answerIndex] = 1
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Intransigent"
            it[this.options] = listOf("انعطاف‌پذیر", "سرسخت", "مذاکره‌کننده", "سازش‌پذیر")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Juxtaposition"
            it[this.options] = listOf("جداسازی", "هم‌جواری", "ترکیب", "پنهان‌کاری")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Labyrinthine"
            it[this.options] = listOf("ساده", "پیچیده", "مستقیم", "واضح")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Magnanimous"
            it[this.options] = listOf("خسیس", "بخشنده", "خودخواه", "حسود")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Nefarious"
            it[this.options] = listOf("خیرخواه", "شرورانه", "بی‌ضرر", "مفید")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Obstreperous"
            it[this.options] = listOf("آرام", "سرکش", "مطیع", "سکوت‌کننده")
            it[this.answerIndex] = 1
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Panacea"
            it[this.options] = listOf("مشکل", "درمان همه دردها", "بیماری", "سم")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Quagmire"
            it[this.options] = listOf("باتلاق", "جاده صاف", "اوج", "پیروزی")
            it[this.answerIndex] = 0
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Reticent"
            it[this.options] = listOf("پرحرف", "کم‌حرف", "اجتماعی", "شوخ")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Salubrious"
            it[this.options] = listOf("ناسالم", "سالم", "خطرناک", "آلوده")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Tenuous"
            it[this.options] = listOf("محکم", "سست", "پایدار", "قوی")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Unctuous"
            it[this.options] = listOf("صادق", "چاپلوسانه", "خشن", "بی‌تفاوت")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Vicissitude"
            it[this.options] = listOf("ثبات", "دگرگونی", "پیروزی", "شکست")
            it[this.answerIndex] = 1
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Wistful"
            it[this.options] = listOf("شاد", "دلتنگ", "بی‌تفاوت", "عصبانی")
            it[this.answerIndex] = 1
            it[this.points] = 19
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Xenophobia"
            it[this.options] = listOf("ترس از خارجی‌ها", "عشق به سفر", "دوستی با غریبه‌ها", "بی‌تفاوتی فرهنگی")
            it[this.answerIndex] = 0
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Yoke"
            it[this.options] = listOf("جداسازی", "یوق (اتصال)", "آزادی", "پایان")
            it[this.answerIndex] = 1
            it[this.points] = 18
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Zephyr"
            it[this.options] = listOf("طوفان", "نسيم ملایم", "باران شدید", "گرمای سوزان")
            it[this.answerIndex] = 1
            it[this.points] = 20
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Acerbic"
            it[this.options] = listOf("شیرین", "تلخ و گزنده", "ملایم", "دوستانه")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Bombastic"
            it[this.options] = listOf("مختصر", "پرطمطراق", "ساده", "بی‌تکلف")
            it[this.answerIndex] = 1
            it[this.points] = 21
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Chicanery"
            it[this.options] = listOf("صداقت", "حقه‌بازی", "سادگی", "شفافیت")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Diaphanous"
            it[this.options] = listOf("ضخیم", "شفاف و نازک", "تاریک", "سخت")
            it[this.answerIndex] = 1
            it[this.points] = 24
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Esoteric"
            it[this.options] = listOf("عمومی", "پنهان و تخصصی", "ساده", "آشکار")
            it[this.answerIndex] = 1
            it[this.points] = 22
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }

        DailyWordTable.insert {
            it[this.word] = "Fecund"
            it[this.options] = listOf("نازا", "بارور", "خشک", "بی‌ثمر")
            it[this.answerIndex] = 1
            it[this.points] = 23
            it[this.createdAt] = LocalDateTime.now()
            it[this.showDate] = startDate.plusDays(index++.toLong()).toKotlinLocalDateTime()
        }
    }
}