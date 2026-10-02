# طرح فنی پیاده‌سازی: پنل ادمین تحت وب (Implementation Plan)

**شاخه فیچر**: `002-web-admin-panel` | **تاریخ**: ۱۴۰۵/۰۷/۰۶ (2026-09-27) | **سند مشخصات**: [spec.fa.md](spec.fa.md)

---

## خلاصه طرح (Summary)

پیاده‌سازی پنل مدیریت تحت وب جامع و امن برای AISpeakingPlus با تمرکز بر:
1. **مدیریت مراحل (Stages)**: ویرایشگر دوگانه شامل فرم بصری (منوهای کشویی انتخاب صداهای Kokoro TTS، جنسیت و آغازگر، کادرهای Drag & Drop تصاویر با پیش‌نمایش آنی) و باکس زنده JSON با همگام‌سازی بلادرنگ دوطرفه جهت سهولت ساخت مرحله از طریق پرامپت‌های هوش مصنوعی.
2. **مدیریت و تعلیق کاربران**: مشاهده لیست، صفحه‌بندی، جستجوی شماره همراه با نرمال‌سازی ارقام فارسی و تعلیق/فعال‌سازی حساب با ابطال آنی نشست در ردیس.
3. **اعطای اشتراک دستی بدون پرداخت**: ثبت رکورد با برچسب منبع `MANUAL_ADMIN`، شناسه ادمین، ثبت علت و تمدید انباشتی زمان بدون ایجاد تراکنش درگاه بانکی.
4. **داشبورد آماری و ثبت تاریخچه عملیات**: پایش شاخص‌های کلیدی و ثبت تمامی اقدامات در جدول `admin_audit_logs`.
5. **معماری فرانت‌اند و بک‌اند**: توسعه فرانت‌اند در پوشه اختصاصی `admin-web` با **React، Vite، TypeScript و Tailwind CSS** و توسعه اندپوینت‌ها در ماژول `server` با Ktor 3.x، فریم‌ورک Exposed روی دیتابیس PostgreSQL و انطباق با استاندارد OpenAPI 3.0.3 جهت همگام‌سازی خودکار با Apidog.

---

## بستر و زمینه فنی (Technical Context)

- **زبان و نسخه‌ها**: Kotlin 2.x (JVM برای سرور Ktor) و TypeScript / React 19 (برای پنل وب در `admin-web`).
- **وابستگی‌های اصلی**:
  - سرور: Ktor Server 3.x (انجین Netty), JetBrains Exposed, Koin 4.x, BCrypt (`jbcrypt:0.4`), kotlinx.serialization, kotlinx-coroutines, Ktor OpenAPI.
  - فرانت‌اند: React 19, Vite, TypeScript, Tailwind CSS, Lucide React (آیکون‌های وکتوری), React Router DOM.
- **پایگاه‌داده و حافظه کش**: PostgreSQL 15+ (جداول جدید `admin_users` و `admin_audit_logs` و توسعه جداول موجود) و Redis 7+ (لیست سیاه توکن‌ها و کش اطلاعات استیج‌ها).
- **چارچوب آزمون**: JUnit 5, Ktor `testApplication`, Mockk برای سرور، و Vitest / React Testing Library برای فرانت‌اند.
- **سکوی هدف**: مرورگرهای وب مدرن (دسکتاپ و تبلت) و سرور لینوکس.
- **اهداف کارایی و محدودیت‌ها**:
  - پاسخ جستجو و فیلتر کاربران زیر ۱۰۰ میلی‌ثانیه.
  - اعمال آنی همگام‌سازی دوطرفه بین فرم و باکس JSON زیر ۵۰ میلی‌ثانیه در مرورگر.
  - تفکیک قطعی و نفوذناپذیر توکن ادمین (`admin-jwt`) با نقش `ROLE_ADMIN` از توکن‌های کاربران عادی.
  - مستندسازی کامل تمامی مسیرهای جدید با متادیتای Ktor `.describe` و سازگاری با استاندارد OpenAPI 3.0.3 برای همگام‌سازی لایو در Apidog.

---

## بررسی انطباق با اصول پایه‌ای پروژه (Constitution Check)

- [x] **اصل اول (Clean Architecture)**: جداسازی دقیق لایه‌های Domain (مدل‌ها و Use Caseهای ادمین)، Data (مخازن Exposed و DTOها) و Presentation (روت‌های Ktor در سرور و کامپوننت‌های React در فرانت‌اند وب).
- [x] **اصل دوم (Decoupled Web Architecture)**: پیاده‌سازی مستقل و سبک پنل ادمین وب در پوشه `admin-web` با استفاده از استک استاندارد وب (React + Vite + Tailwind CSS) با اتصال مستقیم از طریق REST API به بک‌اند Ktor سرور، جهت تضمین سرعت فوق‌العاده، رندرینگ بی‌نقص DOM و تفکیک کامل پنل مدیریتی از کلاینت موبایل/کاربر.
- [x] **اصل سوم (Ktor Coroutine Pipeline)**: کلیه عملیات‌های دیتابیس در بلاک‌های غیرمسدودکننده `newSuspendedTransaction(Dispatchers.IO)` و روی انجین Netty.
- [x] **اصل چهارم (Koin DI)**: تزریق وابستگی سازنده (Constructor Injection) برای تمام سرویس‌ها، ریپازیتوری‌ها و کنترلرها با ماژول اختصاصی `adminModule`.
- [x] **اصل پنجم (High Performance & Redis Cache)**: ابطال آنی نشست در ردیس و کش استیج‌ها با زمان پاسخگویی سریع.
- [x] **اصل ششم (OpenAPI 3.0.3 Living Documentation)**: تمامی روت‌های `/api/admin/*` دارای متادیتای کامل `.describe { ... }` با نمونه‌های استاندارد ورودی و خروجی برای سینک در Apidog.

---

## ساختار پکیج‌ها و کدهای منبع

```text
server/src/main/kotlin/ir/speaking/
├── feature/admin/
│   ├── auth/
│   │   ├── AdminPrincipal.kt
│   │   ├── AdminAuthService.kt
│   │   ├── AdminBootstrapper.kt
│   │   └── routing/AdminAuthRouting.kt
│   ├── stage/
│   │   ├── dto/AdminStageDto.kt
│   │   ├── service/AdminStageService.kt
│   │   └── routing/AdminStageRouting.kt
│   ├── user/
│   │   ├── dto/AdminUserDto.kt
│   │   ├── service/AdminUserService.kt
│   │   └── routing/AdminUserRouting.kt
│   ├── subscription/
│   │   ├── dto/AdminSubscriptionDto.kt
│   │   ├── service/AdminSubscriptionService.kt
│   │   └── routing/AdminSubscriptionRouting.kt
│   ├── media/
│   │   └── routing/AdminMediaRouting.kt
│   ├── audit/
│   │   ├── db/AdminAuditTable.kt
│   │   └── service/AuditLogService.kt
│   └── db/
│       └── AdminUserTable.kt
└── core/
    ├── Databases.kt               # ثبت جداول جدید و ستون‌های مایگریشن
    └── Security.kt                # پیکربندی پرووایدر اختصاصی admin-jwt

admin-web/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tailwind.config.js
├── index.html
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── api/
    │   ├── client.ts              # ارتباط با اندپوینت‌های REST سرور Ktor و مدیریت توکن ادمین
    │   └── types.ts               # اینترفیس‌های DTO و مدل‌های داده
    ├── components/
    │   ├── layout/
    │   │   ├── AdminLayout.tsx
    │   │   ├── AdminSidebar.tsx
    │   │   └── AdminTopBar.tsx
    │   ├── stages/
    │   │   ├── VoiceDropdown.tsx       # منوی کشویی انتخاب ۱۱ صدای Kokoro TTS
    │   │   ├── ImageDropzone.tsx       # کادر Drag & Drop آپلود عکس با پیش‌نمایش
    │   │   └── JsonSyncEditor.tsx      # باکس همگام‌ساز بلادرنگ دوطرفه JSON
    │   └── ui/                         # کامپوننت‌های پایه (Modal, Badge, Button, Input)
    └── pages/
        ├── LoginPage.tsx
        ├── DashboardPage.tsx
        ├── StagesListPage.tsx
        ├── StageEditorPage.tsx         # ویرایشگر دوگانه فرم و JSON
        ├── UsersPage.tsx
        └── AuditLogsPage.tsx
```

---

## داکیومنت‌های طراحی ایجاد شده (Phase 1 Artifacts)

- **مدل داده و پایگاه‌داده**: [data-model.md](data-model.md)
- **تصمیم‌های فنی و پژوهش**: [research.md](research.md)
- **قراردادهای وب‌سرویس**: [contracts/](contracts/)
  - [admin-auth-api.md](contracts/admin-auth-api.md)
  - [admin-stages-api.md](contracts/admin-stages-api.md)
  - [admin-users-api.md](contracts/admin-users-api.md)
  - [admin-subscriptions-api.md](contracts/admin-subscriptions-api.md)
  - [admin-media-api.md](contracts/admin-media-api.md)
- **راهنمای آزمون‌های محلی**: [quickstart.md](quickstart.md)
