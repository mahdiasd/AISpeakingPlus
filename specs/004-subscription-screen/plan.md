# Implementation Plan: صفحه اختصاصی اشتراک‌ها و ارتقای حساب کاربری ویژه

**Branch**: `feature/subscription-screen` | **Date**: ۱۴۰۵/۰۷/۱۵ (۲۰۲۶-۱۰-۰۶) | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from [`specs/004-subscription-screen/spec.md`](./spec.md)

---

## ۱. خلاصه اجرایی (Summary)

پیاده‌سازی یکپارچه و انتها به انتهای **صفحه اختصاصی، چشم‌نواز و مدرن اشتراک‌ها** بر مبنای Clean Architecture و حذف دیالوگ/باتم‌شیت موقت قبلی:
1. **لایه رابط کاربری و نمایش (SharedUI & CMP)**:
   - ساخت صفحه اختصاصی تمام‌صفحه `SubscriptionScreen` با پالت رنگی شاداب، گرادیانت‌های نئونی، هدر طلایی با آیکون تاج، کارت‌های تعاملی پلن‌ها (۱ ماهه، ۳ ماهه، ۶ ماهه)، لیست مزایای کلیدی، فیلد کد تخفیف با اعتبارسنجی پویا و دکمه کنش خرید با نمایش قیمت نهایی.
   - پیاده‌سازی `SubscriptionViewModel` مبتنی بر MVI (`SubscriptionState`, `SubscriptionIntent`, `SubscriptionEffect`) با مدیریت StateFlow.
2. **اتصال ناوبری (Navigation 3)**:
   - تعریف مسیر تایپ‌سیف `SubscriptionRoute` در ماژول `navigation`.
   - اتصال دکمه «خرید اشتراک» در نوار بالای نقشه سفر (`JourneyMapScreen`).
   - اتصال کلیک روی مراحل قفل‌شده داستانی (`StageLockStatus.LOCKED_SUBSCRIPTION`) جهت هدایت تمام‌صفحه به جای دیالوگ.
   - اتصال دکمه ارتقا/تمدید در کارت وضعیت اشتراک صفحه پروفایل (`ProfileScreen`).
   - حذف کامل کامپوزبل موقت `SubscriptionPaywallSheet` و بازگشت یکپارچه با به‌روزرسانی آنی وضعیت حساب.
3. **لایه دامنه و داده (Domain & Data)**:
   - استفاده از مدل‌های دامنه `SubscriptionPlan` و `SubscriptionStatus`.
   - ایجاد یوزکیس `SubscribePlanUseCase` جهت ارسال درخواست خرید/فعال‌سازی پلن انتخابی.
   - افزودن متد `subscribe` به `SubscriptionRepository` و `SubscriptionRepositoryImpl`.
4. **لایه سرور (Ktor Server & OpenAPI 3.0.3)**:
   - تکمیل اندپوینت امن `POST /api/v2/subscriptions/subscribe` در `subscriptionRouting.kt` با ذخیره‌سازی و تمدید دوره در `SubscriptionTable`.
   - مستندسازی کامل با متادیتای Ktor OpenAPI (`.describe { ... }`) سازگار با OpenAPI 3.0.3 جهت Live Sync در Apidog.

---

## ۲. زمینه فنی (Technical Context)

- **زبان و نسخه (Language/Version)**: Kotlin 2.x (Kotlin Multiplatform & Kotlin JVM)
- **وابستگی‌های اصلی (Primary Dependencies)**:
  - کلاینت: Compose Multiplatform (CMP)، Navigation 3 (`androidx.navigation3`)، Koin 4.x، kotlinx.coroutines، kotlinx.serialization
  - سرور: Ktor Server 3.x (Netty، ContentNegotiation، JWT Auth، OpenAPI)، JetBrains Exposed، PostgreSQL، Redis
- **ذخیره‌سازی (Storage)**:
  - سرور: PostgreSQL (`subscriptions` table و `users` table)
  - کلاینت: حافظه نهان محلی و StateFlow
- **آزمون‌پذیری (Testing)**:
  - کلاینت: تست‌های واحد لایه دامنه با Mock Repository برای `SubscribePlanUseCase` و `SubscriptionViewModel`
  - سرور: تست‌های یکپارچگی مسیرهای Ktor با `testApplication` برای بررسی اندپوینت‌های `/api/v2/subscriptions/*`
- **پلتفرم‌های هدف (Target Platform)**: Android، Desktop (JVM)، iOS (KMP)، Web (WASM/JS)، Server (Linux/JVM)
- **نوع پروژه (Project Type)**: اپلیکیشن مالتی‌پلتفرم ماژولار + سرویس بک‌اند Ktor
- **اهداف کارایی (Performance Goals)**:
  - پاسخگویی زیر ۵۰ میلی‌ثانیه برای اندپوینت‌های اشتراک
  - رندر فوری و انتقال بدون پرش زیر ۳۰۰ میلی‌ثانیه از صفحه اصلی و پروفایل به صفحه اشتراک
- **محدودیت‌ها (Constraints)**:
  - رعایت ۱۰۰٪ مرزهای Clean Architecture (عدم نفوذ فریم‌ورک‌ها به لایه Domain)
  - پشتیبانی کامل از چینش راست‌به‌چپ (RTL)، تایپوگرافی استاندارد فارسی و تبدیل ارقام انگلیسی به فارسی
  - رعایت سازگاری OpenAPI 3.0.3 روی تمام مسیرهای سرور
- **دامنه و مقیاس (Scale/Scope)**: ۱ صفحه مقصد کامل، ۵ کامپوننت ماژولار نمایش، ۱ یوزکیس جدید، ۱ اندپوینت جدید سرور، اتصال ۳ نقطه ورودی ناوبری.

---

## ۳. بررسی انطباق با اصول قانون اساسی (Constitution Check)

*گیت ارزیابی: پیش از آغاز فاز تحقیقات و پس از اتمام فاز طراحی بررسی گردید.*

| اصل قانون اساسی | الزامات اصلی | تحلیل انطباق معماری | وضعیت |
|---|---|---|---|
| **I. Clean Architecture Everywhere** | تفکیک دقیق لایه‌ها و جهت وابستگی‌ها به درون | مدل‌های دامنه و یوزکیس‌ها در `domain`؛ پیاده‌سازی داده در `data`؛ رابط کاربری در `sharedUI`؛ مسیریابی در `server`. بدون هرگونه وابستگی UI به Domain. | **PASS** ✅ |
| **II. Pure KMP & CMP Client** | کدنویسی مشترک برای تمامی پلتفرم‌ها در `commonMain` | تمامی کامپوننت‌های صفحه اشتراک، ویومدل، قراردادها و فراخوانی‌های شبکه در `commonMain` ماژول‌های کلاینت پیاده‌سازی می‌شوند. | **PASS** ✅ |
| **III. Ktor Server & Async Coroutines** | عدم مسدودسازی Threadها، استفاده از Coroutine و Dispatchers.IO | کلیه تراکنش‌های پایگاه داده سرور با `newSuspendedTransaction(Dispatchers.IO)` و غیر مسدودکننده اجرا می‌گردند. | **PASS** ✅ |
| **IV. Dependency Injection via Koin** | تزریق از طریق Constructor، ماژول‌های مجزا | ماژول Koin برای `SubscriptionViewModel`، `SubscribePlanUseCase` و `SubscriptionRepo` تعریف و رجیستر می‌شود. بدون Service Locator در لایه دامنه. | **PASS** ✅ |
| **V. Ultra-Low Latency & High Performance** | پاسخگویی زیر ۱۰۰ میلی‌ثانیه، کشینگ، ساختار بهینه | کوئری‌های اشتراک بر روی کلید خارجی اندیس‌گذاری‌شده `user_id` اجرا شده و پاسخ‌ها با `kotlinx.serialization` بدون سربار منتقل می‌شوند. | **PASS** ✅ |
| **VI. Living OpenAPI Specification** | مستندسازی خودکار با متادیتای کامل `.describe` سازگار با 3.0.3 | اندپوینت `POST /api/v2/subscriptions/subscribe` مجهز به بلوک کامل `.describe` شامل شمای بدنه، نمونه درخواست و کدهای وضعیت HTTP می‌باشد. | **PASS** ✅ |

---

## ۴. ساختار پروژه و فایل‌ها (Project Structure)

### مستندات این ویژگی
```text
specs/004-subscription-screen/
├── plan.md              # این سند برنامه اجرایی
├── spec.md              # مشخصات و نیازمندی‌های ویژگی
├── research.md          # تصمیمات معماری و هویت بصری
├── data-model.md        # ساختار موجودیت‌ها و قراردادهای MVI
├── quickstart.md        # راهنمای اعتبارسنجی و سناریوهای آزمون
├── checklists/
│   └── requirements.md  # چک‌لیست کیفیت نیازمندی‌ها
└── contracts/
    └── subscription-api.md # قراردادهای API اندپوینت‌های اشتراک
```

### ساختار کدهای اجرایی در ریپازیتوری
```text
sharedUI/
└── src/commonMain/kotlin/ir/aispeaking/sharedui/ui/
    ├── subscription/
    │   ├── SubscriptionContract.kt          # MVI State, Intent, Effect
    │   ├── SubscriptionViewModel.kt         # StateFlow ViewModel logic
    │   ├── SubscriptionScreen.kt            # Fullscreen Compose Destination
    │   └── component/
    │       ├── SubscriptionHeader.kt        # Hero section with crown & glow
    │       ├── PlanSelectionCard.kt         # Interactive plan card with badges
    │       ├── SubscriptionBenefitsList.kt  # Golden feature checklist
    │       └── PromoCodeInputRow.kt         # Coupon code field & validation
    ├── stage/
    │   ├── JourneyMapScreen.kt              # Header CTA and locked stage wiring
    │   └── SubscriptionPaywallSheet.kt      # DEPRECATED / REMOVED
    └── profile/
        └── ProfileScreen.kt                 # SubscriptionCard upgrade wiring

navigation/
└── src/commonMain/kotlin/ir/aispeaking/navigation/
    ├── Routes.kt                            # Add SubscriptionRoute
    ├── config.kt                            # Register SubscriptionRoute serializer
    └── AppNavigation.kt                     # Wire entry<SubscriptionRoute> & callbacks

domain/
└── src/commonMain/kotlin/ir/aispeaking/domain/
    ├── repository/stage/SubscriptionRepository.kt # Add subscribe method
    └── usecase/stage/SubscriptionUseCases.kt       # Add SubscribePlanUseCase

data/
└── src/commonMain/kotlin/ir/aispeaking/data/
    └── repository/stage/SubscriptionRepositoryImpl.kt # Implement subscribe

network/
└── src/commonMain/kotlin/ir/aispeaking/network/
    └── api/stage/SubscriptionApi.kt          # Add subscribe network call

server/
└── src/main/kotlin/ir/speaking/feature/subscription/
    ├── repository/SubscriptionRepo.kt       # Add activateSubscription method
    └── routing/subscriptionRouting.kt        # Add POST /api/v2/subscriptions/subscribe
```

---

## ۵. ردیابی پیچیدگی (Complexity Tracking)

هیچ انحرافی از اصول معماری یا قانون اساسی وجود ندارد؛ تمامی گیت‌ها پاس شدند و نیازی به توجیه پیچیدگی مضاعف وجود ندارد.
