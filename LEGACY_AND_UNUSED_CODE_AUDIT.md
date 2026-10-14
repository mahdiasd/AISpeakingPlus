# گزارش ممیزی کدهای منسوخ، بدون استفاده و به‌جامانده از پروژه قبلی
**پروژه:** AI Speaking Plus (AISpeakingPlus)  
**تاریخ گزارش:** ۲۳ مهر ۱۴۰۵  
**روش تحلیل:** تحلیل گراف دانش کدبیس (Codebase Memory Graph) با رهگیری لبه‌های `CALLS` و `USAGE`، درجه وابستگی، تحلیل استاتیک و جستجوی جامع در تمامی ماژول‌های کلاینت (CMP) و سرور (Ktor).

---

## مقدمه و خلاصه اجرایی
این سند جامع‌ترین بررسی ساختاری پروژه است که مشخص می‌سازد کدام بخش‌ها از سیستم قدیمی (شامل سناریوهای دسته‌ای، جعبه لایتنر، سیستم رقابت، چالش‌های روزانه و متدهای نام‌گذاری شده تحت `AiSpeakingMultiplatform`) در پروژه فعلی رسوب کرده‌اند و کدام توابع، کلاس‌ها، فایل‌های استاتیک و لایه‌های کلاینت و سرور هم‌اکنون بدون هیچ فراخواننده‌ای (Dead Code) در ریپازیتوری رها شده‌اند.

---

## ۱. کدهای به‌جامانده از پروژه قبلی (Legacy Code)

در این بخش مواردی معرفی می‌شوند که متعلق به نسخه ۱ (Scenarios & Lightener) یا زیرساخت‌های قبل از بازطراحی داستان‌محور (V2 Story-Based Journey) بوده و با معماری فعلی تعارضی ندارند اما فضای دیسک، حجم باندل و پیچیدگی کد را به شدت افزایش داده‌اند.

### ۱.۱. دارایی‌های استاتیک حجیم و فایل‌های سنگین سرور (`server/src/main/resources/static/`)
در پوشه منابع استاتیک سرور، مجموعه‌ای از تصاویر و دارایی‌های حجیم مربوط به سناریوها و فیچرهای حذف‌شده اپلیکیشن قبلی وجود دارد که دیگر هیچ روت یا صفحه‌ای از آن‌ها استفاده نمی‌کند:
1. **پوشه `server/src/main/resources/static/scenario/`**: شامل بیش از ۵۰ تصویر با فرمت WebP در ۱۲ دسته‌بندی منسوخ (`art`, `business`, `education`, `emergency`, `everyday_conversations`, `ielts_exam`, `medical`, `music`, `resilience`, `sports`, `technology`, `travel`). در نسخه ۲، مراحل به شکل یکپارچه در `static/stages` قرار دارند و مسیر `/resources/scenario` در هیچ کجای کلاینت یا پنل وب استفاده نمی‌شود.
2. **پوشه `server/src/main/resources/static/category/`**: شامل تصاویر دسته‌بندی سناریوهای پروژه قبل.
3. **پوشه `server/src/main/resources/static/challenge/`**: شامل تصاویر ۱۲ چالش روزانه اپ قبلی (`apartment_viewing`, `visa_interview`, `crisis_management` و ...).
4. **پوشه `server/src/main/resources/static/banner/`**: شامل بنرهای تبلیغاتی داشبورد اپلیکیشن قبلی (`banner_1.webp` و ...).
5. **پوشه `server/src/main/resources/static/avatar/`**: آواتارهای اولیه ۵ گانه ربات (`ai_avatar_1.webp` تا ۵) که با سیستم جدید آواتارهای کاراکترها در پوشه `avatars/` جایگزین شده‌اند.
6. **فایل‌های `static/index.html` و `static/landing/`**: صفحات لندینگ قدیمی سرور از نوامبر ۲۰۲۵.

### ۱.۲. دامپ‌ها، فایل‌های فشرده و اسکریپت‌های تست رهاشده در ریشه سرور (`server/`)
در دایرکتوری اصلی سرور، چندین فایل با حجم مجموع نزدیک به **۴۰۰ مگابایت** وجود دارد که مربوط به بک‌آپ‌ها و ایمیج‌های توسعه قبلی هستند:
1. **`server/ai-speaking-image.tar.gz`**: با حجم ۱۹۴ مگابایت (ایمیج خروجی داکر قدیمی).
2. **`server/postgres-15.tar.gz`**: با حجم ۱۵۲ مگابایت (دامپ خام پایگاه داده پستگرس قبلی).
3. **`server/redis-7.tar.gz`**: با حجم ۴۴ مگابایت (فایل دیتای ردیس قدیمی).
4. **`server/final_db_backup.sql`**: فایل بک‌آپ دیتابیس با حجم ۶۴۰ کیلوبایت.
5. **`server/last_200_users.csv` و فایل‌های `group_1_subscribed.txt` و `group_2_recent.txt`**: شماره موبایل و اطلاعات کاربران قدیمی.
6. **`server/home_50_once.js` و `server/stress-test.js`**: اسکریپت‌های تست بار k6 که اندپوینت منسوخ `/api/v1/home` و ساختارهای `Categories` و `Scenarios` را تست می‌کنند.
7. **پرامپت‌های استاتیک متنی**: `category_image_generator_prompt`، `image_generator_prompt`، `scenario_generator_prompt.txt` و `ai_response.json`.
8. **پوشه‌های استقرار قدیمی**: `server/deploy/`, `server/deploy_dist/`, `server/deploy_files/`, `server/backups/`.

### ۱.۳. کلاس‌ها و ابزارهای منسوخ سرور (`server/src/main/kotlin/ir/speaking/core/`)
1. **`ir.speaking.core.utils.ImageType.kt`**:
   یک `sealed class` شامل تایپ‌های `SCENARIO`, `CHALLENGE`, `CATEGORY`, `Message` است که صفر رفرنس خارجی در کل پروژه دارد.
2. **`ir.speaking.core.utils.ImageSaver.kt`**:
   کلاسی شامل توابع ذخیره و حذف عکس در مسیرهای استاتیک سناریو که به کلی با `AdminMediaRouting` (پوشه `uploads/stages`) جایگزین شده است.
3. **`ir.speaking.core.utils.ImageData.kt` (۲۰۷ خط)**:
   کدهای پیچیده رفلکشن برای تزریق مالتی‌پارت به مدل‌های قدیمی با انوتیشن `@FileUpload` و توابع `parseMultipartData` و `injectFiles` که هیچ استفاده‌ای ندارند.
4. **`ir.speaking.core.utils.DatabaseHelper.kt` (۱۳۶ خط)**:
   ابزار ریست دیتابیس با متغیر `allTables = emptyArray<Table>()` که هیچ تستی یا کدی در سرور آن را صدا نمی‌زند.
5. **`ir.speaking.core.utils.MyConstant.kt`**:
   - مقدار `BASE_PROMPT`: حاوی پرامپت سناریوهای قدیمی همراه با فیلد زائد `finishedTasksIndex`.
   - مقدار `SUGGESTION_PROMPT`: پرامپت قدیمی ۳ گزینه‌ای بر پایه `REMAINING_TASKS`.
   (در معماری جدید، پرامپت‌ها در `StageChatService` به شکل پویا و با در نظر گرفتن سطح CEFR کاربر تولید می‌شوند).
6. **`ir.speaking.core.response.PagedList.kt`**:
   مدل بسته‌بندی لیست‌های صفحه‌بندی‌شده از معماری قبل.

### ۱.۴. پکیج‌های تنظیمات لوکال در ماژول `storage/`
در ماژول `storage`، تعداد زیادی Preference قدیمی تعریف شده که حتی در Koin تزریق نشده یا هیچ مصرف‌کننده‌ای در کلاینت ندارند:
1. **`storage/preferences/scenario/`**: کلاس‌های `ScenarioPreferences` و `ScenarioPreferencesImpl` به همراه مدل‌های `SharedScenario` و `SharedScenarioTask`.
2. **`storage/preferences/guide/`**: اینترفیس `GuidePreferences` و `GuidePreferencesImpl` شامل متدهای ثبت وضعیت دیده‌شدن راهنمای لایتنر، سناریو، نقشه، چالش و ثبت‌نام.
3. **`storage/preferences/firebase/`**: کلاس‌های `FirebaseTokenPreferences` و `FirebaseTokenPreferencesImpl` (پروژه نوتیفیکیشن فایربیس ندارد).
4. **`storage/preferences/welcome_message/`**: کلاس‌های `WelcomeMessagePreferences`، `WelcomeMessagePreferencesImpl` و مدل `SharedWelcomeMessage`.
5. **`storage/preferences/onboarding/`**: کلاس‌های `OnboardingPreferences` و `OnboardingPreferencesImpl`.
6. **`storage/preferences/voice_setting/`**: کلاس‌های `VoiceSettingPreferences`، `VoiceSettingPreferencesImpl` و مدل `SharedVoiceSetting`.
7. **`storage/preferences/theme_mode/`**: کلاس‌های `ThemeModePreferences`، `ThemeModePreferencesImpl` و مدل `SharedThemeMode`.
8. **ثوابت در `SharedKeyConstant.kt`**: مقادیر `SCENARIO`, `THEME_MODE`, `VOICE_SETTING`, `ONBOARDING`, `GUIDE`, `FIREBASE_TOKEN`, `WELCOME_MESSAGE`.

### ۱.۵. آیکون‌ها، وکتورها و استرینگ‌های به‌جامانده در `sharedUI/`
در ماژول مشترک کاربری، فایل‌های گرافیکی و رشته‌های متعددی از سیستم‌های لایتنر، ویدیوکال و سناریوها دست‌نخورده باقی مانده‌اند:
1. **آیکون‌های لایتنر**:
   - `ic_lightener.xml`
   - `ic_lightener2.png`
   - `ic_lightener_active_dark.xml`
   - `ic_lightener_active_light.xml`
2. **آیکون‌های سناریو، چالش و کلمات روزانه**:
   - `ic_daily_word.xml`, `ic_word.xml`
   - `ic_challenge.xml`, `ic_challenge_active_dark.xml`, `ic_challenge_active_light.xml`
   - `ic_scenario_active_dark.xml`, `ic_scenario_active_light.xml`
3. **مسیر نقشه قدیمی و آنبوردینگ**:
   - `ic_roadmap_dashline_1.png` تا `6.png`
   - `ic_roadmap_level_1_not_passed.xml`, `ic_roadmap_level_1_passed.xml`
   - `ic_roadmap_level_2_not_passed.xml`, `ic_roadmap_level_2_passed.xml`
   - `plan_item_vector.png`
   - `vector_onboarding_1.webp`, `vector_onboarding_2.webp`, `vector_onboarding_3.webp`
4. **آیکون‌های تماس ویدیویی**:
   - `ic_camera.xml`, `ic_switch_camera.xml`, `ic_video.xml`, `ic_video_off.xml`
5. **تصاویر راهنما (Guide Dialog)**:
   - `guide_register.webp`, `guide_lightener.webp`, `guide_roadmap.webp`
   - `guide_challenge_1.webp`, `guide_challenge_2.webp`
   - `guide_scenario_1.webp`, `guide_scenario_2.webp`, `guide_scenario_3.webp`
   - `guide_chat_1.webp` تا `guide_chat_6.webp`
   - `guide_profile_1.webp`, `guide_profile_2.webp`
6. **رشته‌های متنی منسوخ در `strings.xml`**:
   - رشته‌های لایتنر: `lightener`, `translations_created`, `add_to_lightener`, `in_lightener`, `empty_lightener`, `lightener_toolbar_title`
   - رشته‌های چالش و رقابت: `challenge`, `daily_challenge`, `competitions`, `complete_scenarios`, `complete_words`
   - رشته‌های کلمات روزانه: `daily_word_title`, `btn_word_answer_accept`, `no_daily_word_exist`
   - رشته‌های سناریوهای قدیمی: `scenarios`, `scenarios_title`, `error_cant_find_scenario_id`, `error_local_scenario_not_found`, `error_language_level_click_when_progress_is_null`

### ۱.۶. مغایرت‌های نام‌گذاری پروژه قبلی (`AiSpeakingMultiplatform`)
پروژه از ریپازیتوری قبلی تغییر نام داده اما در لانچرهای پلتفرمی هنوز از شناسه قدیمی استفاده می‌شود:
1. **`desktopApp/src/main/kotlin/main.kt`**:
   خط ۱۴: `title = "AiSpeakingMultiplatform"` به جای عنوان فعلی `AI Speaking Plus`.
2. **`androidApp/src/main/AndroidManifest.xml`**:
   خط ۱۰: `android:label="AiSpeakingMultiplatform"`.
3. **`webApp/src/commonMain/resources/manifest.json`**:
   خط ۲ و ۳: `"name": "AiSpeakingMultiplatform"`, `"short_name": "AiSpeakingMultiplatform"`.
4. **`webApp/src/commonMain/resources/index.html`**:
   خط ۱۰: `<title>AiSpeakingMultiplatform</title>`.

---

## ۲. توابع و متدهایی که در هیچ‌کجا استفاده نمی‌شوند (Dead Functions)

با پایش گراف فراخوانی در کل کدبیس، توابع و متدهای زیر شناسایی شدند که هیچ فراخواننده‌ای (In-degree = 0) در سراسر سیستم ندارند.

### ۲.۱. ماژول سرور (`server/src/main/kotlin/ir/speaking/`)
| فایل | نام تابع / متد | علت عدم استفاده |
|---|---|---|
| `core/utils/Logger.kt` | `logError(message)` | لاگینگ سرور با `PrintHelper` استاندارد شده؛ این توابع بی‌استفاده‌اند |
| `core/utils/Logger.kt` | `logSuccess(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logWarning(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logInfo(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logDebug(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logNotice(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logDefault(message)` | بی‌استفاده |
| `core/utils/Logger.kt` | `logCritical(message)` | بی‌استفاده |
| `core/utils/AppUtils.kt` | `validUUID(stringId)` | هیچ کالبکی ندارد؛ در کنترلرها مستقیماً از `.toUUID()` اکستنشن استفاده می‌شود |
| `core/utils/AppUtils.kt` | `calculateDiscountPrice(price, percent)` | منطق تخفیف به سرویس پروموکدها و اشتراک منتقل شده |
| `core/utils/Extension.kt` | `Instant.toHumanReadable()` | بدون فراخواننده |
| `core/utils/Extension.kt` | `String.toUUIDOrNull()` | بدون فراخواننده |
| `core/utils/Extension.kt` | `String.toLocalDateTime()` | بدون فراخواننده |
| `core/utils/Extension.kt` | `String.toLocalDate()` | بدون فراخواننده |
| `core/utils/Extension.kt` | `LocalDate.Companion.of(y, m, d)` | بدون فراخواننده |
| `core/utils/ImageSaver.kt` | `saveImage()`, `deleteImage()`, `moveImage()` | آپلود فایل‌ها مستقیماً در `AdminMediaRouting` پیاده شده است |
| `core/utils/DatabaseHelper.kt` | `dropAllTables()`, `createAllTables()`, `resetAllTables()`, `clearAllData()` | ابزار رهاشده بدون کاربرد اجرایی |
| `core/utils/ImageData.kt` | `parseMultipartData()`, `parseMultipartDataWithFiles()`, `parseJsonElement()`, `injectFiles()` | رفلکشن‌های بدون مصرف |

### ۲.۲. ماژول ابزاری کلاینت (`utils/src/commonMain/kotlin/ir/aispeaking/utils/`)
| فایل | نام تابع / متد | وضعیت |
|---|---|---|
| `Extension.kt` | `String.safeToInt(): Int` | بدون فراخواننده در کلاینت یا سرور |
| `Extension.kt` | `String.addSpaces(sliceSize): String` | فقط توسط کامپوننت منسوخ `SpaceTransformation` صدا زده می‌شد |
| `Extension.kt` | `fun <T> immutableListOf(): ImmutableList<T>` (بدون آرگومان) | اورلود بی‌استفاده |
| `MyUtils.kt` | `MyUtils.generateStringUUID(): String` | کل آبجکت `MyUtils` بدون رفرنس است |
| `MyUtils.kt` | `MyUtils.generateUUID(): Uuid` | بدون رفرنس |
| `app_mode/AppMode.kt` | مقدار `ApplicationMode` | متغیر استاتیک بی‌استفاده در کل اپلیکیشن |
| `time/TimeExtension.kt` | `String?.toInstant(): Instant` | بدون فراخواننده |
| `time/TimeExtension.kt` | `String?.toInstantOrNull(): Instant?` | بدون فراخواننده |
| `time/TimeExtension.kt` | `Instant.toReadableString(): String` | بدون فراخواننده |

### ۲.۳. ماژول‌های دامنه، داده و شبکه (`domain/`, `data/`, `network/`)
| فایل / ماژول | نام تابع یا کلاس | شرح وضعیت |
|---|---|---|
| `domain/usecase/health/CheckServerHealthUseCase.kt` | `operator fun invoke()` | لایه Health Check کلاینت در هیچ کجای اسپلش یا ویومدل‌ها صدا زده نمی‌شود |
| `data/repository/health/HealthRepositoryImpl.kt` | `getHealth()` | بدون فراخواننده |
| `network/api/health/HealthApi.kt` | `getHealth()` | بدون فراخواننده |
| `domain/usecase/tts/TtsUseCases.kt` | `GetTtsVoicesUseCase.invoke()` | سیستم TTS مستقل کلاینت مصرف‌کننده‌ای ندارد چون صدا به صورت استریم SSE از سرور می‌آید |
| `domain/usecase/tts/TtsUseCases.kt` | `SynthesizeSpeechUseCase.invoke()` | بدون استفاده در UI |
| `domain/usecase/tts/TtsUseCases.kt` | `StreamAudioDirectUseCase.invoke()` | بدون استفاده در UI |
| `domain/usecase/stage/SendStageChatMessageUseCase.kt` | `SendStageChatMessageUseCase` | در `ChatViewModel` به عنوان فیلد تزریق شده ولی در کد هیچ‌کجا فراخوانی نمی‌شود (با استریم SSE جایگزین شده) |
| `network/api/stage/StageApi.kt` | `sendStageChatMessage()` | اندپوینت غیر استریمینگ چت بدون فراخواننده در UI |
| `data/mapper/paginate/PagingMapper.kt` | `PagingMeta.toPaging()` | سیستم صفحه‌بندی مدل‌های قدیمی بدون فراخواننده |
| `domain/model/paging/Paging.kt` | `Paging<T>.addMore()`, `nextPage()`, `firstPage()`, `errorHappened()` | توابع الحاقی صفحه‌بندی بدون استفاده |
| `domain/model/level/LanguageGroupLevel.kt` | `LanguageGroupLevel.copy()` | مدل گروه‌بندی پایه‌ای/متوسط/پیشرفته سناریوهای قدیمی |
| `domain/model/level/LanguageLevel.kt` | `LanguageLevel?.isInLevelGroup()` و `getGroup()` | توابع بدون استفاده |
| `sharedUI/ui/extension/DataExtension.kt` | `LanguageGroupLevel.name()` و `LanguageLevel.descriptionToPersian()` | بدون استفاده |

### ۲.۴. کامپوننت‌ها و اکستنشن‌های رابط کاربری (`sharedUI/`)
| کامپوننت / فایل | توابع / کلاس‌ها | شرح وضعیت |
|---|---|---|
| `ui/viewmodel/BaseViewModel.kt` | کل کلاس `BaseViewModel` و متدهای `setState`, `setUiMessage`, `setUiNavigation` | تمام ۷ ویومدل اصلی اپلیکیشن مستقیماً از `androidx.lifecycle.ViewModel` ارث‌بری دارند و این کلاس کاملاً بلااستفاده است |
| `ui/viewmodel/MviModelInterface.kt` | `UiState`, `UiEvent`, `UiNavigation` | اینترفیس‌های مارکر قدیمی |
| `ui/core/ui_message/UiMessageScreen.kt` | `UiMessageScreen`, `UiMessage`, `AppError.toUiMessage` | کامپوننت پیام‌های سیستم وابسته به `BaseViewModel` |
| `ui/validation/` (کل پکیج) | `IsDigitRule`, `MaxLengthRule`, `MobileNumberRule`, `NoSpacesRule`, `NotEmptyRule`, `NotPersianRule`, `InputField` | سیستم اعتبارسنجی فرم‌ها که در صفحه لاگین یا پروفایل هیچ استفاده‌ای از آن نشده است |
| `ui/extension/UiExtensions.kt` | `Dp.toCorner()` | بدون استفاده |
| `ui/extension/UiExtensions.kt` | `LazyListState.OnBottomReached()` و `LazyGridState.OnBottomReached()` | فقط در کامپوننت‌های منسوخ `SwipeList` و `SwipeGrid` صدا زده شده‌اند |
| `ui/extension/UiExtensions.kt` | `Dp.toComposePx()` و `Int.toComposeDp()` | بدون استفاده |
| `ui/extension/UiExtensions.kt` | `String.toPrice()` | بدون استفاده |
| `ui/extension/UiExtensions.kt` | `Modifier.dashedBorder()` و `DashedDivider()` | بدون استفاده |
| `ui/extension/Extensions.kt` | `immutableSetOf()` (هر دو اورلود) | بدون استفاده |
| `ui/game/GameKit.kt` | `Modifier.dimmed(enabled: Boolean)` | بدون استفاده |
| `ui/core/icon/AppIcon.kt` | `MeetMenuIcon()` | منسوخ |
| `ui/core/icon/AppIcon.kt` | `AppIcon(imageVector: ImageVector, ...)` | تمام فراخوانی‌ها از نسخه `DrawableResource` استفاده می‌کنند |
| `ui/utils/lifecycle/LifecycleAwareComponent.kt` | `OnResume()`, `OnPause()`, `OnDestroy()` | بدون فراخواننده |
| `ui/remember/rememberPermissionGrant.kt` | `rememberPermissionGrant()` | بدون فراخواننده |
| `ui/permission/` | `PerPermissionScreen`, `PermissionItem`, `AudioPermission`, `PostNotificationPermission` | دسترسی میکروفون مستقیماً در کنترلر صدا مدیریت می‌شود |
| `ui/core/horizontal_pager/AppHorizontalPager.kt` | `AppHorizontalPager()`, `findCenterPosition()` | بدون استفاده در هیچ صفحه‌ای از اپ |
| `ui/core/list/SwipeList.kt` و `SwipeGrid.kt` | `SwipeList()`, `SwipeGrid()` | بدون استفاده در هیچ صفحه‌ای از اپ |
| `ui/core/divider/VerticalDashDivider.kt` و `HorizontalDashDivider.kt` | `VerticalDashDivider()`, `HorizontalDashDivider()` | بدون استفاده |
| `ui/core/divider/TextDivider.kt` | `TextDivider()`, `CustomSpacer()` | بدون استفاده |
| `ui/core/text/SelectableText.kt` | `SelectableText()` (۱۶۹ خط کد) | بدون استفاده |
| `ui/core/text/AutoScrollingText.kt` | `AutoScrollingText()` (۱۲۳ خط کد) | بدون استفاده |
| `ui/core/text/HeadlineTextPreview.kt` | `HeadlineTextPreview()` | فایل آزمایشی رها شده |
| `ui/core/avatar/ShowAvatarsDialog.kt` | `ShowAvatarsDialog()` | با `AvatarPickerDialog` در فیچر پروفایل جایگزین شده است |
| `ui/utils/shape/BubbleShape.kt` | `TooltipShape` (۹۹ خط محاسبات کانوَس) | بدون استفاده |
| `ui/core/tab/AppTab.kt` | `AppTab()`, `AppVerticalTab()`, `UiTab` | تب‌های قدیمی بدون استفاده |
| `ui/core/input/SpaceTransformation.kt` | `SpaceTransformation` | بدون استفاده |
| `ui/core/unauthorized/UnauthorizedContent.kt` | `UnauthorizedContent()` | بدون استفاده |
| `ui/core/not_access/ReLoginContent.kt` | `ReAuthContent()` | بدون استفاده |
| `ui/core/loading/PageLoading.kt` | `PageLoading()` | بدون استفاده (لودینگ‌های اختصاصی در صفحات استفاده می‌شود) |
| `ui/core/indicator/PageIndicatorView.kt` | `PageIndicatorView()`, `PageIndicator()` | بدون استفاده |
| `ui/core/empty/EmptyContent.kt` | `EmptyContent()` | فقط توسط لیست‌های منسوخ `SwipeList` استفاده می‌شد |
| `ui/core/app_box/AppBox.kt` | `AppBox()` | بدون استفاده |
| `ui/core/space/VerticalSpace.kt` | `VerticalSpace()`, `HorizontalSpace()` | بدون استفاده در صفحات اصلی |
| `ui/lottie/LottieLoader.kt` | `LottieLoader()` | انیمیشن‌های ضبط صدا به Canvas منتقل شده‌اند و کامپوننت Lottie بی‌استفاده است |

---

## ۳. جمع‌بندی و دسته‌بندی اولویت‌های پاکسازی

| اولویت | بخش | تاثیر پاکسازی |
|---|---|---|
| **بحرانی (کاهش حجم فوق‌العاده)** | فایل‌های حجیم ریشه سرور (`server/*.tar.gz`, `*.sql`, `*.csv`) | آزادسازی حدود **۴۰۰ مگابایت** فضای ریپازیتوری |
| **بالا (کاهش حجم کلاینت و سرور)** | تصاویر استاتیک منسوخ در `server/static/` و دراوئبل‌های `sharedUI` (`guide_*`, `ic_lightener*`, `scenario/`) | کاهش حدود **۳۰ تا ۴۰ مگابایت** از حجم استاتیک و سورس |
| **متوسط (خوانایی و معماری)** | کلاس‌های پایه و فریم‌ورک‌های مرده (`BaseViewModel`, `MviModelInterface`, `UiMessage`, `Validation`, `ImageData`, `DatabaseHelper`) | کاهش بیش از **۱۵۰۰ خط کد مرده و اضافه** |
| **کم (اصلاح هویت)** | تغییر شناسه و عنوان‌های `AiSpeakingMultiplatform` در لانچرهای اندروید، دسکتاپ و وب | یکپارچگی برندینگ و رفع مغایرت هویتی اپ |

این گزارش به عنوان مستند مرجع پاکسازی کدبیس (Clean-Slate & Refactoring) ذخیره شده و تیم می‌تواند بر اساس اولویت‌بندی فوق نسبت به حذف فایل‌های اضافه اقدام نماید.
