# راهنمای جامع ارسال پوش‌نوتیفیکیشن با فایربیس (FCM) و هدایت به صفحات (Deep Linking)

این مستند نحوه ارسال پوش‌نوتیفیکیشن از طریق **Firebase Cloud Messaging (FCM)** را برای هر دو پلتفرم **Android** و **Web** (مرورگر موبایل و دسکتاپ) به همراه تنظیم دقیق پارامترهای دیپ‌لینک (Deep Link) برای هدایت خودکار کاربر به مرحله مورد نظر، صفحه خرید اشتراک، پروفایل، لیست مراحل و ... توضیح می‌دهد.

---

## ۱. نحوه عملکرد و معماری دریافت نوتیفیکیشن

### در اندروید (Android):
- **هنگامی که اپلیکیشن باز است (Foreground):** نوتیفیکیشن توسط `AppFirebaseMessagingService` دریافت شده و به صورت یک نوتیفیکیشن محلی (Local Notification) در کانال `aispeaking_general_channel` همراه با دیتای دیپ‌لینک نمایش داده می‌شود.
- **هنگامی که اپلیکیشن بسته است یا در پس‌زمینه است (Background / Closed):** سیستم‌عامل اندروید نوتیفیکیشن را در نوار اعلان‌ها نمایش می‌دهد و پس از کلیک کاربر، اکتیویتی `AppActivity` اجرا شده و اطلاعات موجود در `data` به سیستم ناوبری `AppDeepLinkManager` تحویل داده می‌شود تا مستقیماً به صفحه یا مرحله مورد نظر برود.

### در وب (Web / Browser):
- **سرویس‌ورکر (`firebase-messaging-sw.js`):** در مرورگر کاربر ثبت می‌شود و حتی اگر تب اپلیکیشن در پس‌زمینه باشد، نوتیفیکیشن را دریافت و نمایش می‌دهد.
- **هنگام کلیک کاربر در وب:** مرورگر پنجره اپلیکیشن را باز/فوکوس کرده و هش URL را به صفحه مورد نظر (مانند `/#chat?stageId=stage-01-inflight-london` یا `/#subscription`) تغییر می‌دهد و کامپوننت `WebBrowserNavigation` صفحه مقصد را باز می‌کند.

---

## ۲. مشخصات و کلیدهای دیتای دیپ‌لینک (Payload Data Specification)

برای هدایت کاربر، باید در بخش **Custom Data** (یا فیلد `data` در API)، کلیدها و مقادیر زیر را ارسال کنید:

| نام کلید (Key) | مقادیر معتبر (Values) | توضیح عملکرد |
| :--- | :--- | :--- |
| `screen` | `none` یا ارسال نشدن فیلد | **بدون لینک (اختیاری):** فقط نمایش اعلان پیام بدون تغییر صفحه یا اکشن کلیک. |
| `stage_id` یا `stageId` | شناسه مرحله (مانند `stage-01-inflight-london` یا `stage-02-hotel-checkin`) | مستقیماً صفحه چت تمرین آن مرحله را باز می‌کند. |
| `screen` | `chat` | رفتن به صفحه تمرین مرحله (همراه با `stage_id`). |
| `screen` | `subscription` یا `sub` | باز کردن صفحه خرید و ارتقای اشتراک (VIP). |
| `screen` | `stages` یا `stages_list` | باز کردن صفحه لیست و کاتالوگ تمام مراحل. |
| `screen` | `profile` | باز کردن صفحه پروفایل کاربر و تنظیمات. |
| `screen` | `main` | رفتن به صفحه اصلی (نقشه ماجراجویی / Journey Map). |
| `screen` | `login` | هدایت به صفحه ورود / ثبت‌نام. |
| `url` | آدرس دیپ‌لینک کامل (مانند `aispeaking://chat?stageId=stage-01` یا `/#chat?stageId=stage-01`) | پشتیبانی از لینک‌های سفارشی کامل. |

---

## ۳. ارسال نوتیفیکیشن از طریق پنل کنسول فایربیس (Firebase Console)

اگر می‌خواهید به صورت دستی یا برای کمپین‌های مارکتینگ نوتیفیکیشن ارسال کنید:

1. وارد **[کنسول فایربیس](https://console.firebase.google.com/)** شوید و پروژه **aispeaking-132a3** را انتخاب کنید.
2. از منوی سمت چپ به بخش **Engage > Messaging** (یا **Cloud Messaging**) بروید.
3. روی دکمه **New campaign** و سپس **Notifications** کلیک کنید.
4. **مرحله ۱ (Notification):**
   - **Notification title:** عنوان پیام (مثلاً: `مرحله جدید باز شد! ✈️`)
   - **Notification text:** متن پیام (مثلاً: `مکالمه در پرواز لندن را همین الان تمرین کن و ۳ ستاره بگیر.`)
5. **مرحله ۲ (Target):**
   - اپلیکیشن اندروید (`ir.aispeaking`) یا وب یا یک Topic خاص را انتخاب کنید.
6. **مرحله ۳ (Scheduling):** زمان ارسال را مشخص کنید (Now یا Scheduled).
7. **مرحله ۴ (مهم‌ترین بخش - Additional options):**
   - به پایین صفحه اسکرول کنید تا به بخش **Custom data** برسید.
   - کلید و مقدار مورد نظرتان را اضافه کنید:

#### نمونه‌های تنظیم Custom Data در کنسول:

**مثال ۱: باز کردن یک مرحله خاص (مثلاً مرحله ۱):**
- Key: `stage_id` | Value: `stage-01-inflight-london`
- Key: `screen` | Value: `chat`

**مثال ۲: پیشنهاد تخفیف و باز کردن صفحه اشتراک:**
- Key: `screen` | Value: `subscription`

**مثال ۳: رفتن به لیست مراحل:**
- Key: `screen` | Value: `stages`

---

## ۴. ارسال نوتیفیکیشن از سمت سرور (Backend / FCM HTTP v1 API)

برای ارسال خودکار از سمت سرور با استفاده از REST API v1 یا SDKهای فایربیس:

### فرمت بدنه درخواست (JSON Payload):

```json
{
  "message": {
    "token": "DEVICE_OR_WEB_REGISTRATION_TOKEN",
    "notification": {
      "title": "مرحله جدید باز شد! ✈️",
      "body": "مکالمه در هتل را تمرین کن و مهارتت رو بالا ببر."
    },
    "data": {
      "stage_id": "stage-02-hotel-checkin",
      "screen": "chat",
      "click_action": "ir.aispeaking.NOTIFICATION_CLICK"
    },
    "android": {
      "priority": "high",
      "notification": {
        "channel_id": "aispeaking_general_channel",
        "default_sound": true,
        "default_vibrate_timings": true
      }
    },
    "webpush": {
      "headers": {
        "Urgency": "high"
      },
      "fcm_options": {
        "link": "/#chat?stageId=stage-02-hotel-checkin"
      }
    }
  }
}
```

---

### نمونه کد ارسال با cURL (HTTP v1):

```bash
curl -X POST https://fcm.googleapis.com/v1/projects/aispeaking-132a3/messages:send \
  -H "Authorization: Bearer YOUR_OAUTH2_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": {
      "topic": "all_users",
      "notification": {
        "title": "تخفیف ویژه اشتراک AI Speaking 🌟",
        "body": "همین امروز با ۵۰ درصد تخفیف حساب پرو خود را فعال کنید."
      },
      "data": {
        "screen": "subscription"
      },
      "android": {
        "notification": {
          "channel_id": "aispeaking_general_channel"
        }
      },
      "webpush": {
        "fcm_options": {
          "link": "/#subscription"
        }
      }
    }
  }'
```

---

### نمونه کد با پایتون (Python - Firebase Admin SDK):

```python
import firebase_admin
from firebase_admin import credentials, messaging

# مقداردهی اولیه با Service Account کلید خصوصی پروژه
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)

def send_stage_push(token: str, stage_id: str, title: str, body: str):
    message = messaging.Message(
        token=token,
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        data={
            "stage_id": stage_id,
            "screen": "chat",
        },
        android=messaging.AndroidConfig(
            priority="high",
            notification=messaging.AndroidNotification(
                channel_id="aispeaking_general_channel",
            ),
        ),
        webpush=messaging.WebpushConfig(
            fcm_options=messaging.WebpushFCMOptions(
                link=f"/#chat?stageId={stage_id}"
            )
        )
    )

    response = messaging.send(message)
    print("Successfully sent message:", response)

# نمونه فراخوانی:
# send_stage_push(
#     token="USER_TOKEN",
#     stage_id="stage-01-inflight-london",
#     title="وقت تمرین زبان است!",
#     body="مرحله ۱ منتظر شماست."
# )
```

---

### نمونه کد با نود جی‌اس (Node.js / TypeScript):

```typescript
import * as admin from 'firebase-admin';

// admin.initializeApp({ credential: admin.credential.applicationDefault() });

async function sendNotificationToUser(
  token: string,
  screen: 'chat' | 'subscription' | 'profile' | 'stages',
  stageId?: string
) {
  const dataPayload: Record<string, string> = { screen };
  if (stageId) {
    dataPayload['stage_id'] = stageId;
  }

  const webLink = stageId ? `/#chat?stageId=${stageId}` : `/#${screen}`;

  const message: admin.messaging.Message = {
    token: token,
    notification: {
      title: 'پیام جدید از AI Speaking Plus',
      body: 'برای مشاهده روی اعلان کلیک کنید.',
    },
    data: dataPayload,
    android: {
      priority: 'high',
      notification: {
        channelId: 'aispeaking_general_channel',
      },
    },
    webpush: {
      fcmOptions: {
        link: webLink,
      },
    },
  };

  const response = await admin.messaging().send(message);
  console.log('Successfully sent message:', response);
}
```

---

## ۵. نکات کلیدی برای تست در پلتفرم وب (Web Browser)

1. **پروتکل امن (HTTPS):** نوتیفیکیشن مرورگرها و سرویس‌ورکرها به دلایل امنیتی **فقط روی HTTPS** (یا روی `localhost` / `127.0.0.1`) فعال می‌شوند.
2. **دریافت توکن وب کاربر:**
   - پس از اولین تعامل کاربر با صفحه (کلیک یا لمس)، پنجره اجازه دسترسی نوتیفیکیشن مرورگر باز می‌شود.
   - توکن کاربر در لاگ کنسول با فرمت زیر چاپ می‌شود:
     `[FCM Web Token]: eXXXX...`
   - همچنین در حافظه مرورگر در کلید `fcm_web_token` ذخیره می‌شود:
     ```javascript
     console.log(localStorage.getItem('fcm_web_token'));
     ```
3. **کلید VAPID اختیاری:**
   - اگر در کنسول فایربیس در تب **Cloud Messaging > Web configuration** یک Web Push Certificate ساختید، می‌توانید کلید عمومی (Public Key) آن را در `index.html` در فیلد `window.FIREBASE_VAPID_KEY = "..."` قرار دهید. در غیر این صورت فایربیس از شناسه پیش‌فرض پروژه استفاده می‌کند.

---

## ۶. خلاصه لینک‌ها و مسیرهای درون‌برنامه‌ای (Routes Summary)

| صفحه مقصد | هش وب (Web Hash) | دیپ‌لینک اندروید (Android URI) | پارامتر دیتای نوتیف |
| :--- | :--- | :--- | :--- |
| چت مرحله | `/#chat?stageId=<ID>` | `aispeaking://chat?stageId=<ID>` | `{"screen": "chat", "stage_id": "<ID>"}` |
| خرید اشتراک | `/#subscription` | `aispeaking://subscription` | `{"screen": "subscription"}` |
| لیست مراحل | `/#stages` | `aispeaking://stages` | `{"screen": "stages"}` |
| پروفایل کاربر | `/#profile` | `aispeaking://profile` | `{"screen": "profile"}` |
| نقشه اصلی | `/#main` | `aispeaking://open` | `{"screen": "main"}` |

---

## ۷. ارسال از طریق پنل ادمین (Admin Web Panel) و ایزولاسیون نسخه جدید

در پنل وب ادمین (`admin-web`)، بخش اختصاصی **«ارسال نوتیفیکیشن»** در مسیر `/admin/notifications` پیاده‌سازی شده است.

### ویژگی‌های پنل ادمین:
1. **ایزولاسیون کانال (`aispeaking_plus_v1`):** برای جلوگیری از ارسال ناخواسته اعلان به کاربران نسخه قبلی اپلیکیشن، کلاینت‌های اندروید و وب نسخه پلاس به طور خودکار در تاپیک `aispeaking_plus_v1` عضو می‌شوند و پنل ادمین نیز به صورت پیش‌فرض پیام‌ها را روی همین تاپیک ارسال می‌کند.
2. **لینک کلیک کاملاً اختیاری:** امکان انتخاب گزینه «بدون لینک (فقط نمایش پیام - بدون کلیک)» به عنوان پیش‌فرض، تا پیام‌های صرفاً اطلاع‌رسانی و برادکست بدون تحمیل مسیر یا مرحله ارسال شوند.
3. **ویجت بررسی وضعیت نوتیفیکیشن مرورگر جاری (Live Browser Tester):** نمایش وضعیت مجوز نوتیفیکیشن در همان مرورگر (دسکتاپ یا موبایل)، دریافت و کپی توکن FCM دستگاه، و امکان ارسال **تست مستقیم به همان مرورگر** با یک کلیک جهت راستی‌آزمایی بی‌درنگ.
4. **ارسال تست به تک‌توکن:** امکان انتخاب حالت «تک‌دستگاه اختصاصی (تست)» و ارسال به توکن اختصاصی یک گوشی یا مرورگر خاص برای بررسی پیش از ارسال همگانی.
5. **انتخاب مرحله از لیست کشویی:** تمامی مراحل ثبت‌شده در سیستم به همراه نام فارسی و شناسه مرحله در فرم لیست شده و ادمین تنها با یک کلیک مرحله مورد نظر را انتخاب می‌کند.
6. **پیش‌نمایش زنده در گوشی (Live Mobile Mockup):** نمایش همزمان ظاهر نوتیفیکیشن در صفحه قفل/نوار اعلان موبایل، شامل متن، آیکون، عکس و نشانگر مقصد یا برچسب «بدون عملکرد کلیک».
7. **گزارش کامل خطا:** در صورتی که فایربیس به هر دلیلی (خطای احراز هویت، کلید نامعتبر، توکن اشتباه و...) درخواست را رد کند، متن کامل خطا در یک کادر قرمز رنگ همراه با دکمه کپی به ادمین نمایش داده می‌شود.
8. **تاریخچه نشست جاری:** نمایش پیام‌های ارسال‌شده در نشست حاضر ادمین با ذکر شناسه پیام فایربیس و زمان ارسال.

