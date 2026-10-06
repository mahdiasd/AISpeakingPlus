# Phase 1 Data Model: صفحه اختصاصی اشتراک‌ها و ارتقای حساب ویژه

**شاخه ویژگی**: `feature/subscription-screen`  
**تاریخ**: ۱۴۰۵/۰۷/۱۵ (۲۰۲۶-۱۰-۰۶)  
**وضعیت**: آماده برای پیاده‌سازی (Ready for Implementation)

---

## ۱. مدل‌های دامنه سمت کلاینت (Client Domain Models)

### ۱.۱ `SubscriptionPlan` (مدل پلن اشتراک)
اطلاعات بسته‌های زمانی قابل سفارش توسط کاربر را بازنمایی می‌کند.

| فیلد | نوع داده | وضعیت تهی‌پذیری | توضیحات |
|---|---|---|---|
| `id` | `String` | غیر تهی | شناسه یکتای پلن (مانند `"plan-1m"`, `"plan-3m"`, `"plan-6m"`) |
| `type` | `String` | غیر تهی | شناسه دسته‌بندی پلن (مانند `"1_MONTH"`, `"3_MONTHS"`, `"6_MONTHS"`) |
| `titleFa` | `String` | غیر تهی | عنوان رسمی به زبان فارسی (مانند «اشتراک ۳ ماهه») |
| `durationDays` | `Int` | غیر تهی | طول مدت اعتبار بر حسب روز (۳۰، ۹۰، ۱۸۰) |
| `priceTomans` | `Long` | غیر تهی | قیمت پایه بسته به تومان (بدون اعمال کد تخفیف) |
| `discountPercent` | `Int` | غیر تهی | درصد تخفیف عمومی اعمال‌شده روی پلن (مثلاً ۱۵٪ یا ۲۵٪) |
| `badge` | `String?` | اختیاری | برچسب برگزیده بصری (مانند «محبوب‌ترین» یا «بهترین ارزش») |

**ویژگی‌های محاسباتی دامنه (Domain Computed Properties)**:
- `dailyPriceTomans`: هزینه معادل روزانه به تومان (`priceTomans / durationDays`).
- `discountedPrice(extraPercent: Int)`: محاسبه قیمت پس از کسر تخفیف پلن و تخفیف کد تبلیغاتی.

---

### ۱.۲ `SubscriptionStatus` (مدل وضعیت اشتراک کاربر)
وضعیت کنونی و اعتبار زمانی حساب زبان‌آموز را مشخص می‌کند.

| فیلد | نوع داده | وضعیت تهی‌پذیری | توضیحات |
|---|---|---|---|
| `isSubscriber` | `Boolean` | غیر تهی | آیا کاربر هم‌اکنون اشتراک فعال و معتبر دارد یا خیر |
| `planType` | `String?` | اختیاری | نوع پلن فعال کاربر در صورت وجود |
| `expiresAt` | `String?` | اختیاری | زمان انقضای اشتراک به فرمت استاندارد ISO 8601 |
| `remainingDays` | `Int` | غیر تهی | تعداد روزهای تقویمی باقیمانده تا انقضا |

---

### ۱.۳ `DiscountCoupon` (مدل کد تخفیف)
اعتبار و مشخصات کد تخفیف واردشده توسط کاربر.

| فیلد | نوع داده | وضعیت تهی‌پذیری | توضیحات |
|---|---|---|---|
| `code` | `String` | غیر تهی | متن کد تخفیف واردشده |
| `discountPercent` | `Int` | غیر تهی | درصد تخفیف تعلق‌گرفته (بین ۰ تا ۱۰۰) |
| `isValid` | `Boolean` | غیر تهی | آیا کد در حال حاضر معتبر و قابل اعمال است |
| `message` | `String` | غیر تهی | پیام بازخورد به کاربر (مانند «کد تخفیف ۲۰٪ اعمال شد») |

---

## ۲. قرارداد وضعیت و تعاملات لایه نمایش (Presentation MVI Contract)

### ۲.۱ `SubscriptionState` (حالت رابط کاربری)
```kotlin
data class SubscriptionState(
    val isLoading: Boolean = false,
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "plan-3m",
    val currentStatus: SubscriptionStatus? = null,
    val promoCodeInput: String = "",
    val appliedPromo: DiscountCoupon? = null,
    val promoValidationMessage: String? = null,
    val isPromoError: Boolean = false,
    val isPurchasing: Boolean = false,
    val isPurchaseSuccess: Boolean = false,
    val errorMessage: String? = null
)
```

### ۲.۲ `SubscriptionIntent` (رویدادها و خواست‌های کاربر)
```kotlin
sealed interface SubscriptionIntent {
    data object LoadPlansAndStatus : SubscriptionIntent
    data class SelectPlan(val planId: String) : SubscriptionIntent
    data class OnPromoCodeChanged(val code: String) : SubscriptionIntent
    data object ApplyPromoCode : SubscriptionIntent
    data object PurchaseSelectedPlan : SubscriptionIntent
    data object DismissError : SubscriptionIntent
}
```

### ۲.۳ `SubscriptionEffect` (اثرات جانبی یک‌باره)
```kotlin
sealed interface SubscriptionEffect {
    data object NavigateBack : SubscriptionEffect
    data class SubscriptionActivatedSuccessfully(val remainingDays: Int) : SubscriptionEffect
    data class ShowToast(val message: String) : SubscriptionEffect
}
```

---

## ۳. مدل‌های انتقال داده شبکه (Network DTOs)

### ۳.۱ `SubscribeRequestDto`
درخواست خرید یا فعال‌سازی اشتراک ارسال‌شده به سرور.
```kotlin
@Serializable
data class SubscribeRequestDto(
    val planId: String,
    val promoCode: String? = null
)
```

### ۳.۲ `SubscribeResponseDto`
پاسخ بازگشتی از سرور پس از فعال‌سازی موفق اشتراک.
```kotlin
@Serializable
data class SubscribeResponseDto(
    val isSubscriber: Boolean,
    val planType: String,
    val expiresAt: String,
    val remainingDays: Int,
    val message: String
)
```

---

## ۴. طرح پایگاه داده سرور (Server Database Schema)

جداول درگیر در پردازش و ذخیره اشتراک‌ها:

### `SubscriptionTable` (جدول `subscriptions`)
| ستون | نوع داده SQL | کلید | توضیحات |
|---|---|---|---|
| `id` | `UUID` | PK | شناسه یکتای رکورد اشتراک |
| `user_id` | `UUID` | FK -> `users.id` | شناسه کاربر مالک اشتراک |
| `plan_type` | `VARCHAR(50)` | - | نوع بسته (مانند `"1_MONTH"`, `"3_MONTHS"`, `"6_MONTHS"`) |
| `started_at` | `TIMESTAMPTZ` | - | زمان شروع اعتبار اشتراک |
| `expires_at` | `TIMESTAMPTZ` | - | زمان پایان اعتبار اشتراک |
| `status` | `VARCHAR(20)` | - | وضعیت (`"ACTIVE"`, `"CANCELLED"`, `"EXPIRED"`) |
| `grant_source` | `VARCHAR(50)` | - | منبع ایجاد (`"USER_PURCHASE"`, `"ADMIN_GRANT"`, `"PROMO"`) |
| `grant_reason` | `TEXT` | - | توضیحات یا یادداشت تراکنش |
| `created_at` | `TIMESTAMPTZ` | - | زمان ایجاد رکورد |
