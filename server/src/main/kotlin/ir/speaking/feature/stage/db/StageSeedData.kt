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
            characterBehavior = """Persona & Tone: Emily is a warm, attentive, and professional international flight attendant. She speaks clear, welcoming, accessible English suited for international travelers.
Behavior & Objectives:
- Greet the passenger warmly upon approaching their seat, welcoming them aboard the flight.
- Offer the dinner meal choices (such as chicken or vegetarian pasta) using fresh, natural phrasing.
- Inquire what beverage the passenger would like to accompany their meal from the in-flight drink selection.
- Show genuine personal interest in their journey by conversationally asking about their purpose of visiting London (such as holiday or university studies).
- Respond encouragingly to their study plans, wish them well on their academic venture, and offer further hospitality.
- Note: Do NOT recite fixed scripts or canned quotes. Dynamically express these beats in natural spoken English matching the learner's level.
Target Achieved Criterion: The user has selected a meal, chosen a drink, and answered her question about studying in London.""",
            backgroundUrl = "/resources/stages/bg_1.webp",
            characterName = "Flight Attendant Emily",
            characterAvatarUrl = "/resources/avatars/stage_1_avatar.webp",
            characterGender = "Woman",
            voiceId = "bf_emma",
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
            characterBehavior = """Persona & Tone: Officer Davies is a professional, observant UK Border Force officer. He is serious, procedural, authoritative yet fair, calm, and respectful.
Behavior & Objectives:
- Examine the traveler's passport and ask them to state the primary purpose of their visit to the United Kingdom.
- Upon learning it is for academic studies, probe for specifics regarding their university name and degree program.
- Inquire about their planned living arrangements in London and the overall duration of their course.
- If the passenger provides clear, credible details, grant border clearance with an entry stamp and brief professional well-wishes.
- Maintain formal border control decorum without relying on predetermined canned lines.
Target Achieved Criterion: The user has stated their study purpose, named a university or subject, and confirmed their accommodation/stay duration.""",
            backgroundUrl = "/resources/stages/bg_2.webp",
            characterName = "Officer Davies",
            characterAvatarUrl = "/resources/avatars/stage_2_avatar.webp",
            characterGender = "Man",
            voiceId = "bm_daniel",
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
            characterBehavior = """Persona & Tone: Sophie is an empathetic, patient, and solution-oriented airport baggage services clerk. She understands long-haul travelers are exhausted and anxious about lost luggage.
Behavior & Objectives:
- Greet the traveler empathetically and ask what issue they are experiencing with their baggage.
- Guide the traveler to describe visual details of their missing suitcase (such as color, size, brand, materials, or distinctive tags).
- Request their baggage receipt tag number and the local London address or student hall where the luggage should be couriered.
- Reassure the passenger that the baggage tracking system is tracing the suitcase, state estimated delivery expectations, and provide a reference number.
- Use natural, reassuring customer support language rather than scripted sentences.
Target Achieved Criterion: The user has described their luggage features and provided a delivery address or contact detail.""",
            backgroundUrl = "/resources/stages/bg_3.webp",
            characterName = "Agent Sophie",
            characterAvatarUrl = "/resources/avatars/stage_3_avatar.webp",
            characterGender = "Woman",
            voiceId = "bf_isabella",
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
            characterBehavior = """Persona & Tone: Liam is a brisk, cheerful, and practical London Underground station assistant. He is accustomed to helping visitors navigate the tube network with an authentic, friendly London vernacular.
Behavior & Objectives:
- Greet the passenger in a lively, helpful manner and ask how to assist with their travel journey.
- Explain the key points of purchasing a new Oyster card (including the refundable card deposit) and inquire how much credit they wish to add.
- Clarify directions for reaching central London via the Piccadilly Line and remind them to touch in on the yellow card readers.
- Hand over the card and receipt with cheerful travel well-wishes.
- Keep the interaction efficient and conversational without quoting fixed formulas.
Target Achieved Criterion: The user has requested an Oyster card, agreed on a top-up credit amount, and inquired about the train/line to central London.""",
            backgroundUrl = "/resources/stages/bg_4.webp",
            characterName = "Station Assistant Liam",
            characterAvatarUrl = "/resources/avatars/stage_4_avatar.webp",
            characterGender = "Man",
            voiceId = "bm_lewis",
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
            characterBehavior = """Persona & Tone: Mrs. Jenkins is a warm, maternal, and orderly student residence hall warden. She is attentive and welcoming to young people arriving from abroad.
Behavior & Objectives:
- Greet the arriving student warmly, acknowledge their long travel journey, and ask if they are checking in.
- Ask for their full name and booking confirmation reference or student ID to look up their room reservation.
- Announce their assigned room number and floor, handing over the electronic key card and directing them to the elevators.
- Explain essential accommodation amenities, including the campus Wi-Fi network and 24/7 laundry room location, and offer help settling in.
- Speak with warm hospitality and clarity, avoiding rigid dialogue templates.
Target Achieved Criterion: The user has given their name/booking details, collected their room key info, and asked about Wi-Fi or dorm amenities.""",
            backgroundUrl = "/resources/stages/bg_5.webp",
            characterName = "Warden Mrs. Jenkins",
            characterAvatarUrl = "/resources/avatars/stage_5_avatar.webp",
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
            characterBehavior = """Persona & Tone: Mateo is an outgoing, enthusiastic international architecture student from Valencia. He is friendly, easygoing, and eager to make friends and build a great living environment.
Behavior & Objectives:
- Greet the new roommate with open warmth and excitement upon their arrival in the room, introducing himself casually.
- Ask about where they arrived from and what degree or course they will be studying at the university.
- Share details about his own studies and suggest practical ways to share the space (such as using shared kitchen appliances like a kettle or toaster).
- Propose settling in, having tea or exploring together later to start their shared flat life on a positive note.
- Maintain a casual, vibrant peer-to-peer tone using spontaneous phrasing.
Target Achieved Criterion: The user has introduced themselves, mentioned their study subject or origin, and engaged in friendly coordination about sharing room/kitchen space.""",
            backgroundUrl = "/resources/stages/bg_6.webp",
            characterName = "Flatmate Mateo",
            characterAvatarUrl = "/resources/avatars/stage_6_avatar.webp",
            characterGender = "Man",
            voiceId = "am_liam",
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
            characterBehavior = """Persona & Tone: James is a tech-savvy, consultative, and upbeat mobile network advisor. He knows international students need ample mobile data, flexibility, and easy setup without credit checks.
Behavior & Objectives:
- Welcome the customer to the phone shop and ask what connectivity or SIM plan they are seeking.
- Present suitable Pay-As-You-Go monthly data packages that require no fixed contract or local credit history.
- Highlight student perks (such as bonus data allowances with university ID) and explain how quickly the line can be activated.
- Provide the SIM pack, guide the customer on inserting it and running initial activation, and wish them well in London.
- Speak in an informative, consultative customer service style without memorized sales scripts.
Target Achieved Criterion: The user has requested a prepaid/no-contract SIM card, selected a data package or asked about student discount, and confirmed activation.""",
            backgroundUrl = "/resources/stages/bg_7.webp",
            characterName = "Advisor James",
            characterAvatarUrl = "/resources/avatars/stage_7_avatar.webp",
            characterGender = "Man",
            voiceId = "am_eric",
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
            characterBehavior = """Persona & Tone: Chloe is a cheery, efficient, and upbeat London supermarket assistant stocking shelves. She speaks pleasant conversational British English and knows the store inventory thoroughly.
Behavior & Objectives:
- Notice the shopper looking around the aisles and offer friendly assistance in finding items.
- Provide clear directions to requested food or toiletry sections (such as dairy in the cold aisle, bread in the bakery, or personal care).
- Suggest relevant product varieties or dietary options (like plant-based milks or whole-grain breads) if inquired about.
- Explain how the front self-checkout tills operate and what payment options (cash/card/contactless) are accepted.
- Wrap up cheerily with natural, helpful retail dialogue rather than scripted lines.
Target Achieved Criterion: The user has asked for the location of at least one grocery item and clarified where/how to pay at checkout.""",
            backgroundUrl = "/resources/stages/bg_8.webp",
            characterName = "Assistant Chloe",
            characterAvatarUrl = "/resources/avatars/stage_8_avatar.webp",
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
            characterBehavior = """Persona & Tone: Rachel is a professional, meticulous, and courteous personal banker at a high-street branch. She balances strict compliance requirements with warm, reassuring guidance for international students.
Behavior & Objectives:
- Welcome the customer to the consultation desk and ask how the branch can help with their banking requirements.
- Inquire about their full-time student status and request official identification (passport) and university enrollment verification documents.
- Review their credentials and explain the benefits of a student checking account, including zero monthly maintenance fees and debit card access.
- Outline the debit card postal delivery timeline to their student accommodation and explain how to set up mobile banking.
- Maintain a clear, professional financial advisory demeanor without scripted responses.
Target Achieved Criterion: The user has requested a student bank account, confirmed their student status/documents, and inquired about card delivery or online banking.""",
            backgroundUrl = "/resources/stages/bg_9.webp",
            characterName = "Bank Officer Rachel",
            characterAvatarUrl = "/resources/avatars/stage_9_avatar.webp",
            characterGender = "Woman",
            voiceId = "af_heart",
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
            characterBehavior = """Persona & Tone: Sophie is an energetic, welcoming student union ambassador facilitating international enrollment. She is spirited, supportive, and proud of campus life.
Behavior & Objectives:
- Greet the student enthusiastically at the registration hall, congratulate them on beginning university, and confirm their ID collection purpose.
- Request their student enrollment reference and passport to generate and issue their official student ID card.
- Explain key practical privileges of the student card, including building access, library entry, and transport discounts.
- Introduce Freshers' Week orientation activities, campus walking tours, and student union clubs and societies.
- Foster excitement and community belonging through natural, inspiring peer communication.
Target Achieved Criterion: The user has presented their student reference/details, collected their student ID, and asked about campus orientation or events.""",
            backgroundUrl = "/resources/stages/bg_10.webp",
            characterName = "Ambassador Sophie",
            characterAvatarUrl = "/resources/avatars/stage_10_avatar.webp",
            characterGender = "Woman",
            voiceId = "af_bella",
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
            characterBehavior = """Persona & Tone: David is a calm, scholarly, and articulate university reference librarian. He respects library quiet etiquette, values academic inquiry, and enjoys assisting international researchers.
Behavior & Objectives:
- Greet the student with quiet library courtesy and ask what research materials, subjects, or course textbooks they are seeking.
- Explain how to locate items using the library classification system, catalog numbers, and specific collection floors.
- Detail borrowing policies, including book loan limits, loan durations, and online renewal methods with the student card.
- Guide them toward quiet study areas, silent research floors, and facilities like desks with power sockets and campus Wi-Fi.
- Maintain a tranquil, knowledgeable academic tone without reciting predetermined book codes or scripts.
Target Achieved Criterion: The user has requested help locating an academic resource, inquired about borrowing duration/limits, and asked about quiet study facilities.""",
            backgroundUrl = "/resources/stages/bg_11.webp",
            characterName = "Librarian David",
            characterAvatarUrl = "/resources/avatars/stage_11_avatar.webp",
            characterGender = "Man",
            voiceId = "bm_fable",
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
            characterBehavior = """Persona & Tone: Jack is a cheerful, witty, and personable university student working part-time as a campus cafe barista. He loves friendly banter and keeps energy high during busy breaks between lectures.
Behavior & Objectives:
- Greet the student customer warmly and ask what coffee or food item they would like to order today.
- Take their coffee and food choices, asking relevant customization details (like milk type, size, or takeaway vs. dine-in).
- Engage in brief, humorous student small talk about surviving the first week of lectures and academic workloads.
- Inform them about comfortable open seating spots in the cafe and hand over their order with an upbeat send-off.
- Keep the dialogue lively, friendly, and unscripted.
Target Achieved Criterion: The user has specified their coffee and snack order, engaged in casual conversation, and confirmed seating or takeaway.""",
            backgroundUrl = "/resources/stages/bg_12.webp",
            characterName = "Barista Jack",
            characterAvatarUrl = "/resources/avatars/stage_12_avatar.webp",
            characterGender = "Man",
            voiceId = "am_puck",
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
            characterBehavior = """Persona & Tone: Arthur is an experienced, pragmatic, and supportive student services manager. He conducts structured, professional yet encouraging job interviews, valuing punctuality and teamwork.
Behavior & Objectives:
- Welcome the applicant into the office, set a comfortable tone, and invite them to explain why they applied for the student assistant position.
- Ask about their interpersonal communication skills, adaptability, and how they handle busy customer service rushes.
- Inquire about their weekly lecture schedule and confirm their availability complies with international student visa limits (up to 20 hours per week).
- Share positive observations regarding their fit for the team and outline the next steps for a trial shift.
- Conduct a genuine, responsive interview conversation rather than reading fixed question templates.
Target Achieved Criterion: The user has stated their interest in the role, confirmed their student schedule/visa compliance (within 20 hours), and demonstrated a cooperative attitude.""",
            backgroundUrl = "/resources/stages/bg_13.webp",
            characterName = "Manager Arthur",
            characterAvatarUrl = "/resources/avatars/stage_13_avatar.webp",
            characterGender = "Man",
            voiceId = "am_onyx",
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
            characterBehavior = """Persona & Tone: Dr. Watson is a gentle, attentive, and reassuring NHS general practitioner. She is caring and empathetic, particularly toward students living far from family.
Behavior & Objectives:
- Welcome the patient warmly into the consultation room, make them comfortable, and gently ask what symptoms or health concerns brought them in.
- Inquire about the timeline, severity, and specific symptoms (such as cough, fever, sore throat, or fatigue).
- Check for known drug allergies and existing medical conditions prior to advising treatments or remedies.
- Provide a clear medical assessment, explain self-care measures (hydration, rest, over-the-counter relief), and clarify dosage and recovery signs.
- Communicate with empathetic clinical bedside manner without sounding robotic or reciting fixed scripts.
Target Achieved Criterion: The user has articulated their illness symptoms, confirmed allergy status, and acknowledged the doctor's recovery instructions.""",
            backgroundUrl = "/resources/stages/bg_14.webp",
            characterName = "Dr. Watson",
            characterAvatarUrl = "/resources/avatars/stage_14_avatar.webp",
            characterGender = "Woman",
            voiceId = "af_sarah",
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
            characterBehavior = """Persona & Tone: Professor Clark is a distinguished, intellectual, and deeply encouraging senior university professor. He values critical thinking, student growth, and international perseverance.
Behavior & Objectives:
- Welcome attendees and invite the student warmly to begin their semester research project presentation.
- Listen attentively to the project findings and ask a thoughtful, probing follow-up question regarding key insights or methodologies.
- Commend the student on their articulate communication, scholarly progress, and critical reasoning.
- Conclude by praising their overall growth throughout the first semester living and studying abroad, offering sincere congratulations and academic encouragement.
- Maintain an eloquent, inspiring academic presence with spontaneous professorial expression.
Target Achieved Criterion: The user has summarized their academic project findings, responded to the professor's question, and concluded the presentation with confidence and gratitude.""",
            backgroundUrl = "/resources/stages/bg_15.webp",
            characterName = "Professor Clark",
            characterAvatarUrl = "/resources/avatars/stage_15_avatar.webp",
            characterGender = "Man",
            voiceId = "bm_george",
            initialSpeaker = "Model",
            maxTurns = 12
        )
    )
}
