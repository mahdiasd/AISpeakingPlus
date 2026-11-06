# AI Speaking Plus — React Cupertino Web Client

این ماژول، کلاینت پرسرعت و مدرن وب برای اپلیکیشن **AI Speaking Plus** است که با هدف ارائه بهترین عملکرد در مرورگرهای موبایل (iOS Safari و Android Chrome) و بازآفرینی زبان طراحی **Liquid Glass و Cupertino اپل** بر پایه استک مدرن وب توسعه داده شده است.

---

## ۱. مشخصات فنی و استک تکنولوژی

- **فریم‌ورک پایه**: React 18.3.1 (LTS استیبل با بالاترین هماهنگی با اکوسیستم)
- **باندلر و ابزار بیلد**: Vite 6.2.0 + TypeScript 5.7
- **موتور انیمیشن و فیزیک اپل**: `motion` (Framer Motion v12/14) برای فیزیک فنری WWDC (`spring` با `stiffness: 450`, `damping: 25`)
- **طراحی و استایل‌دهی**: Tailwind CSS v3.4 + کامپوننت‌های بهینه‌شده Apple Cupertino
- **پشتیبانی پکیج‌های اختصاصی اپل**: هماهنگ با `@react-cupertino-ui/*`
- **تایپوگرافی فارسی**: `@fontsource-variable/vazirmatn` (متغیر و سلف‌هاست، بدون نیاز به CDN خارجی)
- **آیکون‌ها**: `lucide-react`

---

## ۲. کامپوننت‌های اختصاصی Cupertino و Liquid Glass

کامپوننت‌های آماده در مسیر `src/components/cupertino/`:
- `CupertinoNavBar`: هدر محو و شیشه‌ای منطبق با استانداردهای راست‌به‌چپ (دکمه بازگشت با آیکون `>` در گوشه بالا-راست و عنوان کاملاً متقارن در مرکز).
- `CupertinoCard`: کارت شیشه‌ای با افکت Liquid Glass، هایلایت صیقلی لبه بالایی (Specular Highlight) و سایه شناور متریال.
- `CupertinoButton`: دکمه‌های آیفون در حالت‌های `glass`، `filled`، `tinted` و `plain` با انیمیشن فشرده‌شدن و ریباند فنری (`active:scale-[0.97]`).
- `CupertinoSegmentedControl`: انتخاب‌گر تب‌های کشویی و پیوسته iOS با افکت شناور `motion`.
- `CupertinoSwitch`: سوئیچ دوحالته اپل با حرکت فنری و رنگ سبز سیستم iOS.

---

## ۳. دستورات اجرایی

```bash
# رفتن به پوشه وب کلاینت
cd client-web

# نصب وابستگی‌ها
npm install

# اجرای سرور توسعه محلی (پورت 8083 با پراکسی به Ktor 8080)
npm run dev

# ساخت خروجی پروداکشن (تولید خروجی در client-web/dist)
npm run build

# پیش‌نمایش بیلد پروداکشن
npm run preview
```

---

## ۴. ارتباط با بک‌اند (Ktor)

کلاینت ارتباطی API در مسیر `src/api/client.ts` قرار دارد و تمام درخواست‌ها را به صورت خودکار به `/api` ارسال می‌کند. در محیط توسعه محلی، Vite درخواست‌ها را به `http://127.0.0.1:8080` هدایت (Proxy) می‌کند.
توکن احراز هویت در `localStorage['token']` ذخیره می‌شود.

- **شماره موبایل تستی بای‌پس**: `09152413498`
- **کد تایید ثابت (OTP)**: `87799`
