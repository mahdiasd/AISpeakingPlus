# قرارداد وب‌سرویس: مدیریت کاربران (Admin Users API)

**نسخه**: `2.0.0`  
**آدرس پایه**: `/api/admin/users`  
**احراز هویت**: توکن Bearer (`admin-jwt`)  

---

## ۱. جستجو و لیست کاربران

جستجوی کاربران با پشتیبانی از ارقام فارسی شماره همراه (`۰۹۱۲...` یا `0912...`).

- **مسیر**: `GET /api/admin/users`
- **پارامترهای جستجو**: `page`, `limit`, `search`, `status`

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "لیست کاربران دریافت شد",
  "data": {
    "page": 1,
    "limit": 20,
    "totalCount": 1420,
    "totalPages": 71,
    "users": [
      {
        "id": "e5b8d234-58a2-4a7b-a25e-857c1265db21",
        "mobile": "09121234567",
        "nickName": "رضا",
        "score": 380,
        "avatar": "default_avatar",
        "status": "ACTIVE",
        "hasActiveSubscription": true,
        "subscriptionExpiresAt": "2026-10-25T14:30:00Z",
        "createdAt": "2026-08-10T12:00:00Z"
      }
    ]
  }
}
```

---

## ۲. تغییر وضعیت حساب کاربر (فعال‌سازی / تعلیق)

- **مسیر**: `POST /api/admin/users/{userId}/status`

### بدنه درخواست (Request Body)
```json
{
  "status": "SUSPENDED",
  "reason": "تخلف در ارسال صوت نامناسب"
}
```

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "وضعیت کاربر به تعلیق تغییر یافت",
  "data": {
    "userId": "e5b8d234-58a2-4a7b-a25e-857c1265db21",
    "status": "SUSPENDED"
  }
}
```
