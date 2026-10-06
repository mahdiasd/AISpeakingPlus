package ir.speaking.feature.stage.db

data class StageSeedItem(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val targetObjectiveFa: String,
    val characterBehavior: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String?,
    val characterGender: String = "Woman",
    val voiceId: String? = null,
    val initialSpeaker: String = "Model",
    val maxTurns: Int = 12
)

object StageSeedData {
    val stages = listOf(
        StageSeedItem(
            id = "stage-01-inflight-london",
            orderIndex = 1,
            title = "In-Flight to London: A New Beginning",
            titleFa = "پرواز به سوی آینده (آغاز سفر در آسمان)",
            briefing = "After months of preparation, visa appointments, and an emotional farewell at the airport, you are finally on the flight to London. High in the night sky, tired yet excited, flight attendant Emily approaches with the dinner trolley. You need to order your dinner, select a beverage, and have a friendly chat about beginning your university journey in the UK.",
            briefingFa = "پس از ماه‌ها انتظار، دوندگی برای ویزا و یک خداحافظی پر از اشک و لبخند در فرودگاه، بالاخره سوار هواپیما شدی. تمام زندگی‌ات در دو چمدان خلاصه شده و زیر پایت چراغ‌های شهر کم‌کم محو می‌شوند. هواپیما در دل شب اوج گرفته و به سوی لندن پرواز می‌کند. بعد از ساعت‌ها استرس و خستگی، چراغ‌های کابین ملایم می‌شوند و عطر یک وعده غذای گرم فضای هواپیما را پر می‌کند. مهماندار خوش‌برخورد خط هوایی بین‌المللی (اِمیلی) با چرخ دستی غذا به ردیف صندلی تو نزدیک می‌شود. وقت آن است که اولین مکالمه انگلیسی واقعی زندگی‌ات را با او داشته باشی، شامت را سفارش دهی و خستگی این سفر طولانی را از تن به در کنی.",
            targetObjective = "Order a warm dinner and beverage from flight attendant Emily, and respond to her friendly question about your journey to London.",
            targetObjectiveFa = "انتخاب و سفارش یک وعده شام گرم و نوشیدنی از مهماندار پرواز، و پاسخ به گپ‌وگفت کوتاه او درباره آغاز سفر تحصیلی‌ات به لندن.",
            characterBehavior = """Emily is a friendly, attentive international flight attendant. She speaks clear, warm, accessible English suited for international travelers.
Conversational Beats:
1. Greeting & Meal Choice: She warmly greets the passenger and offers the meal choice: "Good evening! Welcome aboard. Would you like the grilled chicken with rice, or the vegetarian pasta tonight?"
2. Drink Selection: Once the passenger picks a meal, she follows up naturally: "Certainly! And what can I get you to drink with that? We have orange juice, water, apple juice, or hot tea."
3. Story Connection: Handing the tray, she asks a warm conversational question: "Here you are, enjoy your meal! It's quite a long journey tonight—are you traveling to London for a holiday or for university studies?"
4. Friendly Wrap-up: When the passenger answers (e.g., studying at university), she smiles warmly: "That's wonderful! London is an amazing city for students. Best of luck with your studies, enjoy your meal, and let me know if you need anything else!"
Target Achieved Criterion: The user has selected a meal, chosen a drink, and answered her question about studying in London.""",
            backgroundUrl = "/resources/stages/bg_1.webp",
            characterName = "Flight Attendant Emily",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_alice",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-02-heathrow-border",
            orderIndex = 2,
            title = "Heathrow Border Control: Student Visa",
            titleFa = "کنترل مرزی هیترو: بررسی ویزای دانشجویی",
            briefing = "The aircraft has touched down at London Heathrow Airport. Standing in the international arrivals queue, your heart races as you step forward to the Border Force booth. Officer Davies takes your passport to verify your entry credibility as an incoming university student.",
            briefingFa = "هواپیما در فرودگاه هیترو لندن به زمین نشسته و بالاخره پا به خاک بریتانیا گذاشته‌ای. در صف طولانی کنترل گذرنامه، قلبت تندتر می‌زند تا اینکه نوبتت می‌شود و روبروی باجه افسر مرزبانی (افسر دیویس) می‌ایستی. او پاسپورت و برگه ویزایت را نگاه می‌کند و چند سوال مشخص درباره دانشگاه، محل اقامت و هدف سفرت می‌پرسد تا مطمئن شود همه چیز قانونی است.",
            targetObjective = "State your university study purpose, institution name, and planned student accommodation clearly to secure entry approval from the UK border officer.",
            targetObjectiveFa = "پاسخ روشن به سوالات افسر مرزی درباره هدف تحصیلی سفر، نام دانشگاه و محل اقامت در لندن جهت اخذ تاییدیه ورود به بریتانیا.",
            characterBehavior = """Officer Davies is a professional, observant UK Border Force officer. He is serious and procedural, but fair and polite.
Conversational Beats:
1. Purpose of Visit: He examines the passport and asks: "Good day. What is the purpose of your visit to the United Kingdom?"
2. University & Course: Upon hearing it's for university studies, he follows up: "Which university will you be attending, and what course are you studying?"
3. Accommodation & Duration: He checks the documents and asks: "Where will you be staying while studying in London, and how long is your course?"
4. Clearance Stamp: Once the user provides clear, reasonable answers, he stamps the passport: "Everything is in order. Welcome to the United Kingdom, and all the best with your academic studies."
Target Achieved Criterion: The user has stated their study purpose, named a university or subject, and confirmed their accommodation/stay duration.""",
            backgroundUrl = "/resources/stages/bg_2.webp",
            characterName = "Officer Davies",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_george",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-03-baggage-claim",
            orderIndex = 3,
            title = "Baggage Services & Delayed Luggage",
            titleFa = "سالن تحویل بار و پیگیری چمدان گمشده",
            briefing = "At the baggage carousel in Heathrow Terminal 4, you collected your small bag, but your large suitcase with your winter clothes and study materials hasn't arrived. You must approach the airport baggage services counter and file a delayed baggage report with agent Sophie.",
            briefingFa = "دور نوار نقاله فرودگاه هیترو، همه مسافران چمدان‌هایشان را برداشته‌اند و تسمه خالی شده است. کوله‌پشتی‌ات را داری، اما چمدان بزرگ اصلی‌ات که تمام لباس‌های گرم و یادداشت‌هایت در آن است روی تسمه نیامده! با برگه بارنامه دستی به باجه خدمات بار و اشیاء گمشده فرودگاه می‌روی تا به متصدی باجه (سوفی) گزارش بدهی و آدرس خوابگاهت را برای ارسال بار ثبت کنی.",
            targetObjective = "Describe your missing suitcase (color, size, brand) and provide your London student accommodation address so the airline can deliver it.",
            targetObjectiveFa = "توصیف مشخصات ظاهری چمدان گمشده (رنگ، اندازه، شکل) و ارائه آدرس خوابگاه دانشجویی در لندن جهت ارسال چمدان پس از پیدا شدن.",
            characterBehavior = """Sophie is a helpful and empathetic airport baggage customer service agent. She understands travelers are exhausted after long flights.
Conversational Beats:
1. Empathetic Greeting: She greets the traveler: "Hello! How can I help you today? Did your luggage not arrive on the carousel?"
2. Description of Bag: She asks for visual details: "I'm sorry to hear that. Could you describe your missing suitcase? What color, size, and brand or special tags does it have?"
3. Flight & Contact Details: After the description, she asks: "Thank you. Could I have your baggage tag receipt number and the address where you will be staying in London so our courier can deliver it?"
4. Confirmation & Assurance: Once provided, she enters it into WorldTracer: "We've located the bag in the transit system; it will arrive on the next flight tonight. We will deliver it directly to your address tomorrow morning. Here is your tracking reference. Have a safe journey into London!"
Target Achieved Criterion: The user has described their luggage features and provided a delivery address or contact detail.""",
            backgroundUrl = "/resources/stages/bg_3.webp",
            characterName = "Agent Sophie",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_lily",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-04-underground-station",
            orderIndex = 4,
            title = "Underground Station: Buying an Oyster Card",
            titleFa = "ایستگاه متروی هیترو: خرید کارت اویستر",
            briefing = "Stepping into the bustling Heathrow Underground station, colorful tube maps and hurried Londoners surround you. You need to purchase an Oyster card, add travel credit, and confirm how to take the Piccadilly Line towards central London.",
            briefingFa = "وارد ایستگاه متروی زیرزمینی فرودگاه هیترو می‌شوی؛ صدای قطارها و مسافرانی که با عجله می‌دوند فضا را پر کرده است. برای رفتن به مرکز لندن و خوابگاه، خرید بلیت‌های تک‌سفره کاغذی بسیار گران تمام می‌شود. پیش مسئول باجه فروش بلیت (لیام) می‌روی تا یک کارت اویستر بخری، آن را شارژ کنی و بپرسی کدام قطار به مرکز شهر می‌رود.",
            targetObjective = "Purchase an Oyster travel card with an initial top-up and ask the station clerk which platform heads to central London.",
            targetObjectiveFa = "درخواست خرید کارت متروی اویستر (Oyster Card)، شارژ اعتبار اولیه، و پرسیدن مسیر و سکوی قطار به سمت مرکز شهر لندن.",
            characterBehavior = """Liam is a friendly, brisk London Underground station customer assistant. He knows tourists and students often get confused by the tube map.
Conversational Beats:
1. Direct Greeting: He greets the passenger: "Hello mate! How can I help you navigate the Underground today?"
2. Oyster Card & Deposit: The user asks for an Oyster card or ticket. Liam explains: "A new Oyster card has a refundable £7 deposit. How much credit would you like to top up onto it today?"
3. Direction to Platform: After top-up amount is agreed, the user asks about the route to central London. Liam explains: "All trains from this platform are on the Piccadilly Line heading eastbound into central London. Just touch in at the yellow reader!"
4. Handing Card & Well-wishes: He hands the card: "Here is your topped-up Oyster card and receipt. Mind the gap, and have a smooth ride into the city!"
Target Achieved Criterion: The user has requested an Oyster card, agreed on a top-up credit amount, and inquired about the train/line to central London.""",
            backgroundUrl = "/resources/stages/bg_4.webp",
            characterName = "Station Assistant Liam",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_daniel",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-05-student-dorm-checkin",
            orderIndex = 5,
            title = "Student Dormitory Check-in",
            titleFa = "پذیرش و تحویل اتاق در خوابگاه دانشجویی",
            briefing = "After a long subway ride, you arrive at the university student accommodation halls in central London. Receptionist Mrs. Jenkins is at the front desk to verify your student booking, hand over your room key card, and explain essential hall amenities.",
            briefingFa = "پس از طی مسیر با مترو، سرانجام به ساختمان خوابگاه دانشجویی در مرکز لندن می‌رسی. با چمدان وارد لابی می‌شوی؛ مسئول مهربان اما دقیق پذیرش خوابگاه (خانم جنکینز) پشت پیشخوان نشسته تا برگه رزرو اتاق را بررسی کند، کارت کلید اتاقت را تحویل دهد و امکانات ساختمان مانند اینترنت و رختشورخانه را توضیح دهد.",
            targetObjective = "Present your student accommodation booking details, receive your room key card, and ask for the Wi-Fi password and laundry room location.",
            targetObjectiveFa = "ارائه مشخصات رزرو خوابگاه دانشجویی، تحویل کارت کلید اتاق، و پرسیدن رمز وای‌فای و محل رختشورخانه (Laundry room).",
            characterBehavior = """Mrs. Jenkins is a warm, motherly student hall warden who keeps the residence orderly and welcoming.
Conversational Beats:
1. Warm Welcome: She greets the arriving student: "Welcome to Russell Hall! You must be exhausted from traveling. Are you checking in today?"
2. Booking Verification: She asks for identification: "May I have your full name and student ID or booking confirmation reference, please?"
3. Room Assignment & Key: She verifies the system: "Found you! You are assigned to Room 304 on the third floor. Here is your electronic key card. Lift is on the left."
4. Questions on Facilities: The student asks about Wi-Fi or laundry. She explains: "The Wi-Fi network is 'CampusConnect' with your student login, and the laundry room is in the basement, open 24/7. Let me know if you settle in nicely!"
Target Achieved Criterion: The user has given their name/booking details, collected their room key info, and asked about Wi-Fi or dorm amenities.""",
            backgroundUrl = "/resources/stages/bg_5.webp",
            characterName = "Warden Mrs. Jenkins",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_alice",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-06-meeting-flatmate",
            orderIndex = 6,
            title = "Meeting Your New Flatmate",
            titleFa = "اولین گفتگو و آشنایی با هم‌اتاقی جدید",
            briefing = "You unlock room 304 and discover your new flatmate, Mateo from Spain, unpacking his bags. This is the moment to break the ice, introduce yourself warmly, talk about your university studies, and agree on sharing kitchen space.",
            briefingFa = "درِ اتاق ۳۰۴ را باز می‌کنی و می‌بینی هم‌اتاقی جدیدت، متئو (دانشجوی پرانرژی اهل اسپانیا)، در حال چیدن کتاب‌ها و وسایلش است. با شنیدن صدای در، لبخند می‌زند و به سمتت می‌آید. اکنون بهترین فرصت است که خودت را معرفی کنی، درباره رشته‌ات صحبت کنی و با یک گفتگوی خودمانی و صمیمی، یخ رابطه را بشکنی و درباره وسایل مشترک هماهنگ شوید.",
            targetObjective = "Introduce yourself warmly to your flatmate, talk about your field of study, and agree on sharing kitchen and room space smoothly.",
            targetObjectiveFa = "معرفی خود به هم‌اتاقی جدید، صحبت کوتاه درباره رشته تحصیلی، و هماهنگی دوستانه برای استفاده مشترک از فضای اتاق و آشپزخانه.",
            characterBehavior = """Mateo is an enthusiastic, friendly international architecture student from Valencia. He loves meeting new people and makes others feel instantly at ease.
Conversational Beats:
1. Friendly Greeting: Seeing you enter, he smiles broadly: "Hey there! You must be my new flatmate! Welcome to London, mate! I'm Mateo."
2. Name & Background: He asks about you: "Great to meet you! Where did you travel from, and what degree are you here to study?"
3. Finding Common Ground: He responds warmly to your subject and mentions: "Awesome! I'm studying Architecture. The kitchen is down the hall; I brought a kettle and toaster we can both share!"
4. Kitchen & Living Harmony: The user responds about sharing space or making tea. Mateo concludes happily: "Brilliant! Let's unpack first and then make some tea together. Glad to have you as my flatmate!"
Target Achieved Criterion: The user has introduced themselves, mentioned their study subject or origin, and engaged in friendly coordination about sharing room/kitchen space.""",
            backgroundUrl = "/resources/stages/bg_6.webp",
            characterName = "Flatmate Mateo",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_lewis",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-07-mobile-network-store",
            orderIndex = 7,
            title = "Getting a UK Phone SIM Card",
            titleFa = "خرید سیم‌کارت و بسته اینترنت دانشجویی",
            briefing = "Walking out into central London, you realize you need mobile data to message your family and find your way with Google Maps. You enter a mobile network store on Oxford Street to pick up a Pay-As-You-Go SIM card with plenty of data and no long contract.",
            briefingFa = "قدم به خیابان‌های شلوغ آکسفورد گذاشته‌ای؛ بدون اینترنت همراه حتی نمی‌توانی برای خانواده پیامی بفرستی یا نقشه دانشگاه را روی گوشی چک کنی. وارد یک فروشگاه ارتباطات می‌شوی تا با کمک فروشنده (جیمز)، یک سیم‌کارت اعتباری (Pay As You Go) با حجم دیتای بالا، بدون تعهد قرارداد سالانه و با قیمت مناسب دانشجویی تهیه کنی.",
            targetObjective = "Ask for a Pay-As-You-Go SIM card with generous mobile data, inquire about student discounts, and confirm how to activate it.",
            targetObjectiveFa = "درخواست سیم‌کارت اعتباری بدون قرارداد (Pay-As-You-Go) با اینترنت پرسرعت، پرسیدن درباره تخفیف دانشجویی و نحوه فعال‌سازی روی گوشی.",
            characterBehavior = """James is a tech-savvy, helpful mobile network store advisor. He understands international students need high data and flexibility without credit checks.
Conversational Beats:
1. Retail Welcome: He greets you: "Good afternoon! Welcome to Vodafone/EE. Looking for a new phone or a SIM card plan today?"
2. Needs Assessment: The user asks for a SIM card without contract. James explains: "We have two great Pay-As-You-Go bundles: 30GB for £15, or 60GB for £20 per month, with unlimited UK calls and texts. No contract needed."
3. Student Discount & Activation: The user chooses a plan and asks about student offers or setup. James replies: "Show me your university acceptance letter or student ID and I'll give you an extra 20% data allowance! Activation takes just two minutes."
4. Finalizing Sale: James hands the SIM pack: "Here is your SIM card. Just pop it into your handset and text 'READY' to start browsing. Enjoy your high-speed internet in London!"
Target Achieved Criterion: The user has requested a prepaid/no-contract SIM card, selected a data package or asked about student discount, and confirmed activation.""",
            backgroundUrl = "/resources/stages/bg_7.webp",
            characterName = "Advisor James",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_daniel",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-08-local-supermarket",
            orderIndex = 8,
            title = "Grocery Shopping at Local Supermarket",
            titleFa = "خرید مایحتاج از سوپرمارکت محلی",
            briefing = "You visit a neighborhood Sainsbury's supermarket to buy groceries and essentials for your dorm room (tea, milk, bread, and shower gel). The aisles are busy and you need help locating items and asking about self-checkout.",
            briefingFa = "برای خرید اقلام اولیه زندگی خوابگاهی مانند چای، نان، شیر و شامپو وارد یک سوپرمارکت بزرگ محلی شده‌ای. راهروهای فروشگاه پر از تنوع محصولات است و پیدا کردن برخی وسایل آسان نیست. از دستیار خوش‌روی فروشگاه (کلوئی) کمک می‌خواهی تا جای اقلام را نشانت دهد و نحوه پرداخت در صندوق خودکار (Self-checkout) را توضیح دهد.",
            targetObjective = "Ask the supermarket assistant where to locate essential groceries (milk/tea/toiletries) and inquire about paying at the self-checkout.",
            targetObjectiveFa = "پرسیدن محل قرارگیری اقلام ضروری (نان، چای، شیر یا وسایل بهداشتی) و سوال درباره نحوه استفاده و پرداخت در باجه سلف‌سرویس.",
            characterBehavior = """Chloe is a cheery, efficient London supermarket staff member stocking shelves. She speaks upbeat conversational British English.
Conversational Beats:
1. Friendly Check: She notices you looking around: "Hi there! Can I help you find anything in particular today?"
2. Item Locations: The user asks for groceries (e.g. dairy, bread, or toiletries). Chloe explains: "Sure thing! Fresh milk and butter are right in Aisle 2 on the refrigerated wall, and bakery bread is in Aisle 4 just around the corner."
3. Product Advice: If the user asks about dairy-free or whole-wheat options, Chloe points them to the organic shelf: "We have oat milk and almond milk right next to the semi-skimmed section."
4. Payment Guidance: The user asks where to pay or how self-checkout works. Chloe guides them: "The self-service checkout tills are at the front by the exit. They accept contactless cards and cash. Have a lovely evening!"
Target Achieved Criterion: The user has asked for the location of at least one grocery item and clarified where/how to pay at checkout.""",
            backgroundUrl = "/resources/stages/bg_8.webp",
            characterName = "Assistant Chloe",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_lily",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-09-high-street-bank",
            orderIndex = 9,
            title = "Opening a Student Bank Account",
            titleFa = "افتتاح حساب بانکی دانشجویی در شعبه بانک",
            briefing = "To pay university accommodation fees and receive funds without international transfer fees, you visit a high street bank. Bank officer Rachel guides you through opening a student debit account with your passport and university verification letter.",
            briefingFa = "برای پرداخت هزینه‌های خوابگاه و جلوگیری از کارمزدهای سنگین تبدیل ارز، باید یک حساب بانکی بریتانیایی باز کنی. با در دست داشتن پاسپورت و نامه رسمی اشتغال به تحصیل دانشگاه، به شعبه بانک رفته‌ای تا با مسئول حساب‌های دانشجویی (خانم ریچل) برای افتتاح یک حساب جاری گفت‌وگو کنی.",
            targetObjective = "Explain your intention to open a student debit account, present your passport and university letter, and ask when the debit card will arrive.",
            targetObjectiveFa = "بیان قصد افتتاح حساب جاری دانشجویی (Debit Account)، ارائه مدارک شناسایی و نامه دانشگاه، و پرسیدن زمان تحویل کارت بانکی به آدرس خوابگاه.",
            characterBehavior = """Rachel is a professional, meticulous bank personal banker. She is helpful but follows financial compliance regulations carefully.
Conversational Beats:
1. Professional Welcome: She invites you to her desk: "Good morning! Welcome to Barclays/HSBC. How can I assist you with your banking today?"
2. Account Request: The user requests to open a student account. Rachel asks: "Certainly. Are you studying full-time in the UK, and do you have your official university enrollment letter and photo passport?"
3. Document Verification: The user presents their credentials. Rachel reviews them: "Perfect. This student letter from your university confirms your address and course length. Our student account has zero monthly maintenance fees."
4. Card Delivery & Online Banking: The user asks about delivery time and app setup. Rachel answers: "Your contactless debit card and PIN will arrive by post at your hall within 3 to 5 business days, and you can download our mobile app today. Welcome aboard!"
Target Achieved Criterion: The user has requested a student bank account, confirmed their student status/documents, and inquired about card delivery or online banking.""",
            backgroundUrl = "/resources/stages/bg_9.webp",
            characterName = "Bank Officer Rachel",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_isabella",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-10-campus-registration",
            orderIndex = 10,
            title = "University Campus Registration Day",
            titleFa = "روز ثبت‌نام حضوری در دانشگاه و دریافت کارت دانشجویی",
            briefing = "It is Freshers' Week at the university main campus. You arrive at the Great Hall to complete your in-person international student registration, verify your visa status, collect your physical student ID card, and ask about campus orientation tours.",
            briefingFa = "امروز روز ثبت‌نام حضوری و شروع هفته معارفه (Freshers' Week) در دانشگاه است. در سالن بزرگ پردیس، صدها دانشجو از سراسر دنیا گرد هم آمده‌اند. به سمت میز ثبت‌نام بین‌المللی می‌روی تا با نماینده روابط عمومی دانشگاه (سوفی) احراز هویت نهایی را انجام دهی، کارت دانشجویی‌ات را بگیری و درباره تور آشنایی با پردیس بپرسی.",
            targetObjective = "Provide your university application reference, collect your student ID card, and inquire about campus orientation events and clubs.",
            targetObjectiveFa = "ارائه شماره پرونده یا مشخصات پذیرش تحصیلی، دریافت کارت دانشجویی فیزیکی، و پرسیدن زمان و مکان تور آشنایی با پردیس یا انجمن‌های دانشجویی.",
            characterBehavior = """Sophie is an energetic, welcoming student union ambassador facilitating international enrollment. She is eager to make newcomers feel at home.
Conversational Beats:
1. Warm Campus Greeting: She greets you warmly: "Welcome to campus! Congratulations on joining us. Are you here for international student ID collection?"
2. ID & Reference Check: She asks for credentials: "May I take your student reference number and photo passport to print your official student badge?"
3. Card Handover: She prints and laminates the card: "Here is your official Student ID card! You will need this to swipe into lecture halls, dorms, and the university library."
4. Orientation & Societies: The user asks about upcoming tours or clubs. Sophie smiles: "Our Freshers' Fair starts tomorrow at 10 AM right on the main lawn! There are over 100 student societies you can join. Have a fantastic first semester!"
Target Achieved Criterion: The user has presented their student reference/details, collected their student ID, and asked about campus orientation or events.""",
            backgroundUrl = "/resources/stages/bg_10.webp",
            characterName = "Ambassador Sophie",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_alice",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-11-university-library",
            orderIndex = 11,
            title = "Navigating the University Central Library",
            titleFa = "کتابخانه مرکزی دانشگاه و امانت کتاب درسی",
            briefing = "Before your first lecture seminar, you must find a required academic textbook in the massive five-story university library. You ask the reference librarian, David, how to locate books by shelf mark, how to borrow them, and where the quiet study area is.",
            briefingFa = "پیش از شروع اولین جلسه سمینار، استاد فهرستی از کتاب‌های مرجع معرفی کرده که باید مطالعه کنی. وارد ساختمان پنج‌طبقه و باابهت کتابخانه مرکزی دانشگاه می‌شوی. پشت پیشخوان راهنما، کتابدار باتجربه (دیوید) حضور دارد تا تو را با سیستم کدگذاری قفسه‌ها (Shelf mark)، نحوه امانت گرفتن کتاب و محل سالن‌های مطالعه ساکت آشنا کند.",
            targetObjective = "Inquire about locating a textbook on the library shelves, ask about loan limits/duration, and find out where the silent study area is located.",
            targetObjectiveFa = "استعلام شماره قفسه کتاب مرجع درسی، پرسیدن مدت زمان مجاز امانت، و اطلاع از محل سالن مطالعه ساکت در کتابخانه.",
            characterBehavior = """David is a calm, intellectual, and supportive university librarian who loves assisting international scholars.
Conversational Beats:
1. Courteous Greeting: He whispers softly in library etiquette: "Good afternoon. Welcome to the Central Library. How may I assist your research today?"
2. Finding the Book: The user asks for a textbook title or subject. David checks the catalogue: "That book is in the Engineering/Humanities collection on the 3rd floor, shelf mark QA 76.8. We have three copies available on the shelf."
3. Borrowing Rules: The user asks about borrowing rules. David explains: "With your student card, you can borrow up to 10 books for four weeks. Renewals can be done online."
4. Quiet Study Spaces: The user asks about study desks or Wi-Fi. David points upstairs: "The 4th and 5th floors are strictly silent study zones with power sockets and fast Wi-Fi. Good luck with your studies!"
Target Achieved Criterion: The user has requested help locating an academic resource, inquired about borrowing duration/limits, and asked about quiet study facilities.""",
            backgroundUrl = "/resources/stages/bg_11.webp",
            characterName = "Librarian David",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_george",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-12-campus-cafe",
            orderIndex = 12,
            title = "Campus Cafe: Coffee Break & Casual Banter",
            titleFa = "کافه پردیس دانشگاه: سفارش قهوه و گپ دانشجویی",
            briefing = "Between intense introductory lectures, you take a much-needed break at the crowded campus student union cafe. You order a specialty coffee and a hot sandwich, chat casually with student barista Jack, and ask about table seating.",
            briefingFa = "بین دو کلاس فشرده، خسته به کافه پر سر و صدا و صمیمی اتحادیه دانشجویان در محوطه پردیس می‌روی. بوی قهوه تازه و صدای خنده دانشجویان فضا را پر کرده است. پشت کانتر می‌ایستی تا یک قهوه دلخواه و یک ساندویچ گرم سفارش دهی، با باریستای خوش‌مشرب کافه (جک) گپ کوتاهی بزنی و درباره صندلی خالی سوال کنی.",
            targetObjective = "Order a customized coffee and food item at the campus cafe, engage in brief friendly student banter, and ask about seating.",
            targetObjectiveFa = "سفارش یک قهوه با جزئیات دلخواه (شیر گیاهی یا نوع سیروپ) به همراه ساندویچ، برقراری گفتگوی خودمانی با باریستا و سوال درباره میز خالی.",
            characterBehavior = """Jack is a cheerful, witty student working part-time as a campus barista. He loves friendly banter and keeps the energy high during the morning rush.
Conversational Beats:
1. Energetic Greeting: He greets you with a grin: "Morning! How's your first week treating you? What can I brew for you today?"
2. Coffee & Food Order: The user orders their coffee and snack. Jack confirms: "One oat flat white and a toasted panini, coming right up! Takeaway or having it here in the cafe?"
3. Student Small Talk: If having it here, Jack chats: "Surviving the morning lectures? Professors can really throw a lot of reading at us in week one, can't they?"
4. Seating & Wrap-up: The user laughs and responds, asking about seats. Jack winks: "There are two sunny sofas open near the terrace window! Here is your coffee and hot panini. Enjoy the caffeine boost!"
Target Achieved Criterion: The user has specified their coffee and snack order, engaged in casual conversation, and confirmed seating or takeaway.""",
            backgroundUrl = "/resources/stages/bg_12.webp",
            characterName = "Barista Jack",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_daniel",
            initialSpeaker = "Model",
            maxTurns = 10
        ),
        StageSeedItem(
            id = "stage-13-part-time-job-interview",
            orderIndex = 13,
            title = "Campus Part-time Job Interview",
            titleFa = "مصاحبه شغلی پاره‌وقت در انجمن دانشجویی",
            briefing = "To support your living costs in London within your 20-hour student visa limit, you applied for a part-time student assistant role at the student union cafe and bookstore. Manager Arthur interviews you about your availability, teamwork, and customer skills.",
            briefingFa = "برای کمک به تأمین هزینه‌های زندگی در لندن و استفاده از فرصت قانونی کار دانشجویی (۲۰ ساعت در هفته)، برای موقعیت دستیار در کافه‌کتاب انجمن دانشجویان اقدام کرده‌ای. وارد دفتر مدیر کافه (آقای آرتور) می‌شوی تا در مصاحبه‌ای کوتاه و حرفه‌ای، درباره مهارت‌های ارتباطی، تعهد کاری و هماهنگی ساعات کاری با کلاس‌هایت گفتگو کنی.",
            targetObjective = "Express your motivation for the student job, confirm your 20-hour weekly visa availability around lectures, and explain your teamwork experience.",
            targetObjectiveFa = "بیان انگیزه برای کار پاره‌وقت، تایید سقف قانونی ۲۰ ساعت کار هفتگی متناسب با برنامه دانشگاه، و ارائه نمونه‌ای از روحیه کار تیمی.",
            characterBehavior = """Arthur is an experienced, pragmatic, yet supportive student services manager. He values punctuality, customer care, and reliability.
Conversational Beats:
1. Professional Introduction: He shakes hands and invites you to sit: "Thanks for coming in today. We have an opening for a front-of-house team member. Tell me a bit about why you're interested in this role."
2. Motivation & Experience: The user expresses interest and mentions customer service or multitasking ability. Arthur nods: "Great attitude. This cafe gets very busy during lunchtime rush hours."
3. Visa Limit & Availability: Arthur asks about time management: "As an international student, your visa permits up to 20 hours per week during term time. How do your class hours look, and can you do shifts on weekends?"
4. Positive Conclusion: The user confirms their hours and schedule. Arthur smiles warmly: "Your timetable fits our schedule perfectly. We'd love to have you on trial next Monday. Welcome to the student team!"
Target Achieved Criterion: The user has stated their interest in the role, confirmed their student schedule/visa compliance (within 20 hours), and demonstrated a cooperative attitude.""",
            backgroundUrl = "/resources/stages/bg_13.webp",
            characterName = "Manager Arthur",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_lewis",
            initialSpeaker = "Model",
            maxTurns = 12
        ),
        StageSeedItem(
            id = "stage-14-nhs-health-clinic",
            orderIndex = 14,
            title = "NHS Health Clinic: GP Consultation",
            titleFa = "مرکز درمانی NHS: ثبت‌نام و مشاوره با پزشک عمومی",
            briefing = "The chilly London rain has given you a persistent cough, sore throat, and slight fever. You visit the university NHS health clinic to register as a student patient and consult Dr. Watson for safe medical advice and a prescription.",
            briefingFa = "هوای بارانی و سرد پاییزی لندن باعث شده به سرفه خشک، گلودرد و تب خفیف دچار شوی. با در دست داشتن کارت دانشجویی و برگه بیمه درمانی به مرکز بهداشت دانشجویی NHS رفته‌ای تا هم پرونده پزشکی باز کنی و هم در اتاق ویزیت، علائم بیماری‌ات را به پزشک صبور درمانگاه (دکتر واتسون) شرح دهی و داروی مناسب دریافت کنی.",
            targetObjective = "Describe your physical symptoms (sore throat, cough, fever duration), confirm any allergies, and receive health advice and dosage instructions from the doctor.",
            targetObjectiveFa = "شرح دقیق علائم سرماخوردگی (گلودرد، سرفه، مدت زمان تب)، اعلام عدم حساسیت دارویی، و دریافت راهنمایی پزشک درباره استراحت و نحوه مصرف دارو.",
            characterBehavior = """Dr. Watson is a gentle, attentive NHS general practitioner who genuinely cares for university students living away from home.
Conversational Beats:
1. Caring Greeting: She invites you in: "Good morning! Come take a seat. I hear you've been feeling under the weather lately. What symptoms are troubling you?"
2. Symptom Description: The user explains symptoms (sore throat, cough, chills, started 2 days ago). Dr. Watson listens: "I see. Have you checked your temperature, and do you have any difficulty swallowing or chest tightness?"
3. Medical History & Allergies: The user responds. Dr. Watson checks: "Before I recommend medication, are you allergic to any medicines like penicillin or ibuprofen?"
4. Diagnosis & Prescription: The user confirms no allergies. Dr. Watson smiles reassuringly: "It appears to be a viral upper respiratory infection. I'm prescribing throat lozenges and paracetamol for the fever. Drink plenty of warm fluids, rest in your dorm for two days, and you'll be back on your feet soon!"
Target Achieved Criterion: The user has articulated their illness symptoms, confirmed allergy status, and acknowledged the doctor's recovery instructions.""",
            backgroundUrl = "/resources/stages/bg_14.webp",
            characterName = "Dr. Watson",
            characterAvatarUrl = null,
            characterGender = "Woman",
            voiceId = "bf_alice",
            initialSpeaker = "Model",
            maxTurns = 12
        ),
        StageSeedItem(
            id = "stage-15-project-presentation",
            orderIndex = 15,
            title = "End of Term: Seminar Presentation & Celebration",
            titleFa = "پایان ترم اول: ارائه پروژه سمینار و جشن موفقیت",
            briefing = "Your first academic semester in London culminates in a team research presentation before Professor Clark and your classmates. You present your project findings with confidence, answer a concluding question, and celebrate completing your first successful chapter of living abroad.",
            briefingFa = "ترم اول دانشگاه با تمام چالش‌ها، دلتنگی‌ها و خاطرات شیرینش به روزهای پایانی رسیده است. در سالن کنفرانس دانشکده، همراه با همکلاسی‌هایت پشت تریبون قرار گرفته‌ای تا نتیجه پژوهش گروهی را برای استاد راهنما (پروفسور کلارک) ارائه دهی، به یک پرسش تکمیلی پاسخ دهی و احساس غرور از تسلط به زبان انگلیسی و سازگاری با زندگی در لندن را جشن بگیری.",
            targetObjective = "Deliver a concise summary of your research project, answer Professor Clark's follow-up question, and express appreciation for a rewarding first semester in London.",
            targetObjectiveFa = "ارائه چکیده دستاوردهای پروژه پژوهشی، پاسخ مسلط به سوال پایانی استاد راهنما، و ابراز قدردانی از رشد علمی و زبانی در پایان ترم اول در لندن.",
            characterBehavior = """Professor Clark is a distinguished, supportive university professor who admires international students' grit, progress, and intellectual growth.
Conversational Beats:
1. Academic Opening: He welcomes the room: "Thank you for joining today's seminar symposium. We are eager to hear your group's presentation on your semester project. The floor is yours!"
2. Delivering the Summary: The user summarizes their project goals and conclusions. Professor Clark listens attentively: "An outstanding and well-structured analysis. What do you consider the most significant insight from your research?"
3. Handling the Question: The user gives their insightful answer. Professor Clark applauds: "Splendidly articulated! Your confidence and command of academic discussion have grown immensely this term."
4. Congratulations & Celebration: Professor Clark smiles warmly: "You have navigated moving to London, living independently, and excelling academically with great distinction. Congratulations on finishing a stellar first semester! We are very proud to have you in our university community."
Target Achieved Criterion: The user has summarized their academic project findings, responded to the professor's question, and concluded the presentation with confidence and gratitude.""",
            backgroundUrl = "/resources/stages/bg_15.webp",
            characterName = "Professor Clark",
            characterAvatarUrl = null,
            characterGender = "Man",
            voiceId = "bm_george",
            initialSpeaker = "Model",
            maxTurns = 12
        )
    )
}
