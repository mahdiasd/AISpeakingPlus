# فهرست وظایف مهندسی: پنل ادمین تحت وب (Tasks)

**شاخه فیچر**: `002-web-admin-panel`  
**تاریخ**: ۱۴۰۵/۰۷/۰۶ (2026-09-27)  
**وضعیت**: پیاده‌سازی کامل فرانت‌اند و بک‌اند انجام شد (Completed)  
**سند مشخصات**: [spec.fa.md](spec.fa.md) | **طرح فنی**: [plan.md](plan.md)  

---

## فاز ۱: راه‌اندازی و زیرساخت مشترک (Setup)

**هدف**: افزودن وابستگی‌های مورد نیاز، پکیج‌بندی و راه‌اندازی پروژه فرانت‌اند React در مونو-ریپو

- [x] T001 افزودن وابستگی هش رمزعبور BCrypt (`org.mindrot:jbcrypt:0.4`) به فایل `server/build.gradle.kts`
- [x] T002 [P] ایجاد ساختار پکیج ادمین در سرور `server/src/main/kotlin/ir/speaking/feature/admin/`
- [x] T003 [P] راه‌اندازی پروژه فرانت‌اند ادمین با Vite + React 19 + TypeScript + Tailwind CSS + Lucide Icons در پوشه `admin-web/`
- [x] T004 تعریف مدل‌ها و DTOهای مشترک ادمین در `server/src/main/kotlin/ir/speaking/feature/admin/model/AdminModels.kt`

---

## فاز ۲: زیرساخت پایه‌ای و پایگاه‌داده (Foundational)

**هدف**: ایجاد جداول دیتابیس، مایگریشن‌ها، پرووایدر امنیتی JWT ادمین و ایجاد خودکار SuperAdmin اولیه

- [x] T005 ایجاد جدول `AdminUserTable` در `server/src/main/kotlin/ir/speaking/feature/admin/db/AdminUserTable.kt`
- [x] T006 [P] ایجاد جدول لاگ عملیات `AdminAuditTable` در `server/src/main/kotlin/ir/speaking/feature/admin/db/AdminAuditTable.kt`
- [x] T007 [P] افزودن ستون‌های `status` و `suspendedReason` به `UserTable.kt` در `server/src/main/kotlin/ir/speaking/feature/user/db/UserTable.kt`
- [x] T008 [P] افزودن ستون‌های `grantSource`، `grantedBy` و `grantReason` به `SubscriptionTable.kt` در `server/src/main/kotlin/ir/speaking/feature/subscription/db/SubscriptionTable.kt`
- [x] T009 [P] افزودن ستون `status` (مقادیر `DRAFT`، `PUBLISHED`، `ARCHIVED`) به `StageTable.kt` در `server/src/main/kotlin/ir/speaking/feature/stage/db/StageTable.kt`
- [x] T010 ثبت جداول و اجرای خودکار ساخت ستون‌های جدید در `server/src/main/kotlin/ir/speaking/core/Databases.kt`
- [x] T011 پیاده‌سازی `AdminPrincipal` و پیکربندی احراز هویت `"admin-jwt"` در `server/src/main/kotlin/ir/speaking/core/Security.kt`
- [x] T012 پیاده‌سازی سرویس لاگ تغییرات `AuditLogService` در `server/src/main/kotlin/ir/speaking/feature/admin/audit/service/AuditLogService.kt`
- [x] T013 پیاده‌سازی ساخت خودکار حساب SuperAdmin اولیه در زمان بوت سرور در `server/src/main/kotlin/ir/speaking/feature/admin/auth/AdminBootstrapper.kt`

**نقطه بازبینی (Checkpoint)**: پایگاه‌داده آماده است، جداول مایگریت شده‌اند، امنیت ادمین تنظیم شده و حساب SuperAdmin اولیه ایجاد شده است.

---

## فاز ۳: داستان کاربر ۱ - ورود امن ادمین و چیدمان پنل (Priority: P1)

**هدف**: ادمین می‌تواند با نام کاربری و رمزعبور وارد شود، توکن معتبر ادمین دریافت کند و ساختار پنل را ببیند.

- [x] T014 [US1] پیاده‌سازی سرویس `AdminAuthService` (بررسی رمزعبور و صدور توکن) در `server/.../AdminAuthService.kt`
- [x] T015 [US1] پیاده‌سازی روت‌های ورود ادمین `AdminAuthRouting` (`POST /api/admin/auth/login` و `GET /api/admin/auth/me`) همراه با متادیتای OpenAPI در `server/.../AdminAuthRouting.kt`
- [x] T016 [US1] پیاده‌سازی کلاینت API و مدیریت توکن ادمین و پروکسی به Ktor در `admin-web/src/api/client.ts`
- [x] T017 [US1] پیاده‌سازی صفحه لاگین ادمین با اعتبارسنجی فرم، تم دارک مدرن و اعلان خطا در `admin-web/src/pages/LoginPage.tsx`
- [x] T018 [US1] پیاده‌سازی قالب و منوی اصلی پنل `AdminLayout` (سایدبار، هدر بالا، دکمه خروج و گارد احراز هویت) در `admin-web/src/components/layout/`

---

## فاز ۴: داستان کاربر ۲ - مدیریت و ویرایش دوگانه Stage با همگام‌ساز زنده دوطرفه (Priority: P1)

**هدف**: ادمین می‌تواند Stageها را هم از طریق فرم بصری (منوهای کشویی و Drag & Drop) و هم از طریق پیست مستقیم JSON خروجی هوش مصنوعی با همگام‌سازی بلادرنگ دوطرفه بسازد و ویرایش کند.

- [x] T019 [US2] پیاده‌سازی سرویس `AdminStageService` (اعتبارسنجی ساختار، تغییر ترتیب و پاکسازی کش ریدیس) در سرور
- [x] T020 [US2] پیاده‌سازی اندپوینت‌های Stageها `AdminStageRouting` (`GET`, `POST`, `PUT`, `DELETE /api/admin/stages`) با مستندات OpenAPI
- [x] T021 [US2] پیاده‌سازی صفحه لیست مراحل `StagesListPage` در React با جدول ریسپانسیو، فیلتر وضعیت و دکمه‌های بالا/پایین در `admin-web/src/pages/StagesListPage.tsx`
- [x] T022 [US2] پیاده‌سازی کامپوننت منوی کشویی صداها `VoiceDropdown` متصل به ۱۱ صدای سرور Kokoro TTS در `admin-web/src/components/stages/VoiceDropdown.tsx`
- [x] T023 [US2] پیاده‌سازی کامپوننت Drag & Drop آپلود تصاویر با پیش‌نمایش آنی در `admin-web/src/components/stages/ImageDropzone.tsx`
- [x] T024 [US2] پیاده‌سازی ویرایشگر دوطرفه `StageEditorPage` در React با همگام‌سازی بلادرنگ فرم بصری و باکس متنی JSON در `admin-web/src/pages/StageEditorPage.tsx`
- [x] T025 [US2] به‌روزرسانی اندپوینت کلاینت `GET /api/v2/stages` جهت فیلتر کردن و عدم نمایش مراحل پیش‌نویس به کاربران عادی

---

## فاز ۵: داستان کاربر ۳ - فهرست، جستجو و تعلیق کاربران (Priority: P1)

**هدف**: جستجوی کاربران با شماره موبایل فارسی، مشاهده پروفایل و امکان فعال یا غیرفعال‌سازی حساب.

- [x] T026 [US3] پیاده‌سازی `AdminUserService` (نرمال‌سازی ارقام فارسی شماره موبایل، صفحه‌بندی، تغییر وضعیت حساب و ابطال نشست در ریدیس) در سرور
- [x] T027 [US3] پیاده‌سازی اندپوینت‌های کاربران `AdminUserRouting` (`GET /api/admin/users`, `GET /api/admin/users/{userId}`, `POST /api/admin/users/{userId}/status`)
- [x] T028 [US3] به‌روزرسانی میدلورهای احراز هویت سرور (`SubscriptionGate.kt`) جهت مسدودسازی درخواست‌های کاربران تعلیق‌شده با خطای ۴۰۳ `ACCOUNT_SUSPENDED`
- [x] T029 [US3] پیاده‌سازی صفحه فهرست کاربران `UsersPage` در React با باکس جستجوی شماره موبایل فارسی، صفحه‌بندی و برچسب‌های وضعیت در `admin-web/src/pages/UsersPage.tsx`
- [x] T030 [US3] پیاده‌سازی مودال جزئیات کاربر `UserDetailModal` با دکمه‌های فعال‌سازی/تعلیق حساب در `admin-web/src/components/users/UserDetailModal.tsx`

---

## فاز ۶: داستان کاربر ۴ - اعطای دستی اشتراک بدون پرداخت با ثبت منشأ (Priority: P1)

**هدف**: امکان اهدای اشتراک به کاربران با برچسب `MANUAL_ADMIN`، شناسه ادمین، ثبت علت و تمدید انباشتی زمان.

- [x] T031 [US4] پیاده‌سازی `AdminSubscriptionService` (ثبت اشتراک دستی، افزودن به انتهای مهلت قبلی، ثبت لاگ و به‌روزرسانی کش ریدیس) در سرور
- [x] T032 [US4] پیاده‌سازی اندپوینت‌های اشتراک دستی `AdminSubscriptionRouting` (`POST /api/admin/users/{userId}/subscriptions/grant`, `POST /api/admin/subscriptions/{id}/cancel`)
- [x] T033 [US4] پیاده‌سازی دیالوگ اعطای اشتراک `GrantSubscriptionModal` در React (انتخاب دوره‌ها: ۷ روز، ۱ ماه، ۳ ماه، ۱ سال، روزهای دلخواه همراه با متن علت) در `admin-web/src/components/users/GrantSubscriptionModal.tsx`
- [x] T034 [US4] نمایش برچسب شفاف «اهدایی ادمین (نام ادمین)» و یادداشت در بخش جزئیات اشتراک کاربر در پنل وب

---

## فاز ۷: داستان کاربر ۵ - داشبورد آماری کلی و لاگ تاریخچه عملیات (Priority: P2)

**هدف**: مشاهده آمارهای کلیدی و جدول لاگ اقدامات ادمین‌ها جهت نظارت و شفافیت.

- [x] T035 [US5] پیاده‌سازی سرویس آمار `AdminDashboardService` (تعداد کل کاربران، اشتراک‌های فعال، Stageهای منتشرشده) در سرور
- [x] T036 [US5] پیاده‌سازی اندپوینت‌های داشبورد و لاگ `AdminDashboardRouting` (`GET /api/admin/dashboard/stats`, `GET /api/admin/audit-logs`)
- [x] T037 [US5] پیاده‌سازی صفحه داشبورد `DashboardPage` با کارت‌های شاخص‌های کلیدی و آمار در `admin-web/src/pages/DashboardPage.tsx`
- [x] T038 [US5] پیاده‌سازی صفحه تاریخچه عملیات `AuditLogsPage` در `admin-web/src/pages/AuditLogsPage.tsx`

---

## فاز ۸: پرداخت نهایی، آپلود رسانه‌ها و همگام‌سازی با Apidog

- [x] T039 پیاده‌سازی اندپوینت آپلود مستقیم عکس `POST /api/admin/media/upload` و سرو فایل‌های استاتیک در سرور
- [x] T040 بررسی سازگاری خروجی OpenAPI 3.0.3 و عدم وجود خطای اسکیما در لایو سینک Apidog
- [x] T041 اجرای سناریوهای آزمون کامل در فایل [quickstart.md](quickstart.md) و اطمینان از صحت عملکرد پنل React
