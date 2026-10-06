# برنامه اقدام: پاکسازی کدهای قدیمی و حفظ زیرساخت‌های پایه (AISpeakingPlus)

## هدف
پاکسازی ایمن کدهای فیچرهای قدیمی در هر دو بخش **سرور (Ktor)** و **کلاینت (Compose Multiplatform)**، همراه با نگهداری کامل موارد زیر:
۱. اسکریپت‌های بیلد گریدل، پلتفرم‌های خروجی و نسخه‌های کتابخانه‌ها.
۲. زیرساخت‌های هسته سرور (تنظیمات پایگاه داده PostgreSQL/Exposed، ردیس Redisson، کانفیگ روتینگ، پلاگین‌های Ktor، امنیت JWT و ماژول‌های بومی STT/TTS).
۳. زیرساخت‌های مشترک کلاینت (کلاینت شبکه Ktor، ذخیره‌سازی چندسکویی Multiplatform Settings، سیستم طراحی و تم پایه).

این پاکسازی به عامل هوش مصنوعی (Agent) در گفتگوی جدید امکان می‌دهد تا بلافاصله بر مبنای این وضعیت تمیز، دستور `/speckit-plan` را اجرا کرده و معماری جدید ماجراجویی داستانی را بدون تداخل و خطاهای بیلد پیاده‌سازی نماید.

---

## بخش‌هایی که باید حفظ شوند (نباید حذف شوند)

### ۱. تنظیمات بیلد، پلتفرم‌ها و کتابخانه‌ها
- فایل‌های ریشه: `build.gradle.kts`، `settings.gradle.kts`، `gradle.properties`، `local.properties`.
- وابستگی‌ها و نگارش‌ها: `gradle/libs.versions.toml`، پوشه `gradle/wrapper/*`، `gradlew` و `gradlew.bat`.
- فایل‌های بیلد ماژول‌های حفظ‌شده.

### ۲. زیرساخت هسته سرور (`server/src/main/kotlin/ir/speaking/core/`)
- اتصال دیتابیس و مدیریت تراکنش‌ها (`core/database/`).
- تنظیمات کلاینت ردیس (`core/redis/`).
- پلاگین‌های Ktor:
  * سریالایزر داده‌ها (`configureSerialization`)
  * احراز هویت و اعتبارسنجی JWT (`configureSecurity`)
  * تنظیمات CORS و وب‌سوکت‌ها
  * فیلتر مدیریت خطاها (StatusPages)
- لایه‌های بومی پردازش صوت (موتورهای `Sherpa-ONNX` و `Kokoro` در ماژول‌های `stt` و `tts`).
- اسکریپت‌های دپلویمنت و تنظیمات داکر (`Dockerfile`، `docker-compose.yml` و پیکربندی Nginx).

### ۳. زیرساخت کلاینت و راه‌اندازهای پلتفرمی
- راه‌انداز اندروید: `androidApp/` (اکتیویتی اصلی و Manifest).
- راه‌انداز دسکتاپ: `desktopApp/` (نقطه ورود Main.kt و کانفیگ‌ها).
- راه‌انداز وب: `webApp/` (پیکربندی Wasm/JS).
- پوسته برنامه iOS: `iosApp/`.
- ماژول‌های پایه‌ای مشترک:
  * `network/`: کلاینت Ktor Client، سریالایزر، لاگر و مدیریت توکن‌ها.
  * `storage/`: سیستم ذخیره‌سازی محلی کلید-مقدار چندسکویی.
  * `utils/`: ابزارهای کمکی و دیسپچرهای کوروتین.
  * `sharedUI/`: سیستم تایپوگرافی، تم، رنگ‌بندی و کامپوننت‌های پایه مشترک.

### ۴. اسناد و مشخصات تولیدشده
- پوشه `.specify/` (قانون اساسی، ارزیابی‌ها، اسناد و پلن‌ها).
- پوشه `specs/001-story-based-speaking-journey/` (مشخصات و چک‌لیست‌ها در هر دو نسخه فارسی و انگلیسی).
- اسناد معماری مرجع (`APP_SPECIFICATION_V2.md`، `TECHNICAL_SPECIFICATION.md`، `DATABASE_SCHEMA.md`).

---

## بخش‌هایی که باید پاکسازی و حذف شوند

### ۱. دامنه‌ها و فیچرهای قدیمی سرور (`server/src/main/kotlin/ir/speaking/feature/`)
حذف روت‌ها، سرویس‌ها و جداول سناریوهای نسخه پیشین که با مراحل داستانی جدید جایگزین می‌شوند:
- `feature/scenario/` و `feature/scenario_detail/` (سناریوهای قدیمی)
- `feature/category/` (دسته‌بندی‌های قبلی)
- `feature/challenge/` و `feature/home/` (داشبورد و چالش‌های گذشته)
- `feature/competition/` (بخش رقابت قدیمی)
- `feature/lightener/` و `feature/word/` (جعبه لایتنر قبلی)
- `feature/plan/` و `feature/purchase/` (مدل‌های پرداخت گذشته — جهت جایگزینی با مدل سه‌سطحی جدید)
- پاکسازی روت‌های فراخوانی‌شده قدیمی در `Routing.kt` و ماژول‌های Koin.

### ۲. ماژول‌های فیچرهای قدیمی کلاینت (`feature/*`)
حذف یا تخلیه ماژول‌های فیچرهای واسط کاربری قبلی:
- `feature/scenarios/` و `feature/scenario_detail/`
- `feature/lightener/`
- `feature/competition/`
- `feature/english_level/` (تعیین سطح آزمایشی قدیمی)
- `feature/search/`
- `feature/roadmap/` (جهت جایگزینی با نقشه ماجراجویی داستانی ۲ بعدی)
- اصلاح خطوط `include(":feature:...")` در `settings.gradle.kts`.

### ۳. پیاده‌سازی‌های دامین و دیتای قدیمی کلاینت (`domain/src/` و `data/src/`)
- پاکسازی Use Caseها، ریپازیتوری‌ها و DTOهای سناریوها و لایتنر قبلی با حفظ ساختار پکیج‌های ریشه (`ir.speaking.domain.*` و `ir.speaking.data.*`).

---

## گام‌های اجرایی برای Agent در چت جدید

۱. **گام اول: ایجاد شاخه تمیز در گیت**
   - اطمینان از وضعیت گیت و ساخت شاخه کاری جدید:
     ```bash
     git checkout -b chore/codebase-clean-slate
     ```

۲. **گام دوم: پاکسازی فیچرهای سرور**
   - حذف پوشه‌های فیچرهای مازاد در سرور (`scenario`, `category`, `challenge`, `home`, `competition`, `lightener`, `word`).
   - ساده‌سازی روت‌های `Routing.kt` و ماژول‌های `di/`.
   - اطمینان از کامپایل بدون خطای سرور:
     ```bash
     ./gradlew :server:compileKotlin
     ```

۳. **گام سوم: پاکسازی فیچرهای کلاینت**
   - حذف ماژول‌های اضافی کلاینت و به‌روزرسانی `settings.gradle.kts`.
   - پاکسازی کدهای اضافی در لایه‌های `domain` و `data`.
   - تست کامپایل ماژول‌های مشترک:
     ```bash
     ./gradlew :domain:compileKotlin :data:compileKotlin :sharedUI:compileKotlin
     ```

۴. **گام چهارم: تأیید نهایی و کامیت**
   - اطمینان از عدم وجود خطای ایمپورت و اجرای بیلد آزمایشی.
   - ثبت کامیت تغییرات:
     ```bash
     git commit -m "chore: purge legacy features, retain build and core infrastructure"
     ```
   - اعلام آمادگی کامل برای اجرای `/speckit-plan` برای فیچر `001-story-based-speaking-journey`.
