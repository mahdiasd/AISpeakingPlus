# Quickstart & Verification Guide: Multi-Market Payment Integration

**Feature**: `006-multi-market-payment`  
**Date**: ۱۴۰۵/۰۷/۲۶ (2026-10-17)  
**Spec**: [spec.md](./spec.md) | **API Contracts**: [contracts/payment-api.md](./contracts/payment-api.md)

این سند مراحل گام‌به‌گام برای اعتبارسنجی و تست سیستم تشخیص پلتفرم، پرداخت چندمارکته، اعمال تخفیف و همگام‌سازی سرور را تشریح می‌کند.

---

## ۱. پیش‌نیازها و محیط اجرا (Prerequisites)

1. **سرور محلی (Ktor)**:
   - اجرای سرور روی پورت ۸۰۸۰:
     ```bash
     ./gradlew :server:run
     ```
   - اطمینان از دسترسی به دیتابیس PostgreSQL و اعمال ساختار جدول `market_payment_transactions`.
2. **پروژه اندروید (Client)**:
   - دستگاه فیزیکی یا شبیه‌ساز اندروید دارای نسخه تست کافه بازار یا مایکت.
   - نام بسته اپلیکیشن: `ir.aispeaking.androidApp`.

---

## ۲. سناریوهای تست و اعتبارسنجی (Validation Scenarios)

### سناریوی ۱: تست تشخیص مارکت نصاب در کلاینت (Installer Detection)
1. برنامه را از طریق دستور ADB با پارامتر نصاب شبیه‌سازی کنید:
   - **کافه بازار**:
     ```bash
     adb install -i com.farsitel.bazaar androidApp/build/outputs/apk/debug/androidApp-debug.apk
     ```
   - **مایکت**:
     ```bash
     adb install -i ir.mservices.market androidApp/build/outputs/apk/debug/androidApp-debug.apk
     ```
   - **نصب مستقیم (سایدلود)**:
     ```bash
     adb install androidApp/build/outputs/apk/debug/androidApp-debug.apk
     ```
2. وارد صفحه اشتراک شوید (`SubscriptionScreen`).
3. **نتیجه مورد انتظار**:
   - در حالت اول، سیستم `AppMarket.CAFE_BAZAAR` را فعال می‌کند.
   - در حالت دوم، سیستم `AppMarket.MYKET` را فعال می‌کند.
   - در حالت سوم، سیستم `AppMarket.DIRECT_WEB` را فعال کرده و دکمه پرداخت مستقیم را نمایش می‌دهد.

---

### سناریوی ۲: تست اعمال کد تخفیف در مایکت (روزهای هدیه مازاد)
1. در نسخه نصب‌شده از مایکت، وارد صفحه اشتراک شوید.
2. کد تخفیف معتبر (مثلاً `TEST20`) را وارد کرده و دکمه اعمال را بزنید.
3. **نتیجه مورد انتظار**:
   - به جای کسر قیمت از فاکتور مایکت، پیام صمیمانه فارسی ظاهر می‌شود:  
     *«با توجه به ساختار پرداخت، معادل ۲۰٪ به مدت زمان اشتراک شما (مثلاً ۱۸ روز هدیه مازاد) افزوده خواهد شد.»*
   - هیچ نامی از سایر مارکت‌ها دیده نمی‌شود.
   - قیمت فاکتور روی دکمه خرید برابر با قیمت اصلی پلن در پنل مایکت باقی می‌ماند.

---

### سناریوی ۳: تست اعمال کد تخفیف در کافه بازار (Dynamic Discount)
1. در نسخه نصب‌شده از کافه بازار، وارد صفحه اشتراک شوید.
2. کد تخفیف را وارد کنید.
3. **نتیجه مورد انتظار**:
   - توکن JWT تخفیف پویا توسط سرور صادر می‌شود.
   - مبلغ نهایی تخفیف‌خورده روی فاکتور رسمی بازار نشان داده شده و پرداخت با قیمت تخفیف‌یافته آغاز می‌گردد.

---

### سناریوی ۴: تست اعتبارسنجی سرور و جلوگیری از Replay Attack
1. یک درخواست نمونه تایید خرید به اندپوینت ارسال کنید:
   ```bash
   curl -X POST http://localhost:8080/api/v2/subscriptions/verify-market-purchase \
     -H "Authorization: Bearer <VALID_USER_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
       "market": "CAFE_BAZAAR",
       "planId": "plan-3m",
       "sku": "plan-3m",
       "purchaseToken": "test_purchase_token_12345",
       "developerPayload": "test_payload"
     }'
   ```
2. بار اول: وضعیت ۲۰۰ و اضافه شدن ۹۰ روز به اشتراک کاربر بازمی‌گردد.
3. ارسال مجدد دقیقاً همان `purchaseToken`:
   - **نتیجه مورد انتظار**: دریافت خطای ۴۰۰ یا ۴۰۹ مبنی بر استفاده قبلی از توکن و عدم تغییر در روزهای اشتراک (خنثی‌سازی تقلب).
