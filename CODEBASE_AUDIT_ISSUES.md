# لیست مشکلات و موارد ناقص یا هاردکد شده در پروژه (Audit Report)

این سند شامل تمامی مواردی است که در بررسی کدهای کلاینت و سرور شناسایی شده‌اند و توسط ایجنت‌ها ناقص، هاردکد شده، یا به شکل غیراستاندارد پیاده‌سازی شده‌اند.
*(موارد مربوط به آواتارها و نسخه دسکتاپ طبق دستور در این گزارش گنجانده نشده‌اند).*

---

### ۱. باگ اساسی صدا: عدم تطابق شناسه‌های صدا در دیتابیس با مدل Kokoro و پخش صدای زن آمریکایی برای مردان بریتانیایی
* **فایل‌ها**:
  * [`server/src/main/kotlin/ir/speaking/feature/stage/db/StageSeedData.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stage/db/StageSeedData.kt)
  * [`server/src/main/kotlin/ir/speaking/feature/tts/model/TtsModels.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/tts/model/TtsModels.kt)
* **شرح مشکل**:
  * در مدل Kokoro-82M تنها شناسه‌های مشخصی برای زبان انگلیسی تعریف شده است:
    * زن بریتانیایی: `bf_emma`, `bf_isabella`
    * مرد بریتانیایی: `bm_george`, `bm_lewis`
    * زن آمریکایی: `af`, `af_bella`, `af_nicole`, `af_sarah`, `af_sky`
    * مرد آمریکایی: `am_adam`, `am_michael`
  * اما در `StageSeedData.kt`، شناسه‌های نامعتبر و ساختگی تعریف شده است:
    * `"bf_alice"`, `"bf_lily"`, `"bm_daniel"`
  * در سرور در [`StageChatService.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt#L48)، چون این شناسه‌ها پیدا نمی‌شوند، سیستم به صدای شناسه `0` (یعنی `af` - صدای زن آمریکایی Heart) فال‌بک می‌زند.
  * **پیامد**: کاراکترهای مرد بریتانیایی مانند **لیام (مرحله ۴)**، **جیمز (مرحله ۷)** و **جک (مرحله ۱۲)** با صدای زن آمریکایی صحبت می‌کنند. همچنین کاراکترهای زن بریتانیایی نیز با لهجه آمریکایی پخش می‌شوند.

---

### ۲. هاردکد تسک‌های مرحله به `listOf(0, 1)` در سرور
* **فایل**:
  * [`server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt#L85)
* **شرح مشکل**:
  * در خط ۸۵ سرویس چت آمده است:
    ```kotlin
    finishTaskIndexes = if (turnResult.isObjectiveDone) listOf(0, 1) else emptyList()
    ```
  * ایجنت فرض کرده که تمام مراحل صرفاً دو تسک دارند (شاخص‌های ۰ و ۱) و به صورت هاردکد در صورت تحقق هدف، این دو شاخص را به کلاینت بازمی‌گرداند؛ در حالی که هر مرحله ممکن است تعداد یا عناوین تسک متفاوتی داشته باشد یا نیازمند پیشرفت مرحله‌به‌مرحله باشد.

---

### ۳. ناقص بودن منطق فال‌بک گفتگو (`generateNextTurn`) برای مراحل ۳ تا ۱۵ در سرور
* **فایل**:
  * [`server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt#L365-L485)
* **شرح مشکل**:
  * تابع `generateNextTurn` نقش فال‌بک آفلاین/اضطراری را دارد تا اگر اتصال هوش مصنوعی (OpenAI) با خطا یا تاخیر مواجه شد، مکالمه قطع نشود.
  * این تابع فقط برای **مرحله ۱ و ۲** منطق سناریو دارد. برای **مراحل ۳ تا ۱۵** هیچ شرط و شاخه‌ای نوشته نشده و مستقیماً به پیام جنریک زیر می‌رود:
    ```kotlin
    Triple(
        "Thank you for sharing that! We have accomplished the conversation goal for this stage. You can finish or continue practicing.",
        "ممنون از همراهی شما! هدف مکالمه در این مرحله با موفقیت محقق شد. می‌توانید مرحله را به پایان برسانید یا به تمرین ادامه دهید.",
        userTurnCount >= 3
    )
    ```
  * **پیامد**: در صورت بروز خطای شبکه یا AI در مراحل ۳ تا ۱۵، شخصیت کاراکتر به کلی از بین رفته و پیام نامربوط پایان مرحله نمایش داده می‌شود.

---

### ۴. ناقص بودن منطق فال‌بک راهنما (`fallbackHint`) برای مراحل ۳ تا ۱۵ در سرور
* **فایل**:
  * [`server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stage/service/StageChatService.kt#L675-L774)
* **شرح مشکل**:
  * در صورتی که درخواست تولید راهنما به هوش مصنوعی با خطا مواجه شود، تابع `fallbackHint` فراخوانی می‌شود.
  * این تابع تنها برای مراحل ۱ و ۲ پیشنهادهای متنی متناسب با سناریو دارد. برای مراحل ۳ تا ۱۵ صرفاً جمله عمومی زیر را بدون توجه به موقعیت مکالمه بازمی‌گرداند:
    ```kotlin
    if (isBeginner) {
        HintResponse(
            suggestionEn = "Can you please help me?",
            explanationFa = "می‌توانید خیلی ساده بپرسید: «Can you please help me?»"
        )
    } else {
        HintResponse(
            suggestionEn = "Could you please explain what I should do next?",
            explanationFa = "می‌توانید مؤدبانه از هم‌صحبت خود بخواهید مرحله بعدی را برایتان توضیح دهد."
        )
    }
    ```

---

### ۵. هک جایگزینی پورت ۸۰۸۱ با ۸۰۸۰ در نسخه وب
* **فایل**:
  * [`webApp/src/commonMain/resources/index.html`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/webApp/src/commonMain/resources/index.html#L46)
* **شرح مشکل**:
  * در پخش‌کننده صوتی جاوااسکریپت خط ۴۶ نوشته شده است:
    ```javascript
    var fullUrl = url.startsWith("http") ? url : (window.location.origin.replace(":8081", ":8080") + url);
    ```
  * این تبدیل پورت فقط برای حالت توسعه محلی با پورت پیش‌فرض ۸۰۸۱ کار می‌کند. اگر وب‌اپ روی پورتی غیر از ۸۰۸۱ (مثلاً ۸۰۸۲ یا ۳۰۰۰) یا پشت دامین/Reverse Proxy بدون شماره پورت اجرا شود، جایگزینی انجام نمی‌شود و درخواست فایل‌های صوتی به سرور وب فرستاده شده و خطای ۴۰۴ می‌دهد.

---

### ۶. هاردکد کدهای تخفیف در کلاینت و شبیه‌سازی صوری خرید در پی‌وال نقشه
* **فایل‌ها**:
  * [`feature/subscription/src/commonMain/kotlin/ir/aispeaking/subscription/SubscriptionViewModel.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/feature/subscription/src/commonMain/kotlin/ir/aispeaking/subscription/SubscriptionViewModel.kt#L134-L164)
  * [`feature/main/src/commonMain/kotlin/ir/aispeaking/main/SubscriptionPaywallSheet.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/feature/main/src/commonMain/kotlin/ir/aispeaking/main/SubscriptionPaywallSheet.kt#L178)
* **شرح مشکل**:
  * اعتبارسنجی کدهای تخفیف به صورت دستی در یک بلوک `when` در کلاینت هاردکد شده است (`GOLD20`, `VIP`, `NOWRUZ`, ...) زیرا اندپوینت اختصاصی جهت استعلام و اعتبارسنجی تخفیف در سرور پیاده‌سازی نشده است.
  * در کامپوزبل باقیمانده `SubscriptionPaywallSheet.kt` دکمه خرید مستقیماً کامنت `// Simulate subscription activation` خورده و بدون فراخوانی ای‌پی‌آی خرید را موفقیت‌آمیز فرض می‌کند (طبق سند spec 004 این فایل باید کلاً حذف شده و به صفحه `SubscriptionScreen` متصل می‌شد).

---

### ۷. عدم اتصال موتور استریم STT سرور به صفحه چت کلاینت
* **فایل‌ها**:
  * [`network/src/commonMain/kotlin/ir/aispeaking/network/websocket/SttWebSocketClient.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/network/src/commonMain/kotlin/ir/aispeaking/network/websocket/SttWebSocketClient.kt)
  * [`data/src/commonMain/kotlin/ir/aispeaking/data/repository/stt/SttRepositoryImpl.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/data/src/commonMain/kotlin/ir/aispeaking/data/repository/stt/SttRepositoryImpl.kt)
  * [`server/src/main/kotlin/ir/speaking/feature/stt/service/SttService.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/server/src/main/kotlin/ir/speaking/feature/stt/service/SttService.kt)
* **شرح مشکل**:
  * زیرساخت استریم وب‌سوکت تبدیل گفتار به متن (Sherpa-ONNX Zipformer) هم در سرور و هم در لایه‌های دیتا/شبکه کلاینت پیاده‌سازی شده است.
  * اما در صفحه چت (`ChatViewModel`) ایجنت این زیرساخت را متصل نکرده و مستقیماً به API نیتیو مرورگر و اندروید تکیه کرده است؛ در نتیجه زیرساخت نوشته‌شده برای STT بدون استفاده رها شده است.

---

### ۸. کامنت TODO و آیکون اشتباه در اعلان افزایش سطح
* **فایل**:
  * [`sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/core/notif/LevelUpOverlayNotification.kt`](file:///Users/mahdi/StudioProjects/AISpeakingPlus/sharedUI/src/commonMain/kotlin/ir/aispeaking/sharedui/ui/core/notif/LevelUpOverlayNotification.kt#L109)
* **شرح مشکل**:
  * کامنت `// TODO replace icon for star` در خط ۱۰۹ باقی مانده و به جای آیکون ستاره از `ic_done` با توضیح اشتباه `"Gift Icon"` استفاده شده است.
