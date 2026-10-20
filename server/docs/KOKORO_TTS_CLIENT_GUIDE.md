# Kokoro-82M Text-to-Speech (TTS) Integration Guide for Client Applications

این مستند راهنمای کامل پیاده‌سازی و استفاده از موتور تبدیل متن به صوت طبیعی **Kokoro-82M** در سمت کلاینت (Android, iOS, Flutter, Web) است.

---

## ۱. مروری بر قابلیت‌های صوتی

موتور صوتی **Kokoro-82M** یک مدل پیشرفته با کیفیت بالا و لحن بسیار طبیعی و انسانی است که خروجی صوتی با کیفیت استودیویی (WAV با نرخ نمونه‌برداری 24000Hz، فرمت 16-bit Mono) تولید می‌کند.

### صداهای موجود (۲۸ صدای طبیعی انگلیسی شامل ۱۳ صدای مردانه متنوع)

#### ۱. صداهای مردانه آمریکایی (US Male Voices - ۹ صدا)
| شناسه (`voiceId`) | کد صدا | نام صدا | جنسیت | لهجه | رتبه کیفی | توضیحات و کاراکتر |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **14** | `am_fenrir` | Fenrir | آقا | آمریکایی (US) | ⭐⭐⭐⭐⭐ (Grade C+) | بسیار باکیفیت، بم و روایی، مناسب سناریوهای داستانی و جدی |
| **17** | `am_onyx` | Onyx | آقا | آمریکایی (US) | ⭐⭐⭐⭐⭐ (محبوب‌ترین) | صدای بم، قدرتمند، رسا و کاریزماتیک با لحن سینمایی |
| **16** | `am_michael` | Michael | آقا | آمریکایی (US) | ⭐⭐⭐⭐⭐ (Grade C+) | گرم، طبیعی، دوستانه و لحن بم ملایم |
| **18** | `am_puck` | Puck | آقا | آمریکایی (US) | ⭐⭐⭐⭐⭐ (Grade C+) | رسا، پرانرژی، گیرا و پرحرارت |
| **13** | `am_eric` | Eric | آقا | آمریکایی (US) | ⭐⭐⭐⭐ | مکالمه‌ای، طبیعی، روان و روزمره |
| **15** | `am_liam` | Liam | آقا | آمریکایی (US) | ⭐⭐⭐⭐ | جوان، صمیمی، دوستانه و شفاف |
| **12** | `am_echo` | Echo | آقا | آمریکایی (US) | ⭐⭐⭐⭐ | متعادل، مدرن و رسا |
| **11** | `am_adam` | Adam | آقا | آمریکایی (US) | ⭐⭐⭐ | با اعتماد به نفس و شفاف (صدای نسخه قبل) |
| **19** | `am_santa` | Santa | آقا | آمریکایی (US) | ⭐⭐⭐ | کاراکتر بم، مسن و مهربان |

#### ۲. صداهای مردانه بریتانیایی (UK Male Voices - ۴ صدا)
| شناسه (`voiceId`) | کد صدا | نام صدا | جنسیت | لهجه | رتبه کیفی | توضیحات و کاراکتر |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **25** | `bm_fable` | Fable | آقا | بریتانیایی (UK) | ⭐⭐⭐⭐⭐ (Grade C) | صدای راوی برجسته، لهجه انگلیسی اصیل، بسیار پخته |
| **26** | `bm_george` | George | آقا | بریتانیایی (UK) | ⭐⭐⭐⭐⭐ (Grade C) | کلاسیک، باوقار و جنتلمن انگلیسی |
| **24** | `bm_daniel` | Daniel | آقا | بریتانیایی (UK) | ⭐⭐⭐⭐ | فصیح، رسمی، شمرده با لهجه RP |
| **27** | `bm_lewis` | Lewis | آقا | بریتانیایی (UK) | ⭐⭐⭐⭐ | مدرن، جوان و پرانرژی |

#### ۳. صداهای زنانه (Female Voices - ۱۵ صدا)
| شناسه (`voiceId`) | کد صدا | نام صدا | جنسیت | لهجه | رتبه کیفی | توضیحات |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **3** | `af_heart` (یا `af`) | Heart (Default) | خانم | آمریکایی (US) | ⭐⭐⭐⭐⭐ (Grade A) | فوق‌العاده طبیعی و انسانی، بهترین برای مکالمه (پیش‌فرض) |
| **2** | `af_bella` | Bella | خانم | آمریکایی (US) | ⭐⭐⭐⭐⭐ (Grade A-) | گرم و صمیمی |
| **6** | `af_nicole` | Nicole | خانم | آمریکایی (US) | ⭐⭐⭐⭐ (Grade B-) | پرانرژی و واضح |
| **9** | `af_sarah` | Sarah | خانم | آمریکایی (US) | ⭐⭐⭐⭐ (Grade C+) | شمرده و آرام |
| **10** | `af_sky` | Sky | خانم | آمریکایی (US) | ⭐⭐⭐ | نرم و ملایم |
| **0** | `af_alloy` | Alloy | خانم | آمریکایی (US) | ⭐⭐⭐ | شفاف و خنثی |
| **1** | `af_aoede` | Aoede | خانم | آمریکایی (US) | ⭐⭐⭐ | آهنگین و محاوره‌ای |
| **4** | `af_jessica` | Jessica | خانم | آمریکایی (US) | ⭐⭐⭐ | زنده و فعال |
| **5** | `af_kore` | Kore | خانم | آمریکایی (US) | ⭐⭐⭐ | متین و آرامش‌بخش |
| **7** | `af_nova` | Nova | خانم | آمریکایی (US) | ⭐⭐⭐ | درخشان و مدرن |
| **8** | `af_river` | River | خانم | آمریکایی (US) | ⭐⭐⭐ | آرام و روان |
| **21** | `bf_emma` | Emma | خانم | بریتانیایی (UK) | ⭐⭐⭐⭐ (Grade B-) | فصیح و رسمی |
| **22** | `bf_isabella` | Isabella | خانم | بریتانیایی (UK) | ⭐⭐⭐⭐ (Grade C) | دلنشین و آرام |
| **20** | `bf_alice` | Alice | خانم | بریتانیایی (UK) | ⭐⭐⭐ | شفاف و کتابی |
| **23** | `bf_lily` | Lily | خانم | بریتانیایی (UK) | ⭐⭐⭐ | شاداب و آهنگین |

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

* فیلد `voiceId` (اختیاری): شماره صدای مد نظر برای پاسخ هوش مصنوعی (پیش‌فرض ۳ صدای Heart). برای صدای مردانه می‌توانید از هر یک از شناسه‌های جدول بالا مانند ۱۴ (Fenrir)، ۱۷ (Onyx) یا ۲۵ (Fable) استفاده کنید.
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
