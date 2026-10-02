# قرارداد وب‌سرویس: مدیریت مراحل (Admin Stages API)

**نسخه**: `2.0.0`  
**آدرس پایه**: `/api/admin/stages`  
**احراز هویت**: توکن Bearer (`admin-jwt`, نقش `ROLE_ADMIN` یا `ROLE_SUPER_ADMIN`)  

---

## ۱. فهرست کلیه Stageها

دریافت لیست تمام Stageها با ترتیب `orderIndex` و وضعیت انتشار.

- **مسیر**: `GET /api/admin/stages`
- **پارامترهای جستجو**: `status` (اختیاری: `ALL`, `DRAFT`, `PUBLISHED`)

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "لیست مراحل دریافت شد",
  "data": {
    "total": 5,
    "stages": [
      {
        "id": "stage-01-tehran-departure",
        "orderIndex": 1,
        "title": "Tehran Airport Departure",
        "titleFa": "خروج از فرودگاه امام تهران",
        "briefing": "You are at the check-in desk at Tehran airport. Request a window seat.",
        "briefingFa": "شما در باجه پذیرش فرودگاه هستید و باید کارت پرواز بگیرید.",
        "targetObjective": "Complete airport check-in, secure a window seat, and obtain your boarding pass",
        "targetObjectiveFa": "انجام فرآیند پذیرش فرودگاه، تحویل بار، انتخاب صندلی مناسب کنار پنجره و دریافت کارت پرواز",
        "characterBehavior": "Sarah is polite but busy airline agent. She verifies luggage weight, asks about seat preference, but won't offer a window seat automatically unless the traveler explicitly requests or inquires.",
        "backgroundUrl": "/uploads/stages/bg_1_vertical.webp",
        "characterName": "Sarah",
        "characterAvatarUrl": "/uploads/avatars/sarah.webp",
        "characterGender": "Woman",
        "voiceId": "af_sarah",
        "initialSpeaker": "Model",
        "maxTurns": 10,
        "status": "PUBLISHED",
        "createdAt": "2026-09-18T10:00:00Z"
      }
    ]
  }
}
```

---

## ۲. ایجاد یا به‌روزرسانی Stage (ثبت فرم یا JSON خروجی AI)

ایجاد مرحله جدید یا ویرایش مرحله موجود بر اساس مقادیر ارسالی از فرم یا باکس JSON.

- **مسیر**: `POST /api/admin/stages`

### بدنه درخواست (Request Body)
```json
{
  "id": "stage-06-london-cafe",
  "orderIndex": 6,
  "title": "Ordering Coffee in London",
  "titleFa": "سفارش قهوه در کافه لندن",
  "briefing": "You walked into a local cafe in Covent Garden. You are tired and need to contact your host.",
  "briefingFa": "وارد یک کافه محلی در کاونت گاردن شده‌اید. خسته‌اید و برای هماهنگی با میزبان نیاز به دسترسی اینترنت دارید.",
  "targetObjective": "Order a warm beverage and obtain cafe WiFi credentials",
  "targetObjectiveFa": "سفارش یک نوشیدنی گرم برای رفع خستگی و دریافت رمز اینترنت وای‌فای کافه",
  "characterBehavior": "Oliver is a fast-paced London barista. He takes orders concisely and provides the WiFi password on the paper receipt only if asked politely.",
  "backgroundUrl": "/uploads/stages/bg_cafe_vertical.webp",
  "characterName": "Barista Oliver",
  "characterAvatarUrl": "/uploads/avatars/oliver.webp",
  "characterGender": "Man",
  "voiceId": "am_adam",
  "initialSpeaker": "Model",
  "maxTurns": 12,
  "status": "PUBLISHED"
}
```

> **الزامات دارایی و فیلدها**:
> ۱. تصویر `backgroundUrl` باید عمودی (Portrait با نسبت ۹:۱۶ مناسب گوشی) باشد.
> ۲. هر دو فیلد `targetObjective` و `targetObjectiveFa` اجباری بوده و به کاربر نمایش داده می‌شوند.
> ۳. فیلد `characterBehavior` برای تزریق رفتار، اصطکاک و منطق مقاومت به موتور پرامپت هوش مصنوعی استفاده می‌شود.


### پاسخ موفق (201 Created)
```json
{
  "status": 201,
  "message": "مرحله با موفقیت ثبت شد",
  "data": {
    "id": "stage-06-london-cafe",
    "orderIndex": 6,
    "title": "Ordering Coffee in London",
    "status": "PUBLISHED"
  }
}
```

---

## ۳. حذف یا آرشیو Stage

- **مسیر**: `DELETE /api/admin/stages/{id}`

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "مرحله با موفقیت حذف یا آرشیو شد"
}
```
