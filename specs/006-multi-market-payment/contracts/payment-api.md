# API Contract: Payment & Subscription Verification

**Feature**: `006-multi-market-payment`  
**Base URL**: `/api/v2/subscriptions`  
**OpenAPI Version**: 3.0.3 Compatible

---

## ۱. اعتبارسنجی کد تخفیف با پشتیبانی مارکت (Validate Promo Code)

- **مسیر (Path)**: `POST /api/v2/subscriptions/validate-promo`
- **احراز هویت**: الزامی (`Bearer JWT`)
- **توضیحات**: کد تخفیف را برای پلن و مارکت مشخص اعتبارسنجی می‌کند. اگر مارکت کافه بازار باشد توکن تخفیف پویا تولید می‌کند و اگر مایکت باشد روزهای هدیه محاسبه می‌شود.

### بدنه درخواست (Request Body)
```json
{
  "promoCode": "NOROOZ1405",
  "planId": "plan-3m",
  "market": "CAFE_BAZAAR" // "CAFE_BAZAAR" | "MYKET" | "DIRECT_WEB"
}
```

### پاسخ موفقیت‌آمیز بازار (Response 200 - Cafe Bazaar)
```json
{
  "status": true,
  "message": "کد تخفیف ۲۰ درصدی با موفقیت اعمال شد",
  "data": {
    "promoCode": "NOROOZ1405",
    "discountPercent": 20,
    "discountAmountTomans": 99800,
    "originalPriceTomans": 499000,
    "finalPriceTomans": 399200,
    "dynamicDiscountToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", // توکن رسمی تخفیف پویا
    "bonusDays": 0,
    "userMessage": "تخفیف ۲۰ درصدی مستقیماً روی فاکتور کافه بازار اعمال گردید."
  }
}
```

### پاسخ موفقیت‌آمیز مایکت (Response 200 - Myket)
```json
{
  "status": true,
  "message": "کد تخفیف معتبر است",
  "data": {
    "promoCode": "NOROOZ1405",
    "discountPercent": 20,
    "discountAmountTomans": 99800,
    "originalPriceTomans": 499000,
    "finalPriceTomans": 499000,
    "dynamicDiscountToken": null,
    "bonusDays": 18, // ۲۰ درصد از ۹۰ روز به عنوان هدیه
    "userMessage": "با توجه به ساختار پرداخت، معادل ۲۰٪ به مدت زمان اشتراک شما (۱۸ روز هدیه) افزوده خواهد شد."
  }
}
```

---

## ۲. تایید نهایی و اعتبارسنجی خرید مارکت (Verify Market Purchase)

- **مسیر (Path)**: `POST /api/v2/subscriptions/verify-market-purchase`
- **احراز هویت**: الزامی (`Bearer JWT`)
- **توضیحات**: توکن خرید دریافتی از کلاینت را از سرور رسمی بازار یا مایکت استعلام کرده، ثبت می‌نماید و روزهای اشتراک را به حساب کاربر اضافه می‌کند.

### بدنه درخواست (Request Body)
```json
{
  "market": "CAFE_BAZAAR", // "CAFE_BAZAAR" | "MYKET"
  "planId": "plan-3m",
  "sku": "plan-3m",
  "purchaseToken": "bazaar_token_example_abcdef123456",
  "orderId": "bazaar_order_987654321",
  "developerPayload": "user_id_12345",
  "promoCode": "NOROOZ1405"
}
```

### پاسخ موفقیت (Response 200)
```json
{
  "status": true,
  "message": "اشتراک با موفقیت فعال و حساب شما ارتقا یافت",
  "data": {
    "isSubscriber": true,
    "planType": "3_MONTHS",
    "expiresAt": "2027-01-25T18:30:00Z",
    "remainingDays": 108, // ۹۰ روز پلن + ۱۸ روز هدیه در مایکت یا تمدید قبلی
    "transactionId": "tx_uuid_88776655"
  }
}
```

### پاسخ خطای امنیتی / توکن نامعتبر (Response 400 / 409)
```json
{
  "status": false,
  "message": "این رسید قبلاً استفاده شده است یا نامعتبر می‌باشد",
  "error": {
    "code": "INVALID_OR_DUPLICATE_PURCHASE_TOKEN",
    "developerMessage": "Purchase token already exists in market_payment_transactions"
  }
}
```

---

## ۳. ایجاد فاکتور پرداخت مستقیم تحت وب (Create Direct Web Payment)

- **مسیر (Path)**: `POST /api/v2/subscriptions/direct-checkout`
- **احراز هویت**: الزامی (`Bearer JWT`)
- **توضیحات**: برای نسخه‌های سایدلود / مستقیم یا محیط وب/دسکتاپ، لینک درگاه پرداخت مستقیم بانکی شاپرک را تولید می‌کند.

### بدنه درخواست (Request Body)
```json
{
  "planId": "plan-3m",
  "promoCode": "NOROOZ1405"
}
```

### پاسخ موفقیت (Response 200)
```json
{
  "status": true,
  "message": "شناسه پرداخت با موفقیت ایجاد شد",
  "data": {
    "paymentUrl": "https://gateway.aispeaking.ir/pay/tx_uuid_9988",
    "transactionId": "tx_uuid_9988",
    "amountTomans": 399200
  }
}
```
