# Kokoro-82M Text-to-Speech (TTS) Integration Guide for Client Applications

این مستند راهنمای کامل پیاده‌سازی و استفاده از موتور تبدیل متن به صوت طبیعی **Kokoro-82M** در سمت کلاینت (Android, iOS, Flutter, Web) است.

---

## ۱. مروری بر قابلیت‌های صوتی

موتور صوتی **Kokoro-82M** یک مدل پیشرفته با کیفیت بالا و لحن بسیار طبیعی و انسانی است که خروجی صوتی با کیفیت استودیویی (WAV با نرخ نمونه‌برداری 24000Hz، فرمت 16-bit Mono) تولید می‌کند.

### صداهای موجود (11 صدای متنوع انگلیسی)

| شناسه (`voiceId`) | کد صدا | نام صدا | جنسیت | لهجه | توضیحات |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **0** | `af` | Heart (Default) | خانم | آمریکایی (US) | شفاف، بسیار طبیعی و مناسب‌ترین برای مکالمه (پیش‌فرض) |
| **1** | `af_bella` | Bella | خانم | آمریکایی (US) | گرم و صمیمی |
| **2** | `af_nicole` | Nicole | خانم | آمریکایی (US) | پرانرژی و واضح |
| **3** | `af_sarah` | Sarah | خانم | آمریکایی (US) | شمرده و آرام |
| **4** | `af_sky` | Sky | خانم | آمریکایی (US) | نرم و ملایم |
| **5** | `am_adam` | Adam | آقا | آمریکایی (US) | با اعتماد به نفس و شفاف |
| **6** | `am_michael` | Michael | آقا | آمریکایی (US) | گرم و بم |
| **7** | `bf_emma` | Emma | خانم | بریتانیایی (UK) | فصیح و رسمی |
| **8** | `bf_isabella` | Isabella | خانم | بریتانیایی (UK) | دلنشین و آرام |
| **9** | `bm_george` | George | آقا | بریتانیایی (UK) | کلاسیک و پخته |
| **10** | `bm_lewis` | Lewis | آقا | بریتانیایی (UK) | مدرن و رسا |

---

## ۲. استفاده در چت هوش مصنوعی (`POST /api/v1/chat`)

زمانی که کاربر در سناریو پیام ارسال می‌کند، سرور همزمان پاسخ متنی و آدرس فایل صوتی تولید شده توسط مدل Kokoro را برمی‌گرداند.

### درخواست (`ChatRequest`)
```json
POST /api/v1/chat
Headers:
  Authorization: Bearer <USER_JWT_TOKEN>
  Content-Type: application/json

Body:
{
  "scenarioId": "65b...",
  "message": "Hi, I would like to order a coffee please.",
  "isFirstMessage": false,
  "isChallenge": false,
  "englishLevel": "Intermediate",
  "starter": "ordering_food",
  "voiceId": 0,
  "generateAudio": true
}
```

* فیلد `voiceId` (اختیاری): شماره صدای مد نظر برای پاسخ هوش مصنوعی (0 تا 10). در صورت ارسال نشدن، 0 (Heart) استفاده می‌شود.
* فیلد `generateAudio` (اختیاری): پیش‌فرض `true` است. اگر مایل به دریافت صوت نبودید می‌توانید `false` بگذارید.

### پاسخ سرور (`ChatResponse`)
```json
{
  "status": "success",
  "data": {
    "grammar": {
      "status": "correct",
      "message": null
    },
    "message": "Sure! Would you like an espresso, a latte, or a cappuccino?",
    "translatedText": "حتماً! آیا اسپرسو، لاته یا کاپوچینو میل دارید؟",
    "finishedTasksIndex": [0],
    "suggests": [
      "I'd like a cappuccino, please.",
      "What do you recommend?"
    ],
    "audioUrl": "/api/v1/tts/audio/kokoro_7d2f9b8c.wav",
    "voiceId": 0,
    "durationMs": 3420
  }
}
```

> **نکته:** کلاینت کافیست آدرس کامل صوت را بسازد:
> `https://<BASE_URL>${response.data.audioUrl}`
> و آن را بلافاصله در مدیا پلیر پخش کند.

---

## ۳. اندپوینت‌های مستقل TTS (برای تلفظ کلمات، جملات و ...)

### ۱. لیست تمام صداها (`GET /api/v1/tts/voices`)
```http
GET /api/v1/tts/voices
```
**پاسخ:**
```json
{
  "status": "success",
  "data": [
    {
      "id": 0,
      "code": "af",
      "name": "Heart (Default)",
      "gender": "FEMALE",
      "accent": "AMERICAN",
      "language": "en-US",
      "description": "Clear, natural American female voice (recommended)"
    },
    ...
  ]
}
```

### ۲. تولید صوت برای متن دلخواه (`POST /api/v1/tts/synthesize`)
مناسب برای زمانی که می‌خواهید تلفظ یک کلمه، توضیح لایتنر یا گزینه‌های Suggestion را پخش کنید.
```http
POST /api/v1/tts/synthesize
Content-Type: application/json

{
  "text": "Eloquent means fluent or persuasive in speaking or writing.",
  "voiceId": 0,
  "speed": 1.0
}
```
**پاسخ:**
```json
{
  "status": "success",
  "data": {
    "audioUrl": "/api/v1/tts/audio/kokoro_e1a4f80c.wav",
    "durationMs": 4120,
    "sampleRate": 24000,
    "voiceId": 0,
    "voiceName": "Heart (Default)"
  }
}
```

### ۳. استریم مستقیم فایل صوتی (`GET /api/v1/tts/speak`)
می‌توانید مستقیماً این URL را به MediaPlayer بدهید تا بدون نیاز به مرحله اضافی صوت پخش شود:
```http
GET /api/v1/tts/speak?text=Hello+world&voiceId=0&speed=1.0
```
خروجی این اندپوینت مستقیماً `Content-Type: audio/wav` با داده‌های باینری صوت است.

---

## ۴. نمونه کدهای پیاده‌سازی کلاینت

### ۱. نمونه کد در اندروید (Kotlin / Media3 ExoPlayer)

```kotlin
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer

class AiAudioPlayer(private val context: Context) {
    private val player = ExoPlayer.Builder(context).build()

    fun playAiResponse(baseUrl: String, audioUrl: String?) {
        if (audioUrl.isNullOrEmpty()) return
        
        val fullUrl = if (audioUrl.startsWith("http")) audioUrl else "$baseUrl$audioUrl"
        
        val mediaItem = MediaItem.fromUri(fullUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun release() {
        player.release()
    }
}
```

### ۲. نمونه کد در فلاتر (Flutter / audioplayers)

```dart
import 'package:audioplayers/audioplayers.dart';

class TtsPlayer {
  final AudioPlayer _audioPlayer = AudioPlayer();

  Future<void> playAiVoice(String baseUrl, String? audioPath) async {
    if (audioPath == null || audioPath.isEmpty) return;

    final String fullUrl = audioPath.startsWith('http') 
        ? audioPath 
        : '$baseUrl$audioPath';

    await _audioPlayer.stop();
    await _audioPlayer.play(UrlSource(fullUrl));
  }

  void dispose() {
    _audioPlayer.dispose();
  }
}
```

### ۳. نمونه کد در iOS (Swift / AVPlayer)

```swift
import AVFoundation

class AiAudioPlayer {
    private var player: AVPlayer?

    func play(baseUrl: String, audioUrl: String?) {
        guard let audioUrl = audioUrl, !audioUrl.isEmpty else { return }
        
        let urlString = audioUrl.hasPrefix("http") ? audioUrl : "\(baseUrl)\(audioUrl)"
        guard let url = URL(string: urlString) else { return }
        
        player = AVPlayer(url: url)
        player?.play()
    }
}
```

---

## ۵. نکات کلیدی برای عملکرد بهینه
1. **کش خودکار سمت سرور (Zero Latency for Cached Audio):** سرور به طور خودکار صوت جملاتی که قبلاً تولید شده‌اند را کش می‌کند. بنابراین متن‌های تکراری، خوش‌آمدگویی‌ها و کلمات پرکاربرد بلافاصله با تاخیر 0 میلی‌ثانیه تحویل داده می‌شوند.
2. **پشتیبانی از Range و Caching در HTTP:** اندپوینت‌های صوتی هدر `Cache-Control: public, max-age=31536000` دارند و کلاینت می‌تواند آن‌ها را به راحتی کش محلی کند.
3. **تغییر سرعت (Speed):** پارامتر `speed` بین `0.5` (آهسته) تا `2.0` (سریع) قابل تنظیم است. مقدار پیش‌فرض `1.0` است.
