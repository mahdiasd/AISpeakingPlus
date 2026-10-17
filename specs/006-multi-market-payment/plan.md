# Implementation Plan: یکپارچه‌سازی سیستم پرداخت و تخفیف چندمارکته (بازار و مایکت)

**Branch**: `feature/multi-market-payment` | **Date**: ۱۴۰۵/۰۷/۲۶ (2026-10-17) | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from [`specs/006-multi-market-payment/spec.md`](./spec.md)

---

## ۱. خلاصه اجرایی (Summary)

پیاده‌سازی ماژولار و جامع سیستم پرداخت چندمارکته مبتنی بر تشخیص هوشمند منبع نصب و پلتفرم در برنامه کلاینت و اعتبارسنجی امن سرور-به-سرور:
1. **تشخیص پلتفرم و منبع نصب (Platform & Installer Detection)**:
   - تعریف رابط `PlatformMarketDetector` در لایه Domain با پیاده‌سازی بومی در کلاینت برای تشخیص منبع نصب بر اساس `getInstallerPackageName`.
   - هدایت هوشمند به کافه بازار (`com.farsitel.bazaar`)، مایکت (`ir.mservices.market`) یا درگاه مستقیم وب/بانکی (در صورت نصب مستقیم APK خارج از مارکت یا پلتفرم‌های دسکتاپ/وب).
2. **یکپارچه‌سازی درگاه‌های پرداخت درون‌برنامه‌ای (Poolakey & Myket IAB)**:
   - پیاده‌سازی `MarketBillingGateway` با دو ماژول اختصاصی برای کافه بازار (بر پایه کتابخانه پولکی) و مایکت (بر پایه استاندارد IAB V3).
   - مدیریت چرخه حیات، ارسال `developerPayload` جهت اتصال خرید به حساب کاربر، و دریافت رسید امن.
3. **مکانیزم کدهای تخفیف با رفتار تفکیک‌شده**:
   - در کافه بازار: استفاده از تخفیف پویا (Dynamic Discount با توکن امضاشده JWT توسط سرور) جهت کاهش ریالی مبلغ فاکتور رسمی بازار.
   - در مایکت: پرداخت فاکتور استاندارد بدون تغییر قیمت و افزودن خودکار روزهای هدیه مازاد به دوره اشتراک کاربر توسط سرور، همراه با پیام دوستانه به زبان فارسی (بدون ذکر نام مارکت دیگر).
4. **اعتبارسنجی و همگام‌سازی سمت سرور (Ktor Server & Server-to-Server Verification)**:
   - ایجاد اندپوینت امن `POST /api/v2/subscriptions/verify-market-purchase` با متادیتای کامل Ktor OpenAPI سازگار با 3.0.3.
   - استعلام مستقیم از API توسعه‌دهندگان کافه بازار و مایکت و ثبت تراکنش در جدول `market_payment_transactions`.
   - مقابله با حملات Replay با ایندکس یکتای `purchase_token`.
   - به‌روزرسانی آنی جدول اشتراک کاربر (`subscriptions`) و افزودن روزهای باقیمانده.
5. **بازیابی خریدهای معلق (Pending Purchases Recovery)**:
   - بازیابی و ارسال خودکار خریدهای تاییدنشده حین باز شدن صفحه اشتراک در صورت قطعی ناگهانی اینترنت.

---

## ۲. زمینه فنی (Technical Context)

- **زبان و نسخه (Language/Version)**: Kotlin 2.x (Kotlin Multiplatform و Kotlin JVM)
- **وابستگی‌های اصلی (Primary Dependencies)**:
  - کلاینت: Compose Multiplatform (CMP)، Koin 4.x، Android Billing IPC (Poolakey / Myket IAB)، kotlinx.coroutines، kotlinx.serialization
  - سرور: Ktor Server 3.x (Netty، ContentNegotiation، JWT Auth، OpenAPI)، JetBrains Exposed، PostgreSQL، Ktor Client (جهت ارتباط سرور با کافه بازار و مایکت)
- **ذخیره‌سازی (Storage)**:
  - سرور: PostgreSQL (جدول `market_payment_transactions`، جدول `subscriptions`، جدول `promos`)
- **آزمون‌پذیری (Testing)**:
  - کلاینت: تست‌های واحد `PlatformMarketDetector`، بررسی تغییرات State در `SubscriptionViewModel` با Mock Gateway
  - سرور: تست‌های یکپارچگی اندپوینت `verify-market-purchase` با `testApplication` و Mock HTTP Client برای سرورهای مارکت
- **پلتفرم‌های هدف (Target Platform)**: Android، Desktop (JVM)، Web (WASM/JS)، Server (Linux/JVM)
- **نوع پروژه (Project Type)**: اپلیکیشن مالتی‌پلتفرم ماژولار + سرویس بک‌اند Ktor
- **اهداف کارایی (Performance Goals)**:
  - اعتبارسنجی سرور و فعال‌سازی اشتراک زیر ۲ ثانیه پس از اتمام پرداخت
  - تشخیص منبع نصب زیر ۵۰ میلی‌ثانیه در زمان راه‌اندازی کلاینت
  - پاسخگویی استعلام کد تخفیف زیر ۵۰۰ میلی‌ثانیه
- **محدودیت‌ها (Constraints)**:
  - رعایت اصول قانون اساسی AISpeakingPlus (Clean Architecture، سازگاری OpenAPI 3.0.3، عدم بلاک کردن Threadها)
  - پشتیبانی کامل از RTL و نمایش مبالغ و پیام‌ها به زبان فارسی با فونت استاندارد
  - منع استفاده از نام مارکت‌های رقیب در کلاینت اختصاصی هر مارکت

---

## ۳. بررسی انطباق با اصول قانون اساسی (Constitution Check)

*گیت ارزیابی: پیش از آغاز فاز تحقیقات و پس از اتمام فاز طراحی بررسی گردید.*

| اصل قانون اساسی | الزامات اصلی | تحلیل انطباق معماری | وضعیت |
|---|---|---|---|
| **I. Clean Architecture Everywhere** | تفکیک لایه‌ها و جهت وابستگی‌ها به درون | اینترفیس‌های `PlatformMarketDetector` و `MarketBillingGateway` در لایه Domain؛ پیاده‌سازی‌های بومی اندروید در `data` و `androidApp`؛ منطق ارائه در `feature/subscription`؛ کنترلرها در `server`. لایه Domain کاملاً عاری از کدهای فریم‌ورک و اندروید است. | **PASS** ✅ |
| **II. Pure KMP & CMP Client** | کدهای مشترک در `commonMain` و تفکیک تمیز پلتفرم | لایه مشترک با معماری پلتفرم سازگار بوده و قابلیت‌های خاص اندروید (AIDL/IPC) پشت اینترفیس مشترک با تزریق وابستگی در ماژول اندروید پیاده می‌شوند. | **PASS** ✅ |
| **III. Ktor Server & Async Coroutines** | عدم مسدودسازی Threadها و استفاده از Coroutine | استعلام‌های شبکه به سرورهای کافه بازار و مایکت با Ktor HTTP Client غیر مسدودکننده (`HttpClient`) و تراکنش‌های پایگاه داده با `newSuspendedTransaction(Dispatchers.IO)` انجام می‌شوند. | **PASS** ✅ |
| **IV. Dependency Injection via Koin** | تزریق از طریق Constructor، ماژول‌های مجزا | ماژول Koin برای سرویس‌های پرداخت کلاینت و سرور به صورت مجزا تعریف شده و هیچ Service Locator در لایه دامنه استفاده نخواهد شد. | **PASS** ✅ |
| **V. Ultra-Low Latency & High Performance** | پاسخگویی سریع، بهینه‌سازی دیتابیس | جدول `market_payment_transactions` روی `user_id` و `purchase_token` دارای شاخص (Index) یکتا خواهد بود تا جستجوها زیر ۱۰ میلی‌ثانیه انجام شوند. | **PASS** ✅ |
| **VI. Living OpenAPI Specification** | مستندسازی خودکار با متادیتای کامل `.describe` سازگار با 3.0.3 | تمام اندپوینت‌های جدید از جمله `POST /api/v2/subscriptions/verify-market-purchase` مجهز به بلوک کامل `.describe` با تعریف دقیق بدنه درخواست، نمونه داده و کدهای پاسخ خواهند بود. | **PASS** ✅ |

---

## ۴. ساختار پروژه و فایل‌ها (Project Structure)

### مستندات این ویژگی
```text
specs/006-multi-market-payment/
├── plan.md              # این سند برنامه اجرایی
├── spec.md              # مشخصات و نیازمندی‌های ویژگی
├── research.md          # تصمیمات معماری، پلتفرم و اعتبارسنجی
├── data-model.md        # ساختار جداول پایگاه داده و مدل‌های MVI
├── quickstart.md        # راهنمای تست گام‌به‌گام و سناریوهای آزمون
├── checklists/
│   └── requirements.md  # چک‌لیست کیفیت نیازمندی‌ها
└── contracts/
    └── payment-api.md   # قراردادهای REST API و OpenAPI
```

### ساختار کدهای اجرایی در ریپازیتوری
```text
domain/src/commonMain/kotlin/ir/aispeaking/domain/
├── model/payment/
│   ├── AppMarket.kt                 # انام مارکت‌ها (کافه بازار، مایکت، مستقیم)
│   └── MarketPurchaseResult.kt      # مدل‌های نتیجه خرید و خطاها
├── repository/
│   └── PaymentRepository.kt         # قرارداد ریپازیتوری پرداخت
└── usecase/payment/
    ├── DetectMarketUseCase.kt        # تشخیص مارکت فعال
    └── VerifyPurchaseUseCase.kt      # ارسال توکن به سرور جهت تایید

data/src/
├── commonMain/kotlin/ir/aispeaking/data/repository/
│   └── PaymentRepositoryImpl.kt     # پیاده‌سازی ریپازیتوری کلاینت
└── androidMain/kotlin/ir/aispeaking/data/payment/
    ├── AndroidMarketDetector.kt      # تشخیص نصاب از روی پکیج منیجر
    ├── MarketBillingGateway.kt       # درایور واسط پرداخت اندروید
    ├── BazaarBillingDriver.kt        # ارتباط با کافه بازار (Poolakey)
    └── MyketBillingDriver.kt         # ارتباط با مایکت (IAB V3)

feature/subscription/src/commonMain/kotlin/ir/aispeaking/subscription/
├── SubscriptionContract.kt          # افزودن وضعیت‌های مارکت، توکن تخفیف و روزهای هدیه
└── SubscriptionViewModel.kt         # اتصال به Gateway پرداخت و هندل خریدهای معلق

server/src/main/kotlin/ir/speaking/feature/
├── subscription/
│   ├── routing/
│   │   └── subscriptionRouting.kt    # اندپوینت‌های verify-market-purchase و validate-promo
│   ├── service/
│   │   ├── MarketVerificationService.kt # سرویس استعلام از API بازار و مایکت
│   │   └── DynamicDiscountService.kt    # تولید توکن JWT تخفیف پویا
│   └── repository/
│       └── MarketPaymentRepo.kt      # دسترسی به جدول market_payment_transactions
```

---

## ۵. مراحل اجرایی بعدی (Next Steps)

پس از تصویب این پلن، با فراخوانی دستور `/speckit-tasks`، وظایف ریزدانه، مستقل و تست‌پذیر برای پیاده‌سازی کلاینت و سرور در `tasks.md` تدوین خواهد شد.
