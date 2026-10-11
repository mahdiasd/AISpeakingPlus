# قرارداد وب‌سرویس: اعطای دستی اشتراک (Admin Subscriptions API)

**نسخه**: `2.0.0`  
**آدرس پایه**: `/api/admin/users/{userId}/subscriptions`  
**احراز هویت**: توکن Bearer (`admin-jwt`)  

---

## ۱. اعطای دستی اشتراک بدون پرداخت مالی

ثبت اشتراک فعال با برچسب منبع `MANUAL_ADMIN`، شناسه ادمین و افزودن مدت زمان جدید به انتهای تاریخ انقضای اشتراک فعال فعلی (در صورت داشتن اشتراک فعال، اشتراک قبلی منقضی (`EXPIRED`) نمی‌شود و دوره زمانی آن تمدید می‌گردد).

- **مسیر**: `POST /api/admin/users/{userId}/subscriptions/grant`

### بدنه درخواست (Request Body)
```json
{
  "planType": "MONTHLY",
  "durationDays": 30,
  "reason": "هدیه مسابقه هفتگی / تستر برتر"
}
```

### پاسخ موفق (201 Created)
```json
{
  "status": 201,
  "message": "اشتراک دستی با موفقیت اعطا شد",
  "data": {
    "subscriptionId": "99999999-aaaa-bbbb-cccc-dddddddddddd",
    "userId": "e5b8d234-58a2-4a7b-a25e-857c1265db21",
    "planType": "MONTHLY",
    "grantSource": "MANUAL_ADMIN",
    "startedAt": "2026-09-27T14:40:00Z",
    "expiresAt": "2026-10-27T14:40:00Z",
    "status": "ACTIVE",
    "grantedByAdminId": "11111111-2222-3333-4444-555555555555"
  }
}
```
