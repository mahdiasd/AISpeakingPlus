# Contract: Subscription & Paywall API Specifications

**Base URL**: `/api/v2/subscriptions`  
**OpenAPI Specification**: Living OpenAPI 3.0.3 compliant (`/openapi.json`)

---

## ۱. `GET /api/v2/subscriptions/plans`

دریافت لیست بسته‌های اشتراک فعال همراه با جزئیات قیمت‌گذاری، روزهای اعتبار و تخفیف‌ها.

### سطح دسترسی
عمومی (Public - بدون نیاز به توکن احراز هویت)

### Query Parameters
*هیچ‌کدام*

### پاسخ موفق: `200 OK`
```json
{
  "success": true,
  "message": "Subscription plans retrieved",
  "data": [
    {
      "id": "plan-1m",
      "type": "1_MONTH",
      "titleFa": "اشتراک ۱ ماهه",
      "durationDays": 30,
      "priceTomans": 199000,
      "discountPercent": 0,
      "badge": null
    },
    {
      "id": "plan-3m",
      "type": "3_MONTHS",
      "titleFa": "اشتراک ۳ ماهه",
      "durationDays": 90,
      "priceTomans": 499000,
      "discountPercent": 15,
      "badge": "محبوب‌ترین"
    },
    {
      "id": "plan-6m",
      "type": "6_MONTHS",
      "titleFa": "اشتراک ۶ ماهه",
      "durationDays": 180,
      "priceTomans": 899000,
      "discountPercent": 25,
      "badge": "بهترین ارزش"
    }
  ]
}
```

---

## ۲. `GET /api/v2/subscriptions/status`

بررسی وضعیت کنونی اشتراک، عنوان بسته فعال، تاریخ انقضا و روزهای باقیمانده برای کاربر لاگین‌شده.

### سطح دسترسی
نیاز به احراز هویت (`Authorization: Bearer <token>`)

### پاسخ موفق: `200 OK` (کاربر دارای اشتراک فعال)
```json
{
  "success": true,
  "message": "Subscription status retrieved",
  "data": {
    "isSubscriber": true,
    "planType": "3_MONTHS",
    "expiresAt": "2026-12-31T23:59:59Z",
    "remainingDays": 86
  }
}
```

### پاسخ موفق: `200 OK` (کاربر فاقد اشتراک یا منقضی‌شده)
```json
{
  "success": true,
  "message": "Subscription status retrieved",
  "data": {
    "isSubscriber": false,
    "planType": null,
    "expiresAt": null,
    "remainingDays": 0
  }
}
```

### خطاهای محتمل
- **`401 Unauthorized`**:
  ```json
  {
    "success": false,
    "message": "User authentication required",
    "data": null
  }
  ```

---

## ۳. `POST /api/v2/subscriptions/subscribe`

فعال‌سازی یا تمدید مستقیم پلن اشتراک انتخاب‌شده توسط کاربر لاگین‌شده.

### سطح دسترسی
نیاز به احراز هویت (`Authorization: Bearer <token>`)

### ساختار بدنه درخواست (Request Body)
```json
{
  "planId": "plan-3m",
  "promoCode": "GOLD20"
}
```

### پاسخ موفق: `200 OK`
```json
{
  "success": true,
  "message": "Subscription activated successfully",
  "data": {
    "isSubscriber": true,
    "planType": "3_MONTHS",
    "expiresAt": "2027-01-06T14:30:00Z",
    "remainingDays": 90,
    "message": "اشتراک ۳ ماهه شما با موفقیت فعال گردید"
  }
}
```

### خطاهای محتمل
- **`400 Bad Request`**: شناسه پلن نامعتبر است.
  ```json
  {
    "success": false,
    "message": "شناسه پلن انتخابی نامعتبر است",
    "data": null
  }
  ```
- **`401 Unauthorized`**: ورود به حساب الزامی است.
  ```json
  {
    "success": false,
    "message": "User authentication required",
    "data": null
  }
  ```
