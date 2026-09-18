# Idea Intake: Story-Based Gamified Speaking Journey with Tiered Access & Evaluation Rubric

- **Slug**: story-based-speaking-journey
- **Created**: 2026-09-17
- **Source**: pasted text
- **Type**: new-capability

## Idea (as captured)

> اپلیکیشن برای کاربرانی هست که زبان انگلیسی یاد میگیرن و نیازمند تمرین برای مکالمه هستند. 
> در همین راستا، با استفاده از هوش منصوعی میتونیم در سناریوهای مختلف، کاربر رو قرار بدیم تا با یک نقش در اون داستان مکالمه داشته باشه. 
> برای جذاب شدن و نرخ بازگشت بیشتر، هر سناریو و داستان رو در قالب یک مرحله به کاربر نشون میدیم که با رد از شدن یک مرحله، میتونه مرحله ی بعد رو شروع کنه. 
> مراحل تا حدودی به هم وابسته و داستانی هستند تا برای کاربر جذاب باشه تا مرحله ی بعدی رو هم بره. 
> کاربر در هر مرحله باید یک هدف رو انجام بده. مثلا یک شخصیت قهرمان برای اپلیکیشن داریم، وقتی کاربر اولین بار وارد اپلیکیشن میشه، یک ویدیو بهش نشون میدیم که اون شخصیت از تهران به لندن مهاجرت کرده و باید یه سری مراحل رو بره. برای اینکه بدونی ایده چیه یک مثال میزنم ولی ضرورتی بر وجود این داستان نیست:
> کاربر باید کلید خونه ای که قبلا اجاره کرده رو از سوپر محل بگیره چون صاحبخونه رفته مسافرت و داده به اون. بعد ما در پراپمت هایی که به هوش مصنوعی میدیم، میگیم باید یه سری مکالمه باهاش داشته باشی. مثلا اینکه اول باید خرید کنه کاربر یا ... و بعد کلید رو بهش بدی. وقتی کاربر مکالمه کرد و چالش ها رو رد کرد امتیاز میگیره. 
> اگر این وسط نیازمند راهنمایی بود یا نمیدونست باید چی بگه قابلیت راهنما هم داریم. ولی خب از ستاره هایی که میتونه برای اون مرحله بگیره کم میکنه 
> این جدول امتیاز دهی هست :
> ۵. فرمول نمرهدهی و ستارهها (Evaluation & Stars Rubric)
> معیار عبور از مرحله و کیفیت عملکرد بر اساس تعداد خطاهای گرامری/املایی و تعداد دفعات استفاده از راهنما سنجیده میشود:
> 
> رتبه	شرط کسب ستاره	وضعیت بازگشایی
> ⭐⭐⭐ (۳ ستاره)	۰ خطای گرامری و ۰ بار استفاده از راهنما (Hint)	باز شدن مرحله بعد + حداکثر پاداش
> ⭐⭐ (۲ ستاره)	مجموعاً ۱ خطا یا ۱ بار استفاده از راهنما	باز شدن مرحله بعد
> ⭐ (۱ ستاره)	مجموعاً ۲ مورد خطا و استفاده از راهنما	باز شدن مرحله بعد (حداقل قبولی)
> ⭕ (۰ ستاره)	۳ مورد یا بیشتر خطا و راهنما یا عدم تحقق ماموریت	عدم بازگشایی مرحله بعد (تکرار الزامی)
> قانون ارتقا: کاربر برای پیشروی در نقشه و باز کردن مرحله بعد، باید حداقل ۱ ستاره کسب کند.
> 
> این رو هم ببین:
> ۲. مدل دسترسی، احراز هویت و گیتینگ (Access Gating & Auth)
> دسترسی کاربران به مراحل به صورت پلکانی و مرحلهبهمرحله کنترل میشود:
> 
> مرحله	سطح دسترسی	مکانیزم احراز هویت	توضیحات
> مرحله ۱	کاملاً رایگان (Guest Mode)	بدون نیاز به ثبتنام یا شماره موبایل	کاربر بلافاصله وارد مرحله ۱ میشود. امتیاز و ستارههای دریافتی موقتاً در دیتابیس محلی دستگاه (Local Storage) ذخیره میگردد.
> مرحله ۲	نیازمند ثبتنام (Registered Free)	ورود با شماره موبایل + پیامک OTP	برای ورود به مرحله ۲، پنجره ثبتنام نمایش داده میشود. پس از تایید شماره موبایل، اطلاعات مرحله ۱ به اکانت کاربر در سرور متصل (Sync) میشود.
> مرحله ۳ به بعد	نیازمند اشتراک ویژه (Premium / Paywall)	خرید اشتراک حساب کاربری	مراحل ۳ به بعد قفل هستند و کاربر برای ادامه ماجراجویی نیازمند تهیه اشتراک است.
> 
> دیزاین مدنظرمم شبیه بازیهای گیم هست و ۲بعدی (استفاده از تصاویر جذاب دو بعدی برای سناریوها، بک‌گراندها و دکمه‌های شبیه بازی). 
> هر مرحله یک بکگراند داره که کاربر با رسیدن به هر مرحله بک گراند کامل رو میبینه و شخصیت داستان یا سناریو و دکمه هایی که در این صفحه داریم که خیلی برای دیزاینش فکر نکردم. وقتی فیچرها رو مشخص کنیم دیزاینر درستش میکنه

## Restated

A gamified, story-driven English speaking practice journey where users progress through sequentially unlocked stages by completing interactive AI roleplay missions. Progress is governed by a strict star-based evaluation rubric (deducting stars for grammar errors and hints used) and a three-tiered access gate (Stage 1 guest/local, Stage 2 mobile OTP registration sync, Stage 3+ premium paywall), accompanied by stylized 2D game-style illustrated backgrounds and UI controls per stage.

## Origin & Context

- **Raised by**: Project Lead / Product Owner (User)
- **Trigger**: Enhancing user engagement, retention, and monetization for English speaking learners through continuous narrative progression, clear feedback rubrics, and tiered user onboarding.

## First-Glance Unknowns

- [NEEDS CLARIFICATION: What are the exact criteria and evaluation mechanisms used by the AI to detect when a mission goal has been successfully accomplished during roleplay?]
- [NEEDS CLARIFICATION: How are hints surfaced to the user during conversations (e.g., suggested phrases, translation, vocabulary hints), and does any hint request automatically count toward star deductions?]
- [NEEDS CLARIFICATION: What are the exact image formats and resolution scales required for 2D game-style illustrated backgrounds and custom UI buttons to ensure sharp rendering across mobile and desktop?]
- [NEEDS CLARIFICATION: In Guest Mode, what is the exact local storage persistence mechanism and conflict resolution policy when syncing Stage 1 local progress to the backend upon Stage 2 OTP login?]
- [NEEDS CLARIFICATION: How is the introductory narrative video delivered and cached on client devices (embedded asset, CDN stream, or downloadable bundle)?]
