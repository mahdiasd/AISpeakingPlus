# قرارداد وب‌سرویس: آپلود تصاویر (Admin Media API)

**نسخه**: `2.0.0`  
**آدرس پایه**: `/api/admin/media`  
**احراز هویت**: توکن Bearer (`admin-jwt`)  

---

## ۱. آپلود تصویر پس‌زمینه یا آواتار (Drag & Drop)

- **مسیر**: `POST /api/admin/media/upload`
- **نوع محتوا**: `multipart/form-data`
- **ورودی**: فایل تصویری (حداکثر حجم ۵ مگابایت، فرمت‌های PNG، JPG، WebP)

### پاسخ موفق (201 Created)
```json
{
  "status": 201,
  "message": "تصویر با موفقیت آپلود شد",
  "data": {
    "url": "/uploads/stages/67a14210-9b43-41bb-bcf1-cba1902401f8.webp",
    "filename": "67a14210-9b43-41bb-bcf1-cba1902401f8.webp"
  }
}
```
