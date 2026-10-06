package ir.aispeaking.sharedui.ui.stage

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_history
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_star_outline
import ir.aispeaking.sharedui.ui.component.AsyncStageBackground
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import androidx.compose.runtime.LaunchedEffect
import org.jetbrains.compose.resources.painterResource

@Composable
fun JourneyMapScreen(
    viewModel: JourneyMapViewModel,
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.refreshStages()
    }

    OnResume {
        viewModel.refreshStages()
    }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.selectedStageForPaywall) {
        if (uiState.selectedStageForPaywall != null) {
            viewModel.dismissPaywall()
            onNavigateToSubscription()
        }
    }
    val currentStage = uiState.currentStage

    val infiniteTransition = rememberInfiniteTransition(label = "screen_animations")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero_pulse"
    )
    val starPulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_pulse"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF070B19))
        ) {
            if (uiState.isLoading && currentStage == null) {
                // Loading indicator
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "در حال بارگذاری مرحله...",
                        color = Color(0xFFA5B4FC),
                        fontSize = 14.sp
                    )
                }
            } else {
                // 1. Fullscreen Stage Artwork Background
                AsyncStageBackground(
                    backgroundUrl = currentStage?.backgroundUrl,
                    modifier = Modifier.fillMaxSize(),
                    enableFrostedGlass = false
                ) {
                    // Soft Top Gradient (contrast for top header bar)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xCC070B19),
                                        Color(0x55070B19),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Soft Bottom Scrim (subtle contrast for bottom controls, leaves center 100% visible)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.45f)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x55070B19),
                                        Color(0xCC070B19),
                                        Color(0xEE070B19)
                                    )
                                )
                            )
                    )

                    // 2. Top Header Bar (Stars & Subscription)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Stars Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xCC0F172A))
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.horizontalGradient(
                                        listOf(Color(0xFFFFD700), Color(0xFFF59E0B))
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_star),
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier
                                        .size(16.dp)
                                        .scale(starPulseScale)
                                )
                                Text(
                                    text = "${uiState.totalStarsEarned} ستاره",
                                    color = Color(0xFFFFD700),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Subscription Badge and Profile Action Button
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (uiState.currentTier == AccessTier.SUBSCRIBER) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF7C3AED), Color(0xFF4F46E5))
                                            )
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            brush = Brush.horizontalGradient(
                                                listOf(Color(0xFFFFD700), Color(0xFFF59E0B))
                                            ),
                                            shape = RoundedCornerShape(22.dp)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_crown),
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "اشتراک ویژه",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // Clickable CTA leading to Subscription Paywall
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(22.dp))
                                        .shadow(
                                            elevation = 8.dp,
                                            shape = RoundedCornerShape(22.dp),
                                            spotColor = Color(0xFFFF5252)
                                        )
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFFF9F43), Color(0xFFFF5252))
                                            )
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(0x66FFFFFF),
                                            shape = RoundedCornerShape(22.dp)
                                        )
                                        .clickable { onNavigateToSubscription() }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_crown),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "خرید اشتراک",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Profile Action Button
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xCC0F172A))
                                    .border(
                                        width = 1.5.dp,
                                        color = Color(0x446366F1),
                                        shape = CircleShape
                                    )
                                    .clickable { onNavigateToProfile() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_profile),
                                    contentDescription = "Profile",
                                    tint = Color(0xFFA5B4FC),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Indicator if inspecting a past stage
                    if (uiState.isViewingPastStage) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xEE1E293B))
                                .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(18.dp))
                                .clickable { viewModel.resetToActiveStage() }
                                .padding(horizontal = 14.dp, vertical = 5.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_history),
                                    contentDescription = null,
                                    tint = Color(0xFFA5B4FC),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "مرور مرحله گذشته",
                                    color = Color(0xFFA5B4FC),
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "✕ بازگشت",
                                    color = Color(0xFFFFD700),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 3. Bottom Controls Area (Clean Title Card + Action Buttons)
                    if (currentStage != null) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Stage Persian Title Card (Clean & frosted, does not obstruct artwork)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xB30F172A))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x33FFFFFF),
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = currentStage.titleFa,
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Hero Button: "شروع مرحله" (Eye-catching, 2D game-button style, well-proportioned)
                            Button(
                                onClick = { viewModel.onStartCurrentStage() },
                                modifier = Modifier
                                    .widthIn(max = 320.dp)
                                    .fillMaxWidth(0.88f)
                                    .height(50.dp)
                                    .scale(pulseScale)
                                    .shadow(
                                        elevation = 12.dp,
                                        shape = RoundedCornerShape(25.dp),
                                        spotColor = Color(0xFF00C896),
                                        ambientColor = Color(0xFF00C896)
                                    ),
                                shape = RoundedCornerShape(25.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0xFF00E5A3),
                                                    Color(0xFF00A86B)
                                                )
                                            )
                                        )
                                        .border(
                                            width = 1.5.dp,
                                            color = Color(0x66FFFFFF),
                                            shape = RoundedCornerShape(25.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_play),
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (uiState.isViewingPastStage) "تکرار مرحله" else "شروع مرحله",
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Secondary Link: "مرحله‌های گذشته" (Subtle & compact)
                            TextButton(
                                onClick = { viewModel.showPastStages() },
                                modifier = Modifier.height(36.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_history),
                                        contentDescription = null,
                                        tint = Color(0xFFA5B4FC),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "مرحله‌های گذشته",
                                        color = Color(0xFFA5B4FC),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (uiState.pastStages.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color(0x556366F1))
                                                .padding(horizontal = 6.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "${uiState.pastStages.size}",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Past Stages Bottom Sheet / Dialog
            if (uiState.showPastStagesSheet) {
                PastStagesDialog(
                    pastStages = uiState.pastStages,
                    currentViewingStage = currentStage,
                    onSelectStage = { stage -> viewModel.selectStageToView(stage) },
                    onDismiss = { viewModel.dismissPastStages() }
                )
            }

            // Briefing Dialog
            uiState.selectedStageForBriefing?.let { stage ->
                StageBriefingDialog(
                    stage = stage,
                    onDismiss = { viewModel.dismissBriefing() },
                    onStartMission = {
                        viewModel.dismissBriefing()
                        onNavigateToChat(stage.id)
                    }
                )
            }

            // Register BottomSheet
            uiState.selectedStageForRegister?.let { stage ->
                RegisterBottomSheet(
                    stage = stage,
                    onDismiss = { viewModel.dismissRegister() },
                    onRegisterSuccess = { viewModel.onRegisterSuccess() }
                )
            }

            // Snackbar
            uiState.snackbarMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearSnackbar() }) {
                            Text("متوجه شدم", color = Color.White)
                        }
                    }
                ) {
                    Text(message)
                }
            }
        }
    }
}

/**
 * Disney-inspired Past Stages Dialog displaying previous completed / accessible stages.
 */
@Composable
fun PastStagesDialog(
    pastStages: List<Stage>,
    currentViewingStage: Stage?,
    onSelectStage: (Stage) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_history),
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "مرحله‌های گذشته",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0x22FFFFFF))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "مراحل گذشته را مرور کرده و مهارت مکالمه خود را تقویت کنید",
                    color = Color(0xFFA5B4FC),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (pastStages.isEmpty()) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x330F172A))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_points),
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "شما در ابتدای مسیر جادویی خود هستید!",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "با تکمیل مرحله اول، سوابق و ستاره‌های شما در اینجا ذخیره خواهند شد.",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // List of past stages
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(pastStages) { stage ->
                            val isSelected = stage.id == currentViewingStage?.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectStage(stage) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF334155) else Color(0xFF0F172A)
                                ),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFD700))
                                } else {
                                    androidx.compose.foundation.BorderStroke(1.dp, Color(0x22FFFFFF))
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Number badge
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF6366F1)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${stage.orderIndex}",
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = stage.titleFa,
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = stage.title,
                                                color = Color(0xFFA5B4FC),
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            // Stars row with vector icons
                                            stage.userProgress?.let { progress ->
                                                Row(
                                                    modifier = Modifier.padding(top = 2.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    repeat(3) { starIndex ->
                                                        val isFilled = starIndex < progress.stars
                                                        Icon(
                                                            painter = painterResource(
                                                                if (isFilled) Res.drawable.ic_star else Res.drawable.ic_star_outline
                                                            ),
                                                            contentDescription = null,
                                                            tint = if (isFilled) Color(0xFFFFD700) else Color(0x44FFFFFF),
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = { onSelectStage(stage) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF475569)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = if (isSelected) "مشاهده‌شده" else "انتخاب",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("بستن", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
