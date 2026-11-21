# سند طراحی فنی (Design): دیکشنری هوشمند متنی و جعبه لایتنر با تکرار فاصله‌دار

## زمینه و انگیزه (Context)

برای مشاهدهٔ دلایل محصولی و زمینهٔ شکل‌گیری این تغییر، به [proposal-fa.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/openspec/changes/add-leitner-and-dictionary/proposal-fa.md) مراجعه کنید.  
کلاینت پروژه یک اپلیکیشن چندسکویی بر پایهٔ **Kotlin Multiplatform (KMP)** و **Compose Multiplatform** است که پلتفرم‌های اندروید، iOS، دسکتاپ و وب (Wasm/JS) را هدف قرار می‌دهد.  
بک‌اند با فریم‌ورک **Ktor 3**، پایگاه‌دادهٔ **PostgreSQL** (با ابزار Exposed ORM)، ردیس **Redis**، موتور صوتی **Kokoro TTS**، و **9router** (مسیریاب سازگار با OpenAI برای مدل‌های زبانی روی پورت ۲۰۱۲۸) توسعه داده شده است.

---

## اهداف و غیر‌اهداف (Goals / Non-Goals)

**اهداف (Goals):**
- ارائهٔ سرویس جستجوی سریع واژه و عبارت در داخل چت با زمان پاسخ‌دهی زیر ۱۰۰ میلی‌ثانیه برای موارد کش‌شده.
- پیاده‌سازی شیت انتخاب کلمه مبتنی بر توکن برای رهایی از مشکلات و خطاهای انتخاب متن با هندل‌های لمسی سیستم‌عامل.
- ذخیرهٔ واژگان در یک کش لکسیکون مشترک سراسری (`lexicon_entries`) و اتصال آن به فلش‌کارت‌های اختصاصی هر کاربر (`user_flashcards`).
- ارائهٔ رابط کاربری لمسی و روان ۵ جعبه‌ای برای مرور لایتنر با استفادهٔ مجدد از فیزیک کروسل سه‌بعدی Coverflow برگرفته از `SubscriptionScreen`.
- پشتیبانی از نوتیفیکیشن‌های روزانه با استپر عددی اختصاصی بدون نیاز به باز شدن کیبورد مجازی.

**غیر‌اهداف (Non-Goals):**
- استنتاج آفلاین مدل‌های زبانی در کلاینت (تمام تحلیل‌های LLM در سمت سرور و با 9router انجام می‌شود).
- فرمول‌های پیچیده و دشوار مانند SM-2 سوپرممو یا FSRS انکی (در این فاز از فواصل زمانی استاندارد و سرراست ۵ جعبه‌ای استفاده می‌شود).
- ارزیابی کامل تلفظ و گفتار (STT) در این مرحله از لایتنر (صوت فعلاً فقط برای شنیدن و تقویت لیسنینگ است).

---

## تصمیمات معماری (Decisions)

### ۱. شیت انتخاب توکن واژه‌ها در برابر هندل‌های بومی انتخاب متن
- **تصمیم**: متن پیام به توکن‌های جداشده با فاصله تقسیم شود و در قالب چیپ‌های تعاملی و قابل‌لمس نمایش یابد (`WordTokenSelectorSheet`).
- **دلیل انتخاب**: انتخاب دقیق متن با انگشت روی صفحات لمسی یا در بستر وب کانواس/Wasm کار دشواری است؛ به‌خصوص زمانی که کاربر می‌خواهد یک عبارت ۲ کلمه‌ای را برگزیند. چیپ‌های توکن‌شده انتخابی آنی، دقیق و بدون خطا با هایلایت بصری آبی فیروزه‌ای (Serene Cyan) ایجاد می‌کنند.
- **گزینه‌های رد شده**:
  - *استفاده از `SelectionContainer` بومی*: با اسکرول والد و حرکات لمسی تداخل پیدا می‌کرد و در پلتفرم‌های مختلف رفتار یکسانی نداشت.
  - *قراردادن اسپن‌های کلیک‌پذیر درون متن حباب چت*: رندرینگ حباب چت را سنگین می‌کرد و حین اسکرول معمولی باعث لمس ناخواستهٔ کلمات می‌شد.

### ۲. معماری کش دو سطحی واژگان و یکپارچگی با 9router
- **تصمیم**: ایجاد جدول مشترک `lexicon_entries` در دیتابیس PostgreSQL به همراه کش کلید-مقداری در Redis بر اساس متن نرمال‌شده. فقط در صورتی که کلمه در کش نباشد، درخواست به کامبوی دیکشنری 9router ارسال می‌شود.
- **دلیل انتخاب**: واژگان پرکاربردی مثل "withdraw" یا "explore" توسط کاربران متعددی جستجو می‌شوند. کش مشترک، زمان پاسخ‌دهی را از حدود ۱۲۰۰ میلی‌ثانیه به کمتر از ۵۰ میلی‌ثانیه کاهش داده و در مصرف توکن‌های هوش مصنوعی صرفه‌جویی چشمگیری می‌کند.
- **قرارداد داده‌ای و پرامپت JSON برای 9router**:
  ```json
  {
    "headword": "withdraw",
    "phonetic": "/wɪðˈdrɔː/",
    "partOfSpeech": "verb",
    "contextMeaningFa": "برداشت کردن (پول)",
    "definitionEn": "to take money out of an account",
    "otherMeaningsFa": ["عقب‌نشینی کردن", "صرف‌نظر کردن"],
    "sampleSentenceEn": "I need to withdraw some cash from the ATM.",
    "sampleSentenceFa": "باید مقداری پول نقد از دستگاه خودپرداز برداشت کنم."
  }
  ```
- **گزینه‌های رد شده**: فراخوانی مستقیم هوش مصنوعی در هر بار جستجو (به دلیل تاخیر بالا و هزینه‌های اضافه رد شد).

### ۳. سنتز خودکار صدا با Kokoro TTS
- **تصمیم**: در اولین جستجوی هر کلمه، بک‌اند Ktor سرویس `TtsService` (موتور Kokoro) را صدا می‌زند تا فایل صوتی تولید و در مسیر `audio_cache/words/{lemma}.mp3` ذخیره شود و یک آدرس پایدار بازگردانده شود.
- **دلیل انتخاب**: بار پردازشی تولید مکرر صوت حذف می‌شود و پخش آنی بدون تاخیر تضمین می‌گردد.

### ۴. روند پیشرفت فواصل تکرار (۵ جعبه با پنالتی یک پله بازگشت)
- **تصمیم**:
  - جعبه ۱: روز اول (مرور هر روزه)
  - جعبه ۲: روز سوم (فاصله ۳ روز)
  - جعبه ۳: روز هفتم (فاصله ۷ روز)
  - جعبه ۴: روز چهاردهم (فاصله ۱۴ روز)
  - جعبه ۵: روز سی‌ام (تسلط کامل)
  - **جریمهٔ فراموشی**: کارت دقیقاً ۱ پله تنزل می‌یابد (مثلاً جعبه ۴ به جعبه ۳ بازمی‌گردد؛ کارت‌های جعبه ۱ در همان جعبه می‌مانند).
- **دلیل انتخاب**: تنبیه سخت‌گیرانه و بازگرداندن کارت به جعبهٔ اول انگیزهٔ زبان‌آموز را به شدت کاهش می‌دهد. کسر یک پله باعث حفظ نظم و تشویق به تمرین مداوم می‌شود.

### ۵. معماری رابط کاربری: نمای گرید + پیجر سه‌بعدی Coverflow
- **تصمیم**: استفادهٔ مجدد از فرمول‌های پرسپکتیو سه‌بعدی Coverflow موجود در کامپوننت `ModernPlanCard` و صفحهٔ `SubscriptionScreen`:
  ```kotlin
  val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
  val scale = 1f - (pageOffset.absoluteValue.coerceIn(0f, 1.5f) * 0.08f)
  val alpha = 1f - (pageOffset.absoluteValue.coerceIn(0f, 1.5f) * 0.18f).coerceIn(0f, 0.28f)
  val rotationY = pageOffset * 7.5f
  ```
- **دلیل انتخاب**: هماهنگی کامل با زبان طراحی شیشه‌ای سیال اپلیکیشن (Liquid Glass) و تجربهٔ لمسی روان.

### ۶. ساختار جداول دیتابیس (با فریم‌ورک Exposed ORM)
```kotlin
object LexiconTable : UUIDTable("lexicon_entries") {
    val normalizedText = varchar("normalized_text", 255).uniqueIndex()
    val headword = varchar("headword", 128)
    val phonetic = varchar("phonetic", 128).nullable()
    val partOfSpeech = varchar("part_of_speech", 64).nullable()
    val contextMeaningFa = text("context_meaning_fa")
    val otherMeanings = text("other_meanings_json").nullable()
    val definitionEn = text("definition_en").nullable()
    val sampleSentenceEn = text("sample_sentence_en").nullable()
    val sampleSentenceFa = text("sample_sentence_fa").nullable()
    val audioUrl = varchar("audio_url", 512).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object UserFlashcardTable : UUIDTable("user_flashcards") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val lexiconId = reference("lexicon_id", LexiconTable, onDelete = ReferenceOption.CASCADE)
    val boxNumber = integer("box_number").default(1)
    val reviewCount = integer("review_count").default(0)
    val lastReviewedAt = timestamp("last_reviewed_at").nullable()
    val nextReviewAt = timestamp("next_review_at")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
```

---

## ریسک‌ها و راهکارهای تعدیل (Risks / Trade-offs)

- **[ریسک] چندمعنایی و ابهام معنایی کلمات در بافت متن**: اگر کلمهٔ "bank" در یک گفتگوی مالی به عنوان «بانک» کش شود، کاربر دیگری که بعداً عبارت "river bank" (ساحل رودخانه) را بزند ممکن است معنای نامرتبط ببیند.
  - *راهکار تعدیل*: در بافت‌های حساس، لکسیکون می‌تواند با ترکیب `normalized_text + context_hash` ایندکس شود؛ همچنین رابط کاربری می‌تواند همهٔ معانی را بیاورد و معنای مرتبط با بافت فعلی را هایلایت کند.
- **[ریسک] خطای تجزیه JSON خروجی هوش مصنوعی از 9router**: مدل ممکن است تگ‌های مارک‌داون ````json ... ```` یا ساختار ناصحیح تولید کند.
  - *راهکار تعدیل*: استفاده از پاک‌سازی ملایم رشته‌ها قبل از `json.decodeFromString` برای حذف تگ‌های کد و داشتن فال‌بک امن برای تعاریف عمومی بدون کرش برنامه.
- **[ریسک] انباشت انبوه کارت‌های معوقه و دلزدگی کاربر پس از غیبت طولانی**:
  - *راهکار تعدیل*: سقف‌گذاری برای هر نوبت مرور (مثلاً دسته‌های حداکثر ۲۰ تایی از کارت‌هایی که بیشترین تاخیر را دارند).

---

## برنامه استقرار و مهاجرت (Migration Plan)

۱. **مهاجرت دیتابیس (DB Migration)**: اجرای مایگریشن SQL برای ساخت جداول `lexicon_entries` و `user_flashcards` همراه با ایندکس‌های مناسب.  
۲. **دیپلوی بک‌اند (Server Deployment)**: استقرار نسخهٔ جدید سرور Ktor شامل اندپوینت‌های `/api/dictionary` و `/api/leitner` و اعتبارسنجی کش صوتی Kokoro روی سرور.  
۳. **دیپلوی کلاینت (Client Deployment)**: انتشار به‌روزرسانی KMP شامل `WordTokenSelectorSheet`، صفحهٔ `LeitnerScreen` و زبانهٔ شناور نقشه مسیر.
