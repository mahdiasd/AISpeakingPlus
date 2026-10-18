# Research & Technical Decisions: Multi-Market Payment Integration

**Feature**: `006-multi-market-payment`  
**Date**: ۱۴۰۵/۰۷/۲۶ (2026-10-17)  
**Spec**: [spec.md](./spec.md)

---

## ۱. تصمیم‌های معماری و فنی کلیدی (Technical Decisions)

### ۱.۱. تشخیص پلتفرم و منبع نصب (Platform & Installer Detection)
- **تصمیم**:
  - در ماژول `domain` یک اینترفیس `PlatformMarketDetector` تعریف می‌شود:
    ```kotlin
    enum class AppMarket { CAFE_BAZAAR, MYKET, DIRECT_WEB, UNSUPPORTED }
    interface PlatformMarketDetector {
        fun detectCurrentMarket(): AppMarket
    }
    ```
  - در پلتفرم اندروید (`androidApp` یا `data/androidMain`) با بررسی `context.packageManager.getInstallerPackageName(packageName)` یا در API 30+ با `getInstallSourceInfo`:
    - پکیج `com.farsitel.bazaar` -> `AppMarket.CAFE_BAZAAR`
    - پکیج `ir.mservices.market` -> `AppMarket.MYKET`
    - در غیر این صورت (سایدلود، دانلود مستقیم APK، کروم، واتساپ، ...) -> `AppMarket.DIRECT_WEB`
  - در پلتفرم‌های دسکتاپ و وب -> مستقیماً `AppMarket.DIRECT_WEB`.
- **دلیل انتخاب**: سازگاری کامل با تمیزی معماری (Clean Architecture)، تست‌پذیری با Fake/Mock، و رفتار قطعی بدون کرش.
- **جایگزین‌های بررسی‌شده**:
  - استفاده از Build Flavor جداگانه برای هر مارکت: با توجه به درخواست کاربر مبنی بر اینکه «یک اپلیکیشن apk نصب شده و باید خود سیستم تشخیص بده از کافه بازار نصب کرده یا مایکت»، تک‌APK هوشمند بسیار منعطف‌تر است.

---

### ۱.۲. استراتژی یکپارچه‌سازی پرداخت کلاینت اندروید (Poolakey و مایکت IAB)
- **تصمیم**:
  - **کافه بازار**: استفاده از کتابخانه رسمی پولکی کاتلین (`ir.cafebazaar.poolakey:poolakey:2.2.0` یا ارتباط تمیز AIDL/Poolakey) برای برقراری ارتباط IPC با سرویس پرداخت بازار.
  - **مایکت**: استفاده از پیاده‌سازی رسمی `MyketIabHelper` بر پایه استاندارد Google Play IAB V3 و اتصال به `ir.mservices.market.InAppBillingService.BIND`.
  - یک درایور مشترک کلاینت به نام `MarketBillingGateway` پیاده‌سازی می‌شود که متدهای استانداردی دارد:
    - `startConnection()`
    - `purchaseProduct(activity, sku, developerPayload, discountToken)`
    - `queryPurchases()`
- **دلیل انتخاب**: مستندات جامع ارائه‌شده در مخزن (`CAFEBAZAAR_IAP_GUIDE.md` و `MYKET_IAP_GUIDE.md`) دقیقاً این دو استاندارد را تشریح کرده‌اند و تضمین‌کننده ارتباط بدون باگ با هر دو مارکت هستند.

---

### ۱.۳. معماری اعمال کد تخفیف (Dynamic Discount در بازار و روزهای هدیه در مایکت)
- **تصمیم**:
  - **کافه بازار**: سرور هنگام اعتبارسنجی کد تخفیف در اندپوینت `POST /api/v2/subscriptions/validate-promo`، در صورتی که مارکت کاربر `CAFE_BAZAAR` باشد، یک توکن JWT امضاشده تخفیف پویا (Dynamic Discount Token) طبق فرمول بازار (`sub`, `sku`, `discount_price`, `exp`, `nonce`) امضا کرده و بازمی‌گرداند. کلاینت این توکن را به متد خرید بازار پاس می‌دهد.
  - **مایکت**: از آنجا که مایکت تخفیف پویا روی درگاه ندارد، سرور در پاسخ `validate-promo` تعداد روزهای هدیه مازاد (`bonusDays`) را محاسبه و اعلام می‌کند. کلاینت پیامی دوستانه به کاربر نمایش می‌دهد:  
    *«با توجه به ساختار پرداخت، معادل درصد تخفیف شما به مدت زمان اشتراک افزوده خواهد شد (مثلاً دریافت روزهای هدیه مازاد).»*  
    (بدون هرگونه اشاره به نام کافه بازار جهت رعایت الزامات انتشار مایکت).
- **دلیل انتخاب**: انطباق ۱۰۰٪ با بازخورد و تصمیم کاربر در جلسه شفاف‌سازی نیازمندی‌ها.

---

### ۱.۴. اعتبارسنجی و امن‌سازی سمت سرور (Server-to-Server Verification)
- **تصمیم**:
  - هیچ اشتراکی صرفاً با اعلام موفقیت کلاینت تایید و فعال نمی‌شود.
  - کلاینت پس از دریافت رسید، اطلاعات را به اندپوینت `POST /api/v2/subscriptions/verify-market-purchase` ارسال می‌کند:
    ```json
    {
      "market": "CAFE_BAZAAR", // یا "MYKET"
      "sku": "plan-3m",
      "purchaseToken": "...",
      "developerPayload": "...",
      "promoCode": "..."
    }
    ```
  - **سرور برای کافه بازار**: با استفاده از توکن پیشخان/OAuth2 به آدرس `https://pardakht.cafebazaar.ir/devapi/v2/api/validate/{package_name}/inapp/{sku}/purchases/{token}/` استعلام می‌گیرد.
  - **سرور برای مایکت**: با استفاده از `Myket-Api-Key` به آدرس `https://developer.myket.ir/api/applications/{package_name}/purchases/products/{sku}/tokens/{token}` استعلام می‌گیرد.
  - **کنترل Replay Attack**: ذخیره منحصربه‌فرد `purchaseToken` در جدول پایگاه‌داده با قفل یکتا (`unique index`). تلاش مجدد برای ارسال همان توکن خطا می‌دهد و مانع از فعال‌سازی چندباره اشتراک با یک توکن دزدی یا تکراری می‌شود.
- **دلیل انتخاب**: تضمین قطعی امنیت مالی، رعایت کامل قوانین پیشگیری از هک IAP در اندروید و انطباق با مستندات رسمی.

---

### ۱.۵. بازیابی خریدهای معلق (Pending Purchases Recovery)
- **تصمیم**:
  - در زمان باز شدن صفحه اشتراک (`SubscriptionViewModel.loadData`) یا راه‌اندازی اولیه برنامه، متد `queryPurchases` مارکت فعال صدا زده می‌شود. اگر خریدی وجود داشت که در دیتابیس محلی یا وضعیت اشتراک اعمال نشده بود، به صورت نامحسوس در پس‌زمینه به سرور ارسال می‌شود تا تایید شود.
- **دلیل انتخاب**: جلوگیری از نارضایتی مشتری در صورت قطع شدن اتصال اینترنت در مرحله بازگشت از درگاه بانک.
