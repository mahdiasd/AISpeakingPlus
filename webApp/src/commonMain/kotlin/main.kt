import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.aispeaking.chat.audio.DefaultStageAudioController
import ir.aispeaking.chat.audio.StageAudioController
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ic_qr
import ir.aispeaking.sharedui.ic_speaker
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_transcribe
import ir.aispeaking.sharedui.ic_voice
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import ir.aispeaking.web.IosChromeGuideModal
import ir.aispeaking.web.IosPwaTutorialModal
import ir.aispeaking.web.IosSafariCompassIcon
import ir.aispeaking.web.IosShareIcon
import ir.aispeaking.web.IosTopNoticeBanner
import ir.aispeaking.web.WebBrowserNavigation
import ir.aispeaking.web.dismissIosA2hsBanner
import ir.aispeaking.web.dismissIosChromeBanner
import ir.aispeaking.web.exitWebApp
import ir.aispeaking.web.hideWebLoader
import ir.aispeaking.web.isUserInAppBrowser
import ir.aispeaking.web.resolveInitialWebRoute
import ir.aispeaking.web.setupWebAnalyticsBridge
import ir.aispeaking.web.setupWebAudioBridge
import ir.aispeaking.web.setupWebVideoBridge
import ir.aispeaking.web.shouldShowIosA2hsBanner
import ir.aispeaking.web.shouldShowIosChromeBanner
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    try {
        setupWebAnalyticsBridge()
    } catch (_: Throwable) {}

    try {
        val audioController = (GlobalContext.get().getOrNull<StageAudioController>() as? DefaultStageAudioController)
            ?: GlobalContext.get().getOrNull<DefaultStageAudioController>()
        if (audioController != null) {
            setupWebAudioBridge(audioController)
        }
    } catch (_: Throwable) {}

    try {
        setupWebVideoBridge()
    } catch (_: Throwable) {}

    ComposeViewport {
        AppTheme {
            val isDark = AppTheme.isDark
            val initialRoute = remember { resolveInitialWebRoute() }

            LaunchedEffect(Unit) {
                // Allow the first frame and text shapes to settle before fading out the HTML loader
                delay(150)
                hideWebLoader()
            }

            var showA2hsBanner by remember { mutableStateOf(shouldShowIosA2hsBanner()) }
            var showChromeBanner by remember { mutableStateOf(shouldShowIosChromeBanner()) }
            var showIosTutorialModal by remember { mutableStateOf(false) }
            var showChromeGuideModal by remember { mutableStateOf(false) }
            val isInApp = remember { isUserInAppBrowser() }

            // Apple-style ambient backdrop shown around the phone frame on large screens.
            val backdropBrush = if (isDark) {
                Brush.radialGradient(
                    0.0f to Color(0xFF161822),
                    0.6f to Color(0xFF0A0C10),
                    1.0f to Color(0xFF000000)
                )
            } else {
                Brush.radialGradient(
                    0.0f to Color(0xFFF9FAFB),
                    0.6f to Color(0xFFEDF0F3),
                    1.0f to Color(0xFFE2E6EB)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backdropBrush),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val isWideScreen = maxWidth > 560.dp
                    val showSidePanels = maxWidth >= 1060.dp

                    if (isWideScreen) {
                        // Desktop / tablet: centered phone-sized stage
                        val phoneWidth = min(420.dp, maxWidth - 32.dp)
                        val phoneHeight = min(880.dp, maxHeight - 32.dp)
                        val phoneShape = RoundedCornerShape(28.dp)

                        // 1. Apple-style ambient luminous glow centered behind the phone
                        Box(
                            modifier = Modifier
                                .size(phoneWidth + 140.dp, phoneHeight + 90.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        0.0f to if (isDark) Color(0x280A84FF) else Color(0x180A84FF),
                                        0.5f to if (isDark) Color(0x125E5CE6) else Color(0x0A5E5CE6),
                                        1.0f to Color.Transparent
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // 2. Side Panels (Symmetrically flanking the centered phone)
                        if (showSidePanels) {
                            val availableSideSpace = ((maxWidth - phoneWidth) / 2) - 40.dp
                            val panelWidth = min(280.dp, availableSideSpace)

                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 32.dp)
                                ) {
                                    // Right Panel (Start in RTL layout): Brand Identity & Feature Highlights
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .width(panelWidth)
                                    ) {
                                        WebBrandPanel(isDark = isDark)
                                    }

                                    // Left Panel (End in RTL layout): Mobile QR & Companion Tips
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .width(panelWidth)
                                    ) {
                                        WebCompanionPanel(
                                            isDark = isDark,
                                            onOpenLanding = { exitWebApp() }
                                        )
                                    }
                                }
                            }
                        }

                        // 3. Centered Phone Stage (Fixed exactly at the center of the screen)
                        Box(
                            modifier = Modifier
                                .width(phoneWidth)
                                .height(phoneHeight)
                                .shadow(
                                    elevation = 36.dp,
                                    shape = phoneShape,
                                    spotColor = if (isDark) Color(0x99000000) else Color(0x28000000),
                                    ambientColor = if (isDark) Color(0x55000000) else Color(0x12000000)
                                )
                                .border(
                                    width = 1.5.dp,
                                    brush = if (isDark) {
                                        Brush.verticalGradient(listOf(Color(0x44FFFFFF), Color(0x14FFFFFF)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0x33000000), Color(0x10000000)))
                                    },
                                    shape = phoneShape
                                )
                                .clip(phoneShape)
                                .background(Game.Ink)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                AppTopBannerSection(
                                    showChromeBanner = showChromeBanner,
                                    showA2hsBanner = showA2hsBanner,
                                    onOpenChromeGuide = { showChromeGuideModal = true },
                                    onDismissChrome = {
                                        showChromeBanner = false
                                        dismissIosChromeBanner()
                                    },
                                    onOpenA2hsGuide = { showIosTutorialModal = true },
                                    onDismissA2hs = {
                                        showA2hsBanner = false
                                        dismissIosA2hsBanner()
                                    }
                                )
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    AppNavigation(
                                        initialRoute = initialRoute,
                                        onBackStackCreated = { WebBrowserNavigation(it) },
                                        onExitApp = { exitWebApp() }
                                    )
                                }
                            }

                            // Apple-style speaker / dynamic pill indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 8.dp)
                                    .size(width = 72.dp, height = 4.dp)
                                    .background(
                                        color = if (isDark) Color(0x35FFFFFF) else Color(0x20000000),
                                        shape = CircleShape
                                    )
                            )
                        }
                    } else {
                        // Phone browser: full-bleed app, no frame.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Game.Ink)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                AppTopBannerSection(
                                    showChromeBanner = showChromeBanner,
                                    showA2hsBanner = showA2hsBanner,
                                    onOpenChromeGuide = { showChromeGuideModal = true },
                                    onDismissChrome = {
                                        showChromeBanner = false
                                        dismissIosChromeBanner()
                                    },
                                    onOpenA2hsGuide = { showIosTutorialModal = true },
                                    onDismissA2hs = {
                                        showA2hsBanner = false
                                        dismissIosA2hsBanner()
                                    }
                                )
                                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                    AppNavigation(
                                        initialRoute = initialRoute,
                                        onBackStackCreated = { WebBrowserNavigation(it) },
                                        onExitApp = { exitWebApp() }
                                    )
                                }
                            }
                        }
                    }
                }

                if (showIosTutorialModal) {
                    IosPwaTutorialModal(
                        visible = showIosTutorialModal,
                        isInAppBrowser = isInApp,
                        onDismiss = { showIosTutorialModal = false }
                    )
                }

                if (showChromeGuideModal) {
                    IosChromeGuideModal(
                        visible = showChromeGuideModal,
                        onDismiss = { showChromeGuideModal = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppTopBannerSection(
    showChromeBanner: Boolean,
    showA2hsBanner: Boolean,
    onOpenChromeGuide: () -> Unit,
    onDismissChrome: () -> Unit,
    onOpenA2hsGuide: () -> Unit,
    onDismissA2hs: () -> Unit
) {
    if (showChromeBanner) {
        IosTopNoticeBanner(
            visible = true,
            text = "در آیفون، دسترسی میکروفون فقط در Safari فعال است. لطفاً با سافاری باز کنید.",
            actionText = "راهنما ›",
            badgeColor = Color(0xFFFF9500),
            badgeIcon = {
                IosSafariCompassIcon(
                    color = Color(0xFFFFB340),
                    modifier = Modifier.size(14.dp)
                )
            },
            onActionClick = onOpenChromeGuide,
            onDismiss = onDismissChrome
        )
    } else if (showA2hsBanner) {
        IosTopNoticeBanner(
            visible = true,
            text = "برای عملکرد بهتر و روان‌تر، برنامه را به صفحه اصلی اضافه کنید",
            actionText = "راهنما ›",
            badgeColor = Color(0xFF0A84FF),
            badgeIcon = {
                IosShareIcon(
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                )
            },
            onActionClick = onOpenA2hsGuide,
            onDismiss = onDismissA2hs
        )
    }
}

@Composable
private fun WebBrandPanel(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .shadow(12.dp, RoundedCornerShape(16.dp), spotColor = Color(0x33000000))
                    .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color(0x18000000), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                GameText(text = "AI Speaking Plus", size = 20.sp, bold = true, latin = true)
                Spacer(Modifier.height(2.dp))
                GameText(
                    text = "دستیار هوشمند مکالمه",
                    size = 12.sp,
                    color = Game.Blue,
                    bold = true
                )
            }
        }

        // Brief Value Text
        GameText(
            text = "مکالمه انگلیسی را در موقعیت‌های واقعی، بدون استرس و با فیدبک زنده هوش مصنوعی تمرین کنید.",
            size = 13.sp,
            lineHeight = 22.sp,
            color = Game.TextSecondary
        )

        // 3 Key Feature Chips
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            WebFeatureCard(
                icon = Res.drawable.ic_voice,
                iconTint = Color(0xFF38BDF8),
                title = "پارتنر صوتی طبیعی",
                subtitle = "لهجه‌های زنده آمریکایی و بریتانیایی",
                isDark = isDark
            )
            WebFeatureCard(
                icon = Res.drawable.ic_transcribe,
                iconTint = Color(0xFF34C759),
                title = "تصحیح زنده و فوری",
                subtitle = "ارزیابی پیوسته تلفظ و گرامر",
                isDark = isDark
            )
            WebFeatureCard(
                icon = Res.drawable.ic_star,
                iconTint = Color(0xFFFFD60A),
                title = "مسیر هدفمند و ماجراجویی",
                subtitle = "سناریوهای روزمره و کسب ستاره",
                isDark = isDark
            )
        }
    }
}

@Composable
private fun WebFeatureCard(
    icon: DrawableResource,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDark) Color(0x12FFFFFF) else Color(0x0A000000))
            .border(1.dp, if (isDark) Color(0x1AFFFFFF) else Color(0x10000000), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = if (isDark) 0.18f else 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                colorFilter = ColorFilter.tint(iconTint)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            GameText(text = title, size = 12.sp, bold = true)
            Spacer(Modifier.height(2.dp))
            GameText(text = subtitle, size = 11.sp, color = Game.TextSecondary)
        }
    }
}

@Composable
private fun WebCompanionPanel(
    isDark: Boolean,
    onOpenLanding: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000))
            .border(1.dp, if (isDark) Color(0x1EFFFFFF) else Color(0x14000000), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(Game.Green, CircleShape)
            )
            GameText(
                text = "ادامه تمرین روی گوشی",
                size = 14.sp,
                bold = true
            )
        }

        // QR Code Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isDark) Color(0x16FFFFFF) else Color(0x0E000000))
                .border(1.dp, if (isDark) Color(0x22FFFFFF) else Color(0x12000000), RoundedCornerShape(16.dp))
                .padding(vertical = 14.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_qr),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(96.dp),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
            GameText(
                text = "اسکن با دوربین گوشی",
                size = 12.sp,
                bold = true,
                color = Game.TextPrimary
            )
            GameText(
                text = "ورود مستقیم بدون نیاز به نصب برنامه",
                size = 11.sp,
                color = Game.TextSecondary
            )
        }

        // Audio Tip Box
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDark) Color(0x180A84FF) else Color(0x100A84FF))
                .border(1.dp, if (isDark) Color(0x330A84FF) else Color(0x200A84FF), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Game.Blue.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_speaker),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(Game.Blue)
                )
            }
            GameText(
                text = "توصیه مکالمه: جهت مکالمه شفاف و کاهش نویز، استفاده از هندزفری پیشنهاد می‌شود.",
                size = 11.sp,
                lineHeight = 18.sp,
                color = Game.TextSecondary
            )
        }

        // Action Link to Landing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenLanding() }
                .background(if (isDark) Color(0x10FFFFFF) else Color(0x08000000))
                .border(1.dp, if (isDark) Color(0x1AFFFFFF) else Color(0x10000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GameText(
                text = "وب‌سایت و معرفی امکانات",
                size = 12.sp,
                bold = true,
                color = Game.TextPrimary
            )
            Image(
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(Game.Blue)
            )
        }
    }
}
