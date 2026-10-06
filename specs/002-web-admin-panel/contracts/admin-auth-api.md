# قرارداد وب‌سرویس: احراز هویت ادمین (Admin Auth API)

**نسخه**: `2.0.0`  
**آدرس پایه**: `/api/admin/auth`  

---

## ۱. لاگین مدیران (Admin Login)

احراز هویت ادمین با نام کاربری و رمزعبور و صدور توکن JWT با نقش `ROLE_ADMIN`.

- **مسیر**: `POST /api/admin/auth/login`
- **احراز هویت**: عمومی (Public)

### بدنه درخواست (Request Body)
```json
{
  "username": "admin@aispeaking.ir",
  "password": "SecureAdminPassword123!"
}
```

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "ورود ادمین با موفقیت انجام شد",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "admin": {
      "id": "11111111-2222-3333-4444-555555555555",
      "username": "admin@aispeaking.ir",
      "fullName": "مدیر ارشد سامانه",
      "role": "ROLE_SUPER_ADMIN"
    }
  }
}
```

---

## ۲. اطلاعات حساب جاری ادمین (Current Admin Profile)

دریافت مشخصات مدیر وارد شده بر اساس توکن فعال.

- **مسیر**: `GET /api/admin/auth/me`
- **احراز هویت**: توکن Bearer (`admin-jwt`)

### پاسخ موفق (200 OK)
```json
{
  "status": 200,
  "message": "پروفایل ادمین دریافت شد",
  "data": {
    "id": "11111111-2222-3333-4444-555555555555",
    "username": "admin@aispeaking.ir",
    "fullName": "مدیر ارشد سامانه",
    "role": "ROLE_SUPER_ADMIN"
  }
}
```
