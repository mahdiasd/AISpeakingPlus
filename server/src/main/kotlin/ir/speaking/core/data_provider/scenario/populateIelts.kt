package ir.speaking.core.data_provider.scenario

import ir.speaking.core.utils.now
import ir.speaking.feature.category.db.CategoryTable
import ir.speaking.feature.chat.model.Role
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.user.model.Gender
import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

fun populateIelts() {
    transaction {
        val cId = CategoryTable.insertAndGetId {
            it[name] = "IELTS Exam"
            it[imageUrl] = "category/ielts_exam.webp"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario1Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "IELTS Speaking Part 1"
            it[persianTitle] = "آیلتس اسپیکینگ بخش اول"
            it[description] =
                "You've just entered the IELTS speaking test room. The examiner greets you and, after checking your ID, begins Part 1 with personal questions about topics like your hometown, work, or daily life. This 4-5 minute section is designed to help you relax and showcase your ability to communicate on familiar topics."
            it[persianDescription] =
                "شما به‌تازگی وارد اتاق آزمون اسپیکینگ آیلتس شده‌اید. ممتحن به شما خوش‌آمد می‌گوید و پس از بررسی کارت شناسایی، بخش اول را با سؤالات شخصی در مورد موضوعاتی مانند شهر محل زندگی، کار یا زندگی روزمره شما آغاز می‌کند. این بخش ۴-۵ دقیقه‌ای طراحی شده تا به شما کمک کند آرامش خود را حفظ کرده و توانایی خود را در صحبت کردن درباره موضوعات آشنا نشان دهید."
            it[aiRole] = "IELTS Examiner"
            it[aiName] = "Ms. Richardson"
            it[gender] = Gender.Woman
            it[points] = 60
            it[createdAt] = LocalDateTime.now()
            it[imageUrl] = "scenario/ielts_exam/ielts_speaking_part_1.webp"
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[starter] = Role.Model
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] =
                "Introduce yourself, briefly mentioning your professional or academic background and your reasons for taking the IELTS exam."
            it[persianDescription] =
                "خود را معرفی کنید و به‌طور خلاصه به سوابق حرفه‌ای یا تحصیلی خود و دلایل‌تان برای شرکت در آزمون آیلتس اشاره کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] =
                "Describe your hometown or current city, focusing on its key features, cultural life, and what you personally like or dislike about it."
            it[persianDescription] =
                "شهر محل تولد یا سکونت فعلی خود را با تمرکز بر ویژگی‌های اصلی، زندگی فرهنگی و آنچه شخصاً در مورد آن دوست دارید یا ندارید، توصیف کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario1Id.value
            it[description] =
                "Talk about your hobbies and interests, explaining how you balance them with your work or study commitments."
            it[persianDescription] =
                "در مورد سرگرمی‌ها و علایق خود صحبت کنید و توضیح دهید که چگونه بین آن‌ها و تعهدات کاری یا تحصیلی خود تعادل برقرار می‌کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario2Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "IELTS Speaking Part 2"
            it[persianTitle] = "آیلتس اسپیکینگ بخش دوم"
            it[description] =
                "For Part 2, the examiner gives you a cue card with a topic. You have one minute to prepare and make notes, then you must speak for 1-2 minutes without interruption. This part tests your ability to organize your thoughts and speak at length on a specific topic."
            it[persianDescription] =
                "در بخش دوم، ممتحن یک کارت راهنما (cue card) با یک موضوع مشخص به شما می‌دهد. شما یک دقیقه فرصت دارید تا آماده شوید و یادداشت بردارید، سپس باید به مدت ۱ تا ۲ دقیقه بدون وقفه صحبت کنید. این بخش توانایی شما را در سازماندهی افکار و صحبت کردن طولانی در مورد یک موضوع خاص می‌سنجد."
            it[aiRole] = "IELTS Examiner"
            it[aiName] = "Mr. Thompson"
            it[gender] = Gender.Man
            it[points] = 70
            it[createdAt] = LocalDateTime.now()
            it[imageUrl] = "scenario/ielts_exam/ielts_speaking_part_2.webp"
            it[aiAvatar] = "avatar/ai_avatar_5.webp"
            it[starter] = Role.Model
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] =
                "Describe a book that had a significant influence on you. You should say what the book is about, why you decided to read it, and explain how it changed your perspective."
            it[persianDescription] =
                "کتابی را توصیف کنید که تأثیر قابل توجهی بر شما داشته است. باید بگویید موضوع کتاب چیست، چرا تصمیم به خواندن آن گرفتید و توضیح دهید چگونه دیدگاه شما را تغییر داد."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario2Id.value
            it[description] =
                "Flesh out your talk with specific examples from the book. Conclude by explaining whether you would recommend it to others and why."
            it[persianDescription] =
                "صحبت خود را با مثال‌های مشخصی از کتاب کامل کنید. در پایان توضیح دهید که آیا آن را به دیگران توصیه می‌کنید و چرا."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario3Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "IELTS Speaking Part 3"
            it[persianTitle] = "آیلتس اسپیکینگ بخش سوم"
            it[description] =
                "Following your Part 2 topic, Part 3 expands into a two-way discussion on related, more abstract themes. This section challenges you to analyze, compare, and speculate, demonstrating advanced vocabulary and grammatical structures."
            it[persianDescription] =
                "در ادامه موضوع بخش دوم، بخش سوم به یک گفتگوی دوطرفه در مورد موضوعات مرتبط اما انتزاعی‌تر گسترش می‌یابد. این بخش شما را به تحلیل، مقایسه و گمانه‌زنی به چالش می‌کشد تا دایره واژگان و ساختارهای گرامری پیشرفته خود را نشان دهید."
            it[aiRole] = "IELTS Examiner"
            it[aiName] = "Dr. Mitchell"
            it[gender] = Gender.Woman
            it[points] = 75
            it[createdAt] = LocalDateTime.now()
            it[imageUrl] = "scenario/ielts_exam/ielts_speaking_part_3.webp"
            it[aiAvatar] = "avatar/ai_avatar_1.webp"
            it[starter] = Role.Model
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] =
                "Analyze how reading habits have evolved with the rise of digital technology. Compare the experience of reading a physical book versus reading on a digital device."
            it[persianDescription] =
                "تحلیل کنید که عادات مطالعه با ظهور فناوری دیجیتال چگونه تکامل یافته است. تجربه خواندن یک کتاب فیزیکی را با خواندن روی یک دستگاه دیجیتال مقایسه کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] =
                "Discuss the role of literature in shaping cultural values in your society. Provide examples if you can."
            it[persianDescription] =
                "درباره نقش ادبیات در شکل دادن به ارزش‌های فرهنگی در جامعه خود بحث کنید. در صورت امکان، مثال‌هایی ارائه دهید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario3Id.value
            it[description] =
                "Evaluate the argument that governments should fund public libraries to promote reading. What are the potential benefits and drawbacks?"
            it[persianDescription] =
                "این استدلال را که دولت‌ها باید برای ترویج مطالعه از کتابخانه‌های عمومی حمایت مالی کنند، ارزیابی کنید. مزایا و معایب بالقوه آن چیست؟"
            it[createdAt] = LocalDateTime.now()
        }

        val scenario4Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Writing Task 2 Strategy"
            it[persianTitle] = "استراتژی رایتینگ تسک ۲"
            it[description] =
                "In this simulation, you'll discuss an IELTS Writing Task 2 topic with an examiner. Instead of writing the essay, you will verbally outline your ideas, structure, and arguments. This exercise hones your ability to quickly analyze a prompt and organize a coherent response."
            it[persianDescription] =
                "در این شبیه‌سازی، شما درباره یک موضوع رایتینگ تسک ۲ آیلتس با ممتحن گفتگو می‌کنید. به جای نوشتن مقاله، شما ایده‌ها، ساختار و استدلال‌های خود را به صورت شفاهی تشریح می‌کنید. این تمرین توانایی شما را در تحلیل سریع موضوع و سازماندهی یک پاسخ منسجم تقویت می‌کند."
            it[aiRole] = "IELTS Writing Examiner"
            it[aiName] = "Dr. Lane"
            it[gender] = Gender.Woman
            it[points] = 75
            it[createdAt] = LocalDateTime.now()
            it[imageUrl] = "scenario/ielts_exam/ielts_writing_task_2.webp"
            it[aiAvatar] = "avatar/ai_avatar_3.webp"
            it[starter] = Role.Model
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "First, analyze the essay prompt: identify the key topic, the specific question, and any opposing viewpoints to address."
            it[persianDescription] = "ابتدا، موضوع انشا را تحلیل کنید: موضوع اصلی، سؤال مشخص و هر دیدگاه مخالفی که باید به آن پرداخته شود را شناسایی کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Next, present your main thesis statement. This should be a clear, concise sentence that directly answers the essay question."
            it[persianDescription] = "سپس، بیانیه اصلی (thesis) خود را ارائه دهید. این باید یک جمله واضح و مختصر باشد که مستقیماً به سؤال انشا پاسخ می‌دهد."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Outline the main ideas for two body paragraphs. For each, state the topic sentence and the supporting examples you would use."
            it[persianDescription] = "ایده‌های اصلی برای دو پاراگراف بدنه را تشریح کنید. برای هر کدام، جمله موضوع (topic sentence) و مثال‌های پشتیبانی که استفاده خواهید کرد را بیان کنید."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario4Id.value
            it[description] = "Finally, explain how you would structure your conclusion to summarize your points and reinforce your thesis."
            it[persianDescription] = "در نهایت، توضیح دهید که چگونه نتیجه‌گیری خود را برای خلاصه کردن نکات و تأکید مجدد بر بیانیه اصلی‌تان ساختاربندی می‌کنید."
            it[createdAt] = LocalDateTime.now()
        }

        val scenario5Id = ScenarioTable.insertAndGetId {
            it[categoryId] = cId.value
            it[title] = "Reading Strategy Session"
            it[persianTitle] = "جلسه استراتژی ریدینگ"
            it[description] =
                "In this session, you will discuss effective strategies for IELTS Reading questions. The examiner will present a question type (e.g., True/False/Not Given), and you will explain your approach, including time management, skimming, and scanning techniques."
            it[persianDescription] =
                "در این جلسه، شما در مورد استراتژی‌های مؤثر برای سؤالات ریدینگ آیلتس بحث خواهید کرد. ممتحن یک نوع سؤال (مانند صحیح/غلط/نامشخص) را مطرح می‌کند و شما رویکرد خود، شامل مدیریت زمان، تکنیک‌های اسکیمینگ و اسکنینگ را توضیح خواهید داد."
            it[aiRole] = "IELTS Reading Examiner"
            it[aiName] = "Ms. Harper"
            it[gender] = Gender.Woman
            it[points] = 70
            it[createdAt] = LocalDateTime.now()
            it[imageUrl] = "scenario/ielts_exam/ielts_reading.webp"
            it[aiAvatar] = "avatar/ai_avatar_2.webp"
            it[starter] = Role.Model
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "For a 'Matching Headings' question, explain your strategy. How do you quickly grasp the main idea of each paragraph?"
            it[persianDescription] = "استراتژی خود را برای سؤالات 'تطبیق عناوین' توضیح دهید. چگونه به سرعت ایده اصلی هر پاراگراف را درک می‌کنید؟"
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "If faced with a 'True/False/Not Given' question, describe how you scan for keywords and what distinguishes 'False' from 'Not Given'."
            it[persianDescription] = "اگر با سؤال 'صحیح/غلط/نامشخص' روبرو شدید، توضیح دهید چگونه برای یافتن کلمات کلیدی متن را اسکن می‌کنید و چه چیزی 'غلط' را از 'نامشخص' متمایز می‌کند."
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Discuss your approach to vocabulary-in-context questions. How do you infer the meaning of an unfamiliar word?"
            it[persianDescription] = "رویکرد خود را به سؤالات مربوط به واژگان در متن مورد بحث قرار دهید. چگونه معنای یک کلمه ناآشنا را استنباط می‌کنید؟"
            it[createdAt] = LocalDateTime.now()
        }

        ScenarioTaskTable.insert {
            it[scenarioId] = scenario5Id.value
            it[description] = "Explain your overall time management strategy for the 60-minute Reading test to ensure you complete all three passages."
            it[persianDescription] = "استراتژی کلی مدیریت زمان خود را برای آزمون ۶۰ دقیقه‌ای ریدینگ توضیح دهید تا اطمینان حاصل کنید که هر سه متن را به پایان می‌رسانید."
            it[createdAt] = LocalDateTime.now()
        }
    }
}