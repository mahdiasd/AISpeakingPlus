# AI Speaking Plus — Agent Development & Server Deployment Guidelines

این فایل به عنوان دستورالعمل دائمی برای تمام ایجنت‌ها در روت پروژه قرار دارد و در شروع هر پیام یا تسک باید مطالعه و رعایت شود.

---

## ۱. اصل بنیادین توسعه و همگام‌سازی (Development & Git Rules)

> [!CRITICAL]
> **تغییر مستقیم کد روی سرور اکیداً ممنوع است.**
> هیچ کدی نباید مستقیماً با ادیتور یا شل روی سرور ویرایش شود. چرخه استاندارد همواره به صورت زیر است:
>
> **پیاده‌سازی در لوکال ⬅️ تست و بیلد لوکال ⬅️ کامیت و Push به گیت‌هاب ⬅️ Pull روی سرور ⬅️ بیلد کانتینر/دیپلوی ⬅️ تست E2E نهایی**

### مراحل گردش کار Git:
1. **برنچ‌بندی**:
   - هرگز مستقیماً روی برنچ‌های محافظت‌شده (`main`, `master`, `production`) پوش نکنید.
   - از برنچ‌های مشخص با قالب کباب‌کیس استفاده کنید: `feature/...`, `fix/...`, `refactor/...`, `chore/...`
   - برنچ پیش‌فرض برای توسعه و هماهنگی سرور تست/استیجینگ: `develop`
2. **قالب استاندارد کامیت (Conventional Commits)**:
   - ساختار: `<type>(<scope>): <description>` (انواع: `feat`, `fix`, `refactor`, `chore`, `test`, `build`, ...)
3. **پوش به گیت‌هاب**:
   - پس از اعمال و تست تغییرات در محیط محلی:
     ```bash
     git add <files>
     git commit -m "<type>(<scope>): <description>"
     git push origin <branch-name>
     ```

---

## ۲. مشخصات و ساختار سرور لایو (Live Server Specification)

### اطلاعات دسترسی SSH:
- **IP سرور**: `193.93.169.198`
- **کاربر لینوکس**: `ubuntu`
- **دستور اتصال SSH**:
  ```bash
  ssh ubuntu@193.93.169.198
  ```
  *(احراز هویت از طریق کلید SSH لوکال و بدون نیاز به پسورد انجام می‌شود؛ در ابزار `run_command` نیاز به `BypassSandbox: true` دارد)*

### مسیرهای حیاتی روی سرور:
| بخش | مسیر روی سرور (`193.93.169.198`) | توضیحات |
|:---|:---|:---|
| **ریپازیتوری سورس‌کد** | `/home/ubuntu/AISpeakingPlus` | مخزن کلون‌شده پروژه و داکر کامپوز |
| **وب‌اپ کلاینت (Wasm/Canvas)** | `/var/www/aispeaking.ir/html/app/` | خروجی کامپایل‌شده وب (HTML/JS/Wasm) |
| **تنظیمات Nginx** | `/etc/nginx/sites-available/aispeaking.ir` | پروکسی معکوس برای وب، API و وب‌سوکت |
| **دامنه‌های عمومی** | `https://staging.aispeaking.ir/app/`<br>`https://staging.aispeaking.ir/api/` | آدرس لایو کلاینت و اندپوینت‌های بک‌اند |

### سرویس‌ها و کانتینرهای Docker:
پروژه بک‌اند در مسیر `/home/ubuntu/AISpeakingPlus/server` یا روت با Docker Compose مدیریت می‌شود:
- `server-app-1`: سرور Ktor (پورت داخلی `8080`، مپ‌شده به `127.0.0.1:8080`)
- `server-db-1`: دیتابیس PostgreSQL 15 (پورت داخلی `5432`)
- `server-redis-1`: کش ردیس Redis 7 (پورت داخلی `6379`)

---

## ۳. مراحل دقیق دیپلوی روی سرور (Deployment Runbook)

### الف) دیپلوی بخش سرور و بک‌اند (Ktor Backend):
هنگامی که تغییری در ماژول‌های بک‌اند (`server/`, `data/`, `domain/`, `network/`) ایجاد شده و به گیت‌هاب پوش شد:

```bash
# ۱. اتصال به سرور و رفتن به پوشه پروژه
ssh ubuntu@193.93.169.198

# ۲. پول کردن آخرین تغییرات از برنچ مربوطه (معمولاً develop)
cd /home/ubuntu/AISpeakingPlus
git fetch origin
git pull origin develop

# ۳. بیلد FatJar بک‌اند
./gradlew :server:buildFatJar -x test

# ۴. بازسازی ایمیج داکر و ری‌استارت کانتینر اپلیکیشن
docker build -t ai-speaking:latest -f server/Dockerfile .
docker restart server-app-1

# ۵. بررسی لاگ و سلامت کانتینر
docker ps --filter "name=server-app-1"
docker logs -f --tail 50 server-app-1
# سلامت اندپوینت:
curl -I http://127.0.0.1:8080/health
```

### ب) دیپلوی بخش وب کلاینت (Compose Multiplatform ترکیبی Wasm + فال‌بک JS):
هنگامی که تغییری در رابط کاربری، کلاینت یا ماژول `webApp` ایجاد شد:

```bash
# ۱. کامپایل بیلد ترکیبی پروداکشن (Wasm به همراه فال‌بک خودکار JS برای مرورگرها و سیستم‌های قدیمی):
./gradlew :webApp:composeCompatibilityBrowserDistribution

# ۲. انتقال امن فایل‌های خروجی به دایرکتوری وب سرور با rsync:
rsync -avz --delete webApp/build/dist/composeWebCompatibility/productionExecutable/ ubuntu@193.93.169.198:/var/www/aispeaking.ir/html/app/

# ۳. در صورت نیاز به ریلود کش وب‌سرور Nginx:
ssh ubuntu@193.93.169.198 "sudo systemctl reload nginx"
```

---

## ۴. استانداردهای معماری و قانون اساسی (Architecture & Constitution)

تمام تغییرات کد باید ۱۰۰٪ منطبق با قانون اساسی پروژه در [`.specify/memory/constitution.md`](file:///.specify/memory/constitution.md) باشد:
1. **Clean Architecture**: جداسازی کامل لایه‌های Domain, Data و Presentation. لایه Domain نباید به کتابخانه‌های فریم‌ورک وابسته باشد.
2. **KMP & Compose Multiplatform**: کدهای مشترک کلاینت فقط در ماژول‌های مشترک (`domain`, `data`, `sharedUI`, `feature/*`) قرار گیرند.
3. **استاندارد RTL و دکمه بازگشت**:
   - در رابط کاربری فارسی، دکمه بازگشت (`AppBackButton`) **همیشه در گوشه بالا-راست (Start anchor)** قرار دارد.
   - آیکون بازگشت (`ic_back`) **حتماً باید به سمت راست (`>`) اشاره کند**.
   - عنوان صفحات در TopAppBar باید وسط‌چین (`TextAlign.Center`) و متقارن باشد.
4. **Ktor & Coroutines**: عدم استفاده از متدهای بلاک‌کننده ترد (Blocking calls) در هندلرهای روتینگ؛ انتقال امور سنگین و دیتابیس به دیسپچرهای مناسب مانند `Dispatchers.IO`.
5. **OpenAPI 3.0.3**: هر اندپوینت Ktor باید مستندات کامل `.describe { ... }` با Request Body و Schema معتبر داشته باشد تا با Apidog سازگار بماند.

---

## ۵. پروتکل اعتبارسنجی و تست خودکار (Automated Verification)

پس از هر تغییر یا دیپلوی، عملکرد سیستم باید به صورت خودکار تست شود:
- **اطلاعات تستی لاگین بدون نیاز به پیامک واقعی (Bypass Test Account)**:
  - شماره موبایل: `09152413498`
  - کد تایید ثابت (Static OTP): `87799`
- **تست کلاینت وب با مهارت `/webapp-testing` (Playwright)**:
  - باز شدن صفحه اول و بارگذاری بدون ارور مراحل
  - سناریوی لاگین و ذخیره توکن در `localStorage['token']`
  - نمایش صحیح اطلاعات در `#profile` و لیست مراحل در `#stages`
