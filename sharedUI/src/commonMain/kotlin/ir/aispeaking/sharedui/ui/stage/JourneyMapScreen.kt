package ir.aispeaking.sharedui.ui.stage

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus

@Composable
fun JourneyMapScreen(
    viewModel: JourneyMapViewModel,
    onNavigateToChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFF6366F1)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 40.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "مسیر سفر داستانی",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "از فرودگاه امام تهران تا لندن",
                        color = Color(0xFFA5B4FC),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                }

                itemsIndexed(uiState.stages) { index, stage ->
                    val isFirst = index == 0
                    val isLast = index == uiState.stages.size - 1

                    // Connecting vertical line before node
                    if (!isFirst) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(
                                    if (stage.lockStatus == StageLockStatus.UNLOCKED)
                                        Color(0xFF6366F1)
                                    else
                                        Color(0xFF334155)
                                )
                        )
                    }

                    StageNodeItem(
                        stage = stage,
                        onClick = { viewModel.onStageClicked(stage) }
                    )

                    // Connecting vertical line after node
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(32.dp)
                                .background(
                                    if (stage.lockStatus == StageLockStatus.UNLOCKED)
                                        Color(0xFF6366F1)
                                    else
                                        Color(0xFF334155)
                                )
                        )
                    }
                }
            }
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

        // Subscription Paywall Sheet
        uiState.selectedStageForPaywall?.let { stage ->
            SubscriptionPaywallSheet(
                stage = stage,
                onDismiss = { viewModel.dismissPaywall() },
                onSubscriptionSuccess = { viewModel.onSubscriptionSuccess() }
            )
        }

        // Story Prologue Dialog
        if (uiState.showPrologue) {
            StoryPrologueDialog(
                onDismiss = { viewModel.dismissPrologue() }
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

@Composable
fun StageNodeItem(
    stage: Stage,
    onClick: () -> Unit
) {
    val isUnlocked = stage.lockStatus == StageLockStatus.UNLOCKED
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Node circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .then(if (isUnlocked && stage.userProgress == null) Modifier.scale(pulseScale) else Modifier)
                .clip(CircleShape)
                .background(
                    when {
                        isUnlocked && (stage.userProgress?.stars ?: 0) > 0 -> Color(0xFF10B981) // Completed green
                        isUnlocked -> Color(0xFF6366F1) // Active purple
                        stage.lockStatus == StageLockStatus.LOCKED_REGISTRATION -> Color(0xFFF59E0B) // Amber
                        stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Color(0xFFEC4899) // Pink
                        else -> Color(0xFF475569) // Gray locked
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isUnlocked) {
                Text(
                    text = "${stage.orderIndex}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = when (stage.lockStatus) {
                        StageLockStatus.LOCKED_REGISTRATION -> "📱"
                        StageLockStatus.LOCKED_SUBSCRIPTION -> "👑"
                        else -> "🔒"
                    },
                    fontSize = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Node Info Card
        Card(
            modifier = Modifier
                .width(240.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF0F172A).copy(alpha = 0.6f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stage.titleFa,
                    color = if (isUnlocked) Color.White else Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stage.title,
                    color = if (isUnlocked) Color(0xFFA5B4FC) else Color(0xFF64748B),
                    fontSize = 12.sp
                )

                // Stars rating display
                stage.userProgress?.let { progress ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        repeat(3) { starIndex ->
                            Text(
                                text = if (starIndex < progress.stars) "⭐" else "☆",
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoryPrologueDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✈️ ماجراجویی به سوی لندن",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "به سفر هیجان‌انگیز یادگیری مکالمه خوش آمدید! در این مسیر داستانی، شما به عنوان یک مسافر از فرودگاه امام تهران حرکت کرده و در موقعیت‌های واقعی روزمره در لندن با شخصیت‌های گوناگون به زبان انگلیسی صحبت خواهید کرد.\n\nمرحله اول کاملاً رایگان و بدون نیاز به ثبت‌نام در دسترس شماست!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 14.sp,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Text("شروع ماجراجویی", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
