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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
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
import ir.aispeaking.navigation.AppRoute
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ic_qr
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.web.IosChromeGuideModal
import ir.aispeaking.web.IosPwaTutorialModal
import ir.aispeaking.web.IosSafariCompassIcon
import ir.aispeaking.web.IosShareIcon
import ir.aispeaking.web.IosTopNoticeBanner
import ir.aispeaking.web.WebBrowserNavigation
import ir.aispeaking.web.WebPageGuideCard
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
            var currentRoute by remember { mutableStateOf<AppRoute?>(initialRoute) }

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
            val appContent = remember(initialRoute) {
                movableContentOf {
                    AppNavigation(
                        initialRoute = initialRoute,
                        onBackStackCreated = { backStack ->
                            WebBrowserNavigation(
                                backStack = backStack,
                                onRouteChanged = { currentRoute = it }
                            )
                        },
                        onExitApp = { exitWebApp() }
                    )
                }
            }

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
                                    // Right Panel (Start in RTL layout): Minimal Brand Header + Contextual Page Guide
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.CenterStart)
                                            .width(panelWidth)
                                    ) {
                                        WebBrandPanel(
                                            isDark = isDark,
                                            currentRoute = currentRoute
                                        )
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
                                    appContent()
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
                                    appContent()
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
private fun WebBrandPanel(
    isDark: Boolean,
    currentRoute: AppRoute?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Minimal App Identity Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .shadow(10.dp, RoundedCornerShape(13.dp), spotColor = Color(0x33000000))
                    .border(1.dp, if (isDark) Color(0x28FFFFFF) else Color(0x16000000), RoundedCornerShape(13.dp))
                    .clip(RoundedCornerShape(13.dp))
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_logo),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Column {
                GameText(text = "AI Speaking Plus", size = 17.sp, bold = true, latin = true)
                Spacer(Modifier.height(1.dp))
                GameText(
                    text = "دستیار هوشمند مکالمه",
                    size = 11.sp,
                    color = Game.Blue,
                    bold = true
                )
            }
        }

        // Contextual Guide for the Active Screen
        WebPageGuideCard(
            currentRoute = currentRoute,
            isDark = isDark
        )
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
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) Color(0x0DFFFFFF) else Color(0x06000000))
            .border(1.dp, if (isDark) Color(0x18FFFFFF) else Color(0x10000000), RoundedCornerShape(20.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(104.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_qr),
                contentDescription = "QR Code",
                modifier = Modifier.size(92.dp),
                colorFilter = ColorFilter.tint(Color.Black)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            GameText(
                text = "ادامه روی گوشی",
                size = 13.sp,
                bold = true,
                color = Game.TextPrimary
            )
            GameText(
                text = "با دوربین گوشی اسکن کن",
                size = 11.sp,
                color = Game.TextSecondary
            )
        }

        // Subtle Link to Landing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenLanding() }
                .background(if (isDark) Color(0x0EFFFFFF) else Color(0x07000000))
                .border(1.dp, if (isDark) Color(0x16FFFFFF) else Color(0x0E000000), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GameText(
                text = "معرفی امکانات در سایت",
                size = 11.sp,
                bold = true,
                color = Game.TextSecondary
            )
            Image(
                painter = painterResource(Res.drawable.ic_arrow_left),
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                colorFilter = ColorFilter.tint(Game.Blue)
            )
        }
    }
}
