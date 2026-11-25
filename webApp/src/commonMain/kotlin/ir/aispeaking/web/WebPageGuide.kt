package ir.aispeaking.web

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.navigation.AppRoute
import ir.aispeaking.navigation.IntroVideoRoute
import ir.aispeaking.navigation.LeaderboardRoute
import ir.aispeaking.navigation.LeitnerRoute
import ir.aispeaking.navigation.LoginRoute
import ir.aispeaking.navigation.MainRoute
import ir.aispeaking.navigation.PaymentResultRoute
import ir.aispeaking.navigation.PlacementTestRoute
import ir.aispeaking.navigation.ProfileRoute
import ir.aispeaking.navigation.SplashRoute
import ir.aispeaking.navigation.StageChatRoute
import ir.aispeaking.navigation.StagesListRoute
import ir.aispeaking.navigation.SubscriptionRoute
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_key
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_payment
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_roadmap
import ir.aispeaking.sharedui.ic_scenario
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class PageGuideContent(
    val title: String,
    val icon: DrawableResource,
    val accentColor: Color,
    val tips: List<String>
)

private fun guideForRoute(route: AppRoute?): PageGuideContent = when (route) {
    is SplashRoute, null -> PageGuideContent(
        title = "خوش اومدی!",
        icon = Res.drawable.ic_lamp,
        accentColor = Color(0xFF38BDF8),
        tips = listOf(
            "چند لحظه صبر کن تا مسیر یادگیریت آماده بشه."
        )
    )
    is IntroVideoRoute -> PageGuideContent(
        title = "شروع ماجراجویی",
        icon = Res.drawable.ic_play,
        accentColor = Color(0xFF38BDF8),
        tips = listOf(
            "این ویدیو کوتاه بهت نشون میده چطور با هوش مصنوعی تمرین کنی.",
            "هر وقت آماده بودی، بزن بریم سراغ مرحله‌ها!"
        )
    )
    is LoginRoute -> PageGuideContent(
        title = "ورود به حساب",
        icon = Res.drawable.ic_key,
        accentColor = Color(0xFF34C759),
        tips = listOf(
            "شماره موبایلت رو وارد کن تا کد تایید برات بیاد.",
            "تمام پیشرفت و ستاره‌هات روی همین حساب ذخیره میشه."
        )
    )
    is MainRoute -> PageGuideContent(
        title = "مسیر مکالمه",
        icon = Res.drawable.ic_roadmap,
        accentColor = Color(0xFF38BDF8),
        tips = listOf(
            "روی «شروع مرحله» بزن تا وارد گفت‌وگوی واقعی بشی.",
            "از دکمه پایین هم می‌تونی همه مرحله‌ها رو ببینی."
        )
    )
    is StageChatRoute -> PageGuideContent(
        title = "تمرین مکالمه",
        icon = Res.drawable.ic_microphone,
        accentColor = Color(0xFF34C759),
        tips = listOf(
            "میکروفون رو بزن و راحت انگلیسی حرف بزن.",
            "نگران اشتباه نباش؛ هوش مصنوعی همون لحظه کمکت می‌کنه.",
            "با هندزفری صدات خیلی شفاف‌تر ضبط میشه."
        )
    )
    is StagesListRoute -> PageGuideContent(
        title = "همه مرحله‌ها",
        icon = Res.drawable.ic_scenario,
        accentColor = Color(0xFF5E5CE6),
        tips = listOf(
            "اینجا کل سناریوهای مسیرت رو یک‌جا می‌بینی.",
            "مرحله‌های باز رو می‌تونی دوباره تمرین کنی تا ۳ ستاره بگیری."
        )
    )
    is LeitnerRoute -> PageGuideContent(
        title = "جعبه لایتنر",
        icon = Res.drawable.ic_lamp,
        accentColor = Color(0xFFFFD60A),
        tips = listOf(
            "لغت‌های جدید مکالمه‌ها اینجا جمع میشن.",
            "مرور روزانه کمک می‌کنه کلمه‌ها همیشه توی ذهنت بمونن."
        )
    )
    is LeaderboardRoute -> PageGuideContent(
        title = "جدول امتیازها",
        icon = Res.drawable.ic_points,
        accentColor = Color(0xFFFF9F0A),
        tips = listOf(
            "با گرفتن ستاره و تمرین بیشتر، رتبه‌ت بالا میره.",
            "رقابت سالم با بقیه انگیزه تمرین رو بیشتر می‌کنه!"
        )
    )
    is PlacementTestRoute -> PageGuideContent(
        title = "تعیین سطح",
        icon = Res.drawable.ic_spell_check,
        accentColor = Color(0xFF38BDF8),
        tips = listOf(
            "چند سوال کوتاه جواب بده تا سطحت رو دقیق بشناسیم.",
            "اینطوری تمرین‌ها دقیقاً اندازه دانش خودت چیده میشه."
        )
    )
    is ProfileRoute -> PageGuideContent(
        title = "پروفایل تو",
        icon = Res.drawable.ic_profile,
        accentColor = Color(0xFF38BDF8),
        tips = listOf(
            "آمار پیشرفت، ستاره‌ها و وضعیت اشتراکت اینجاست.",
            "تنظیمات صدا و تم برنامه رو هم می‌تونی از اینجا عوض کنی."
        )
    )
    is SubscriptionRoute -> PageGuideContent(
        title = "اشتراک ویژه",
        icon = Res.drawable.ic_crown,
        accentColor = Color(0xFFFFD60A),
        tips = listOf(
            "با اشتراک، به همه سناریوها و تمرین نامحدود دسترسی داری.",
            "پلن دلخواهت رو انتخاب کن تا همون لحظه فعال بشه."
        )
    )
    is PaymentResultRoute -> PageGuideContent(
        title = "نتیجه پرداخت",
        icon = Res.drawable.ic_payment,
        accentColor = if (route.isSuccess) Color(0xFF34C759) else Color(0xFFFF453A),
        tips = listOf(
            if (route.isSuccess) {
                "تبریک! اشتراکت همین الان فعال شد و می‌تونی تمرین رو شروع کنی."
            } else {
                "پرداخت انجام نشد؛ می‌تونی دوباره تلاش کنی یا به صفحه اصلی برگردی."
            }
        )
    )
}

@Composable
fun WebPageGuideCard(
    currentRoute: AppRoute?,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val guideKey = currentRoute?.screenName ?: "Splash"

    AnimatedContent(
        targetState = guideKey to currentRoute,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(160))
        },
        label = "WebPageGuideTransition",
        modifier = modifier
    ) { (_, route) ->
        val guide = guideForRoute(route)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (isDark) Color(0x0EFFFFFF) else Color(0x07000000))
                .border(
                    width = 1.dp,
                    color = if (isDark) Color(0x18FFFFFF) else Color(0x10000000),
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(guide.accentColor.copy(alpha = if (isDark) 0.16f else 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(guide.icon),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(guide.accentColor)
                    )
                }
                Column {
                    GameText(
                        text = guide.title,
                        size = 13.sp,
                        bold = true,
                        color = Game.TextPrimary
                    )
                    Spacer(Modifier.height(1.dp))
                    GameText(
                        text = "راهنمای سریع این صفحه",
                        size = 10.sp,
                        color = Game.TextSecondary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                guide.tips.forEach { tip ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 7.dp)
                                .size(5.dp)
                                .background(guide.accentColor.copy(alpha = 0.85f), CircleShape)
                        )
                        GameText(
                            text = tip,
                            size = 12.sp,
                            lineHeight = 20.sp,
                            color = Game.TextSecondary
                        )
                    }
                }
            }
        }
    }
}
