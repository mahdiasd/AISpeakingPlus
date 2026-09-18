---
description: "فهرست وظایف پیاده‌سازی مسیر یادگیری مکالمه داستانی و مرحله‌ای"
---

# وظایف پیاده‌سازی: مسیر مکالمه داستانی مرحله‌ای با دسترسی لایه‌ای و ماتریس ارزیابی

**ورودی**: اسناد طراحی از مسیر `/specs/001-story-based-speaking-journey/`  
**پیش‌نیازها**: [plan.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/plan.md)، [spec.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/spec.md)، [research.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/research.md)، [data-model.md](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/data-model.md)، [contracts/](file:///Users/mahdi/StudioProjects/AISpeakingPlus/specs/001-story-based-speaking-journey/contracts/)

**سازمان‌دهی**: وظایف بر اساس داستان‌های کاربر گروه‌بندی شده‌اند تا پیاده‌سازی و آزمون مستقل هر افزونه امکان‌پذیر باشد.

## ساختار: `- [ ] [TaskID] [P?] [Story?] شرح وظیفه به همراه مسیر فایل`

- **[P]**: امکان اجرای موازی (فایل‌های مجزا، بدون وابستگی مسدودکننده)
- **[Story]**: برچسب داستان کاربر مربوطه ([US1], [US2], [US3], [US4], [US5])
- فازهای راه‌اندازی (Setup)، زیربنایی (Foundational) و پولیش بدون برچسب استوری هستند.

---

## فاز ۱: راه‌اندازی (زیرساخت مشترک)

**هدف**: راه‌اندازی ساختار پوشه‌ها، داده‌های اولیه، و پایپ‌لاین کش دارایی‌های مشترک

- [ ] T001 ایجاد ساختار پوشه‌های فیچر برای ماژول‌های stage، progress، subscription و leaderboard در `server/src/main/kotlin/ir/speaking/feature/` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/`
- [ ] T002 ثبت داده‌های اولیه بیش از ۱۵ مرحله داستانی (از فرودگاه تهران تا لندن) به زبان انگلیسی و فارسی در `server/src/main/kotlin/ir/speaking/feature/stage/db/StageSeedData.kt`
- [ ] T003 [P] تنظیم روتینگ استاتیک Ktor جهت سرو تصاویر دو‌بعدی WebP مراحل با هدر `Cache-Control: public, max-age=2592000, immutable` در `server/src/main/kotlin/ir/speaking/core/Routing.kt`
- [ ] T004 [P] پیکربندی کتابخانه بارگذاری تصویر چندسکویی Coil 3 با کش دیسک ۱۰۰ مگابایت و کش رم در `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/di/CoilModule.kt`

---

## فاز ۲: زیربنایی (پیش‌نیازهای مسدودکننده)

**هدف**: جداول دیتابیس Exposed، مدل‌های پایه دامنه و زیرساخت روتینگ که باید قبل از شروع هر داستان کاربری تکمیل شوند

**⚠️ حیاتی**: هیچ استوری کاربری قبل از اتمام این فاز قابل اجرا نیست.

- [ ] T005 تعریف جدول Exposed به نام `StageTable` در `server/src/main/kotlin/ir/speaking/feature/stage/db/StageTable.kt` دقیقاً مطابق قیود data-model.md: `id VARCHAR(64) PRIMARY KEY`, `order_index INT UNIQUE INDEX`, `title VARCHAR(255) NOT NULL`, `title_fa VARCHAR(255) NOT NULL`, `briefing TEXT NOT NULL`, `briefing_fa TEXT NOT NULL`, `target_objective TEXT NOT NULL`, `background_url VARCHAR(512) NOT NULL`, `character_name VARCHAR(128) NOT NULL`, `character_avatar_url VARCHAR(512) NULL`, `character_gender VARCHAR(16) DEFAULT 'Woman'`, `voice_id VARCHAR(64) NULL`, `initial_speaker VARCHAR(16) DEFAULT 'Model'`, `max_turns INT DEFAULT 12`, `created_at TIMESTAMP DEFAULT NOW()`
- [ ] T006 [P] تعریف جدول Exposed به نام `StageProgressTable` در `server/src/main/kotlin/ir/speaking/feature/stage_progress/db/StageProgressTable.kt` مطابق قیود data-model.md: `id UUID PRIMARY KEY`, `user_id UUID FOREIGN KEY (users.uid) ON DELETE CASCADE`, `stage_id VARCHAR(64) FOREIGN KEY (stages.id) ON DELETE CASCADE`, `stars INT CHECK (stars BETWEEN 0 AND 3)`, `best_score INT DEFAULT 0`, `repeat_count INT DEFAULT 1`, `completed_at TIMESTAMP DEFAULT NOW()`, `updated_at TIMESTAMP DEFAULT NOW()`, و ایندکس یکتا `(user_id, stage_id)`
- [ ] T007 [P] تعریف جدول Exposed به نام `SubscriptionTable` در `server/src/main/kotlin/ir/speaking/feature/subscription/db/SubscriptionTable.kt` مطابق قیود data-model.md: `id UUID PRIMARY KEY`, `user_id UUID FOREIGN KEY (users.uid) ON DELETE CASCADE`, `plan_type VARCHAR(32) ('MONTHLY', 'QUARTERLY', 'BIANNUAL')`, `started_at TIMESTAMP DEFAULT NOW()`, `expires_at TIMESTAMP INDEX`, `status VARCHAR(32) DEFAULT 'ACTIVE' ('ACTIVE', 'EXPIRED', 'CANCELLED')`, `created_at TIMESTAMP DEFAULT NOW()`
- [ ] T008 ثبت `StageTable`، `StageProgressTable` و `SubscriptionTable` در مقداردهی اولیه دیتابیس در `server/src/main/kotlin/ir/speaking/core/Databases.kt`
- [ ] T009 [P] ایجاد اینام‌های دامنه `AccessTier` (`GUEST`, `REGISTERED_FREE`, `SUBSCRIBER`) و `StageLockStatus` (`UNLOCKED`, `LOCKED_PREVIOUS_STAGE`, `LOCKED_REGISTRATION`, `LOCKED_SUBSCRIPTION`) در `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/AccessTier.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/StageLockStatus.kt`
- [ ] T010 [P] تعریف DTOهای سریال‌پذیر برای لیست مراحل، جزئیات، ارزیابی، اشتراک و لیدربورد در `network/src/commonMain/kotlin/ir/aispeaking/network/model/stage/dto/StageDtos.kt`
- [ ] T011 تنظیم ماژول‌های Koin DI برای وابستگی‌های stage، progress، subscription و leaderboard در `server/src/main/kotlin/ir/speaking/di/KoinModule.kt`

---

## فاز ۳: داستان کاربر ۱ - ورود بدون اصطکاک مهمان و غوطه‌وری مرحله ۱ (اولویت: P1) 🎯 MVP

**هدف**: ورود کاربر بدون نیاز به لاگین، مشاهده/رد مقدمه سینماتیک، مشاهده نقشه ۲ بعدی مراحل، انجام مکالمه مرحله ۱، دریافت ستاره و ذخیره لوکال در حافظه دستگاه.

### آزمون‌های داستان کاربر ۱ ⚠️

- [ ] T012 [P] [US1] تست واحد برای `GetStagesUseCase` در حالت مهمان در `domain/src/commonTest/kotlin/ir/aispeaking/domain/usecase/stage/GetStagesUseCaseTest.kt`
- [ ] T013 [P] [US1] تست یکپارچگی روتینگ `GET /api/v2/stages` برای دسترسی مهمان در `server/src/test/kotlin/ir/speaking/feature/stage/StageRoutingTest.kt`

### پیاده‌سازی داستان کاربر ۱

- [ ] T014 [P] [US1] تعریف مدل‌های دامنه `Stage`، `StageProgress` و `LocalGuestProgress` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/Stage.kt`
- [ ] T015 [P] [US1] تعریف اینترفیس `StageRepository` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/StageRepository.kt`
- [ ] T016 [US1] پیاده‌سازی `GetStagesUseCase` و `GetStageDetailUseCase` با محاسبه وضعیت قفل در `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetStagesUseCase.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetStageDetailUseCase.kt`
- [ ] T017 [P] [US1] پیاده‌سازی دیتاسورس لوکال `LocalGuestProgressDataSource` با MultiplatformSettings در `data/src/commonMain/kotlin/ir/aispeaking/data/source/LocalGuestProgressDataSource.kt`
- [ ] T018 [P] [US1] پیاده‌سازی کلاینت Ktor برای `StageApi` در `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/StageApi.kt`
- [ ] T019 [US1] پیاده‌سازی ریپازیتوری کلاینت `StageRepositoryImpl` و مپرها در `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/StageRepositoryImpl.kt` و `data/src/commonMain/kotlin/ir/aispeaking/data/mapper/stage/StageMappers.kt`
- [ ] T020 [P] [US1] پیاده‌سازی `StageRepository` سرور با کوئری‌های Exposed در `server/src/main/kotlin/ir/speaking/feature/stage/repository/StageRepository.kt`
- [ ] T021 [US1] پیاده‌سازی روت‌های `GET /api/v2/stages` و `GET /api/v2/stages/{stageId}` با مجوز مهمان در `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T022 [P] [US1] ساخت کامپوننت Compose به نام `AsyncStageBackground` با افکت بلور در `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/component/AsyncStageBackground.kt`
- [ ] T023 [P] [US1] ساخت دیالوگ بریفینگ مرحله `StageBriefingDialog` با اهداف ماموریت به فارسی در `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/StageBriefingDialog.kt`
- [ ] T024 [US1] ساخت صفحه نقشه سفر `JourneyMapScreen` با بیش از ۱۵ مرحله در `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt` و `feature/main/src/commonMain/kotlin/ir/aispeaking/main/viewmodel/JourneyMapViewModel.kt`
- [ ] T025 [US1] اتصال ماموریت مرحله ۱ به صفحه چت در `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatScreen.kt` و `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatViewModel.kt`

---

## فاز ۴: داستان کاربر ۲ - ماتریس ارزیابی ستاره‌ها و جریمه راهنما (اولویت: P1)

**هدف**: پیاده‌سازی سیستم نمره‌دهی عینی ۰ تا ۳ ستاره با کسر ستاره به ازای خطای گرامری و درخواست راهنما، همراه با دکمه راهنمای فوری.

### آزمون‌های داستان کاربر ۲ ⚠️

- [ ] T026 [P] [US2] تست واحد محاسبه فرمول ماتریس ستاره‌ها در `server/src/test/kotlin/ir/speaking/feature/stage_progress/EvaluationRubricServiceTest.kt`
- [ ] T027 [P] [US2] تست یکپارچگی روت‌های راهنما و ارزیابی در `server/src/test/kotlin/ir/speaking/feature/stage_progress/EvaluationRoutingTest.kt`

### پیاده‌سازی داستان کاربر ۲

- [ ] T028 [P] [US2] تعریف مدل‌های دامنه `EvaluationSession` و `GrammarErrorDetail` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/EvaluationSession.kt`
- [ ] T029 [US2] پیاده‌سازی یوزکیس‌های `RequestStageHintUseCase` و `SubmitStageEvaluationUseCase` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/RequestStageHintUseCase.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SubmitStageEvaluationUseCase.kt`
- [ ] T030 [P] [US2] پیاده‌سازی روت سرور `POST /api/v2/stages/{stageId}/hint` با مدل زبانی در `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T031 [US2] پیاده‌سازی سرویس سرور `EvaluationRubricService` با فرمول `totalPenalties = grammarErrorsCount + hintsUsedCount` در `server/src/main/kotlin/ir/speaking/feature/stage_progress/service/EvaluationRubricService.kt`
- [ ] T032 [US2] پیاده‌سازی روت ارزیابی `POST /api/v2/stages/{stageId}/evaluate` در `server/src/main/kotlin/ir/speaking/feature/stage_progress/routing/progressRouting.kt`
- [ ] T033 [P] [US2] ساخت بنر نمایش راهنمای انگلیسی و فارسی `HintSuggestionCue` در `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/component/HintSuggestionCue.kt`
- [ ] T034 [P] [US2] ساخت کامپوننت ستاره‌ها `StarRatingBadge` با افکت درخشش در `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/StarRatingBadge.kt`
- [ ] T035 [US2] ساخت کارت نتایج پایانی `EvaluationResultCard` با فیدبک فارسی در `sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/stage/EvaluationResultCard.kt`
- [ ] T036 [US2] اتصال دکمه راهنما، شمارنده جریمه و ارسال ارزیابی در `feature/chat/src/commonMain/kotlin/ir/aispeaking/chat/ChatViewModel.kt`

---

## فاز ۵: داستان کاربر ۳ - گیت مرحله ۲: ثبت‌نام پیامکی و همگام‌سازی ابری (اولویت: P2)

**هدف**: مسدودسازی دسترسی مهمان به مرحله ۲، اعتبارسنجی پیامکی شماره همراه، و همگام‌سازی امتیاز لوکال مرحله ۱ به سرور ابری با قاعده حفظ بیشترین امتیاز: $\max(\text{local\_stars}, \text{cloud\_stars})$.

### آزمون‌های داستان کاربر ۳ ⚠️

- [ ] T037 [P] [US3] تست واحد حل تعارض پیشرفت لوکال و ابری در `server/src/test/kotlin/ir/speaking/feature/stage_progress/StageProgressSyncTest.kt`
- [ ] T038 [P] [US3] تست یکپارچگی خطای ۴۰۱ مرحله ۲ و همگام‌سازی در `server/src/test/kotlin/ir/speaking/feature/stage_progress/ProgressSyncRoutingTest.kt`

### پیاده‌سازی داستان کاربر ۳

- [ ] T039 [P] [US3] تعریف اینترفیس `StageProgressRepository` و `SyncGuestProgressUseCase` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/StageProgressRepository.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SyncGuestProgressUseCase.kt`
- [ ] T040 [P] [US3] پیاده‌سازی فراخوانی شبکه `POST /api/v2/progress/sync` در `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/StageProgressApi.kt`
- [ ] T041 [US3] پیاده‌سازی همگام‌سازی سرور با حفظ ماکزیمم ستاره‌ها در تراکنش دیتابیس در `server/src/main/kotlin/ir/speaking/feature/stage_progress/repository/StageProgressRepo.kt`
- [ ] T042 [US3] پیاده‌سازی روت همگام‌سازی و اعمال خطای ۴۰۱ برای مرحله ۲ بدون احراز هویت در `server/src/main/kotlin/ir/speaking/feature/stage_progress/routing/progressRouting.kt` و `server/src/main/kotlin/ir/speaking/feature/stage/routing/stageRouting.kt`
- [ ] T043 [US3] پیاده‌سازی `StageProgressRepositoryImpl` جهت خواندن حافظه لوکال و ارسال به سرور در `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/StageProgressRepositoryImpl.kt`
- [ ] T044 [US3] ساخت باتم‌شیت ثبت‌نام `RegisterBottomSheet` با ورود شماره موبایل، پیامک OTP و سینک خودکار در `feature/register/src/commonMain/kotlin/ir/aispeaking/register/RegisterBottomSheet.kt`
- [ ] T045 [US3] مدیریت کلیک روی مرحله ۲ در نقشه جهت باز کردن مودال ثبت‌نام در `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt`

---

## فاز ۶: داستان کاربر ۴ - دیوار پرداخت مرحله ۳ به بعد و گیت اشتراک (اولویت: P2)

**هدف**: محافظت از محتوای مراحل ۳ به بعد پشت دیوار پرداخت، اعمال خطای ۴۰۲ در سرور، و نمایش پلن‌های ۱، ۳ و ۶ ماهه به همراه کد تخفیف در کلاینت.

### آزمون‌های داستان کاربر ۴ ⚠️

- [ ] T046 [P] [US4] تست یکپارچگی خطای ۴۰۲ مرحله ۳ به بعد در `server/src/test/kotlin/ir/speaking/feature/subscription/SubscriptionRoutingTest.kt`

### پیاده‌سازی داستان کاربر ۴

- [ ] T047 [P] [US4] تعریف مدل‌های دامنه `SubscriptionPlan` و `SubscriptionStatus` در `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/SubscriptionPlan.kt`
- [ ] T048 [P] [US4] تعریف اینترفیس `SubscriptionRepository` و یوزکیس‌های اشتراک در `domain/src/commonMain/kotlin/ir/aispeaking/domain/repository/stage/SubscriptionRepository.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/SubscriptionUseCases.kt`
- [ ] T049 [P] [US4] پیاده‌سازی کلاینت شبکه پلن‌ها و وضعیت اشتراک در `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/SubscriptionApi.kt`
- [ ] T050 [P] [US4] پیاده‌سازی ریپازیتوری سرور اشتراک با کش ردیس در `server/src/main/kotlin/ir/speaking/feature/subscription/repository/SubscriptionRepo.kt`
- [ ] T051 [US4] پیاده‌سازی اینترسپتور امنیتی `requireSubscription` با خطای ۴۰۲ در `server/src/main/kotlin/ir/speaking/feature/subscription/interceptor/SubscriptionGate.kt`
- [ ] T052 [US4] پیاده‌سازی روت‌های پلن‌ها و استعلام وضعیت اشتراک در `server/src/main/kotlin/ir/speaking/feature/subscription/routing/subscriptionRouting.kt`
- [ ] T053 [US4] پیاده‌سازی `SubscriptionRepositoryImpl` در `data/src/commonMain/kotlin/ir/aispeaking/data/repository/stage/SubscriptionRepositoryImpl.kt`
- [ ] T054 [US4] ساخت شیت خرید اشتراک `SubscriptionPaywallSheet` با قیمت تومان و کد تخفیف در `feature/purchases/src/commonMain/kotlin/ir/aispeaking/purchases/SubscriptionPaywallSheet.kt`
- [ ] T055 [US4] مدیریت کلیک روی مراحل ۳ به بعد در نقشه جهت نمایش شیت اشتراک در `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyMapScreen.kt`

---

## فاز ۷: داستان کاربر ۵ - تکرار مراحل و حفظ بالاترین رکورد (اولویت: P3)

**هدف**: امکان تکرار مرحله بدون از دست رفتن رکورد ستاره قبلی ($\text{stars}_{\text{saved}} = \max(\text{existing}, \text{new})$)، افزایش تعداد تکرار، و آپدیت رده‌بندی ردیس لیدربورد.

### آزمون‌های داستان کاربر ۵ ⚠️

- [ ] T056 [P] [US5] تست واحد منطق عدم افت رکورد در `server/src/test/kotlin/ir/speaking/feature/stage_progress/ReplayHighScoreTest.kt`
- [ ] T057 [P] [US5] تست یکپارچگی روت لیدربورد و صفحه‌بندی در `server/src/test/kotlin/ir/speaking/feature/leaderboard/LeaderboardRoutingTest.kt`

### پیاده‌سازی داستان کاربر ۵

- [ ] T058 [P] [US5] تعریف مدل دامنه `LeaderboardEntry` و یوزکیس لیدربورد در `domain/src/commonMain/kotlin/ir/aispeaking/domain/model/stage/LeaderboardEntry.kt` و `domain/src/commonMain/kotlin/ir/aispeaking/domain/usecase/stage/GetJourneyLeaderboardUseCase.kt`
- [ ] T059 [P] [US5] پیاده‌سازی کلاینت شبکه لیدربورد در `network/src/commonMain/kotlin/ir/aispeaking/network/api/stage/LeaderboardApi.kt`
- [ ] T060 [US5] ارتقای منطق ذخیره‌سازی سرور جهت حفظ ماکزیمم ستاره و امتیاز در `server/src/main/kotlin/ir/speaking/feature/stage_progress/repository/StageProgressRepo.kt`
- [ ] T061 [US5] پیاده‌سازی امتیازدهی لیدربورد در ردیس (`ZADD leaderboard:journey`) در `server/src/main/kotlin/ir/speaking/feature/leaderboard/repository/LeaderboardRepo.kt`
- [ ] T062 [US5] پیاده‌سازی اندپوینت `GET /api/v2/leaderboard/journey` در `server/src/main/kotlin/ir/speaking/feature/leaderboard/routing/leaderboardRouting.kt`
- [ ] T063 [US5] ساخت شیت رده‌بندی `JourneyLeaderboardSheet` در `feature/main/src/commonMain/kotlin/ir/aispeaking/main/screen/JourneyLeaderboardSheet.kt`

---

## فاز ۸: پولیش و ملاحظات فراگیر

**هدف**: اعتبارسنجی سناریوهای کوئیک‌استارت، کامپایل نهایی و مستندات

- [ ] T064 [P] اجرای سناریوهای اعتبارسنجی گام‌به‌گام از `specs/001-story-based-speaking-journey/quickstart.md`
- [ ] T065 [P] به‌روزرسانی مستندات فنی و فهرست API در `specs/001-story-based-speaking-journey/README.md`
- [ ] T066 اجرای بررسی کامپایل چندسکویی با `./gradlew :server:compileKotlin :desktopApp:compileKotlin`
- [ ] T067 اجرای تست‌های خودکار با `./gradlew :domain:test :server:test` و اطمینان از پاس شدن همه تست‌ها
