package ir.aispeaking.leitner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.leitner.component.FocusedCardDialog
import ir.aispeaking.leitner.component.LeitnerCardGridItem
import ir.aispeaking.leitner.component.LeitnerTabsRow
import ir.aispeaking.leitner.component.LeitnerTopBar
import ir.aispeaking.leitner.dialog.AddWordDialog
import ir.aispeaking.leitner.dialog.DeleteCardConfirmDialog
import ir.aispeaking.leitner.dialog.LeitnerReminderSheet
import ir.aispeaking.leitner.dialog.LeitnerReviewDialog
import ir.aispeaking.leitner.model.LeitnerTab
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_add
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.dictionary.DictionaryDetailSheet
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun LeitnerScreen(
    viewModel: LeitnerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = AppTheme.isDark

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF090B10) else Color(0xFFF6F8FA))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // TopBar with AppBackButton (Start Anchor / Top Right), Symmetrical Title, and Reminder Bell
                LeitnerTopBar(
                    onNavigateBack = onNavigateBack,
                    onReminderClick = { viewModel.openReminderSheet() },
                    hasActiveReminder = !uiState.reminderTime.isNullOrBlank()
                )

                // Filter Tabs: [ 🎯 امروز برای مرور ], [ همه ], [ جعبه ۱..۵ ]
                LeitnerTabsRow(
                    selectedTab = uiState.selectedTab,
                    dueCount = uiState.totalDueCount,
                    allCount = uiState.allCards.size,
                    boxCounts = uiState.boxCounts,
                    onSelectTab = { viewModel.selectTab(it) }
                )

                // Main Content
                if (uiState.isGuest) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(if (isDark) Color(0xD9181A22) else Color(0xF2FFFFFF))
                                .border(
                                    1.dp,
                                    if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f),
                                    RoundedCornerShape(22.dp)
                                )
                                .padding(24.dp)
                        ) {
                            GameText(
                                text = "🔐",
                                size = 40.sp,
                                align = TextAlign.Center
                            )
                            GameText(
                                text = "برای استفاده از جعبه لایتنر وارد شوید",
                                size = 16.sp,
                                bold = true,
                                color = Game.TextPrimary,
                                align = TextAlign.Center
                            )
                            GameText(
                                text = "کلمات جدیدی که در طول مراحل ذخیره می‌کنید در جعبه لایتنر شما نگهداری و طبق برنامه مرور می‌شوند.",
                                size = 13.sp,
                                color = Game.TextSecondary,
                                align = TextAlign.Center
                            )
                            GameButton(
                                text = "ورود یا ثبت‌نام",
                                onClick = onNavigateToLogin,
                                style = GameButtonStyle.Primary,
                                modifier = Modifier.fillMaxWidth(),
                                height = 48.dp,
                                textSize = 14.sp
                            )
                        }
                    }
                } else if (uiState.isLoading && uiState.allCards.isEmpty() && uiState.dueCards.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AppLoadingIndicator(
                            color = Color(0xFF06B6D4),
                            size = 40.dp
                        )
                    }
                } else {
                    val displayCards = uiState.displayCards

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Hero CTA Banner on "Due Today" tab
                        if (uiState.selectedTab is LeitnerTab.DueToday) {
                            item(span = { GridItemSpan(2) }) {
                                if (uiState.dueCards.isNotEmpty()) {
                                    DueReviewCtaBanner(
                                        dueCount = uiState.dueCards.size,
                                        onStartReview = { viewModel.startReviewSession() }
                                    )
                                } else {
                                    AllDoneBanner()
                                }
                            }
                        }

                        // Empty State if no cards exist for this tab
                        if (displayCards.isEmpty() && uiState.selectedTab !is LeitnerTab.DueToday) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GameText(
                                            text = "📦",
                                            size = 36.sp
                                        )
                                        GameText(
                                            text = "هنوز کارتی در این بخش وجود ندارد.",
                                            size = 14.sp,
                                            color = Game.TextSecondary,
                                            align = TextAlign.Center
                                        )
                                        GameText(
                                            text = "می‌تونی با دکمه + کلمه دلخواهت رو اضافه کنی یا در حین مراحل روی کلمات تپ کنی.",
                                            size = 12.sp,
                                            color = Game.TextMuted,
                                            align = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2-Column Cards Grid
                        items(displayCards, key = { it.id }) { card ->
                            LeitnerCardGridItem(
                                card = card,
                                onClick = { viewModel.openFocusedCard(card) },
                                onDeleteClick = { viewModel.requestDeleteCard(card) }
                            )
                        }
                    }
                }
            }

            // Floating Action Button (+ Add Word FAB)
            if (!uiState.isGuest) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(bottom = 24.dp, end = 20.dp)
                ) {
                    LeitnerAddWordFab(
                        onClick = { viewModel.openAddWordDialog() }
                    )
                }
            }

            // Add Word Modal Dialog
            AnimatedVisibility(
                visible = uiState.isAddWordDialogOpen,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                AddWordDialog(
                    onDismiss = { viewModel.closeAddWordDialog() },
                    onConfirm = { word -> viewModel.searchAndOpenDictionary(word) }
                )
            }

            // Dictionary Detail Bottom Sheet
            DictionaryDetailSheet(
                visible = uiState.isDictionarySheetOpen,
                targetWord = uiState.dictionaryTargetWord,
                entry = uiState.dictionaryEntry,
                isLoading = uiState.isDictionaryLoading,
                errorMessage = uiState.dictionaryError,
                isInvalidWordError = uiState.isDictionaryInvalidWord,
                isAudioPlaying = uiState.isDictionaryAudioPlaying,
                isAddedToLeitner = uiState.isCardAddedToLeitner,
                isAddingToLeitner = uiState.isAddingCardToLeitner,
                onDismiss = { viewModel.closeDictionarySheet() },
                onRetry = { viewModel.retryDictionaryLookup() },
                onEditWord = { viewModel.editInvalidDictionaryWord() },
                onPlayAudio = { url -> viewModel.playDictionaryAudio(url) },
                onStopAudio = { viewModel.stopDictionaryAudio() },
                onAddToLeitner = { entry -> viewModel.addCardFromDictionary(entry) }
            )

            // Focused 3D Card Inspection Modal
            AnimatedVisibility(
                visible = uiState.focusedCard != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.focusedCard?.let { card ->
                    FocusedCardDialog(
                        card = card,
                        onDismiss = { viewModel.closeFocusedCard() },
                        onPlayAudio = { viewModel.playAudio(it) },
                        isAudioPlaying = uiState.isAudioPlaying && uiState.audioPlayingCardId == card.id,
                        onDeleteClick = { viewModel.requestDeleteCard(it) }
                    )
                }
            }

            // Delete Confirmation Dialog
            AnimatedVisibility(
                visible = uiState.cardPendingDelete != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uiState.cardPendingDelete?.let { card ->
                    DeleteCardConfirmDialog(
                        card = card,
                        isDeleting = uiState.isDeletingCard,
                        onDismiss = { viewModel.cancelDeleteCard() },
                        onConfirm = { viewModel.confirmDeleteCard() }
                    )
                }
            }

            // Fullscreen 3D Coverflow Review Session Modal
            AnimatedVisibility(
                visible = uiState.isReviewSessionOpen,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LeitnerReviewDialog(
                    reviewQueue = uiState.reviewQueue,
                    currentIndex = uiState.currentReviewIndex,
                    promotedCount = uiState.promotedCardsCount,
                    demotedCount = uiState.demotedCardsCount,
                    isFinished = uiState.isReviewSessionFinished,
                    onReviewCard = { card, rem -> viewModel.submitCardReview(card, rem) },
                    onDismiss = { viewModel.dismissReviewSession() },
                    onPlayAudio = { viewModel.playAudio(it) },
                    isAudioPlaying = uiState.isAudioPlaying
                )
            }

            // Reminder Time Stepper BottomSheet
            if (uiState.isReminderSheetOpen) {
                LeitnerReminderSheet(
                    initialHour = uiState.reminderHour,
                    initialMinute = uiState.reminderMinute,
                    onSave = { hour, minute ->
                        viewModel.setReminderTime(hour, minute)
                    },
                    onDismiss = { viewModel.dismissReminderSheet() }
                )
            }
        }
    }
}

/**
 * Apple Design Floating Action Button for adding custom words to Leitner deck.
 */
@Composable
private fun LeitnerAddWordFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "fab_press_scale"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(56.dp)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF0A84FF), Color(0xFF0066CC))
                )
            )
            .border(1.5.dp, Color.White.copy(alpha = 0.45f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_add),
            contentDescription = "افزودن کلمه جدید به لایتنر",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun DueReviewCtaBanner(
    dueCount: Int,
    onStartReview: () -> Unit
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = if (isDark) 0.22f else 0.16f),
                        Color(0xFF3B82F6).copy(alpha = if (isDark) 0.18f else 0.12f)
                    )
                )
            )
            .border(
                1.5.dp,
                Color(0xFF06B6D4).copy(alpha = if (isDark) 0.55f else 0.45f),
                shape
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        GameText(
                            text = "مرور آماده",
                            size = 11.sp,
                            bold = true,
                            color = Color.White
                        )
                    }

                    GameText(
                        text = "$dueCount کارت نوبت مروره".fa(),
                        size = 14.sp,
                        bold = true,
                        color = Game.TextPrimary
                    )
                }
            }

            GameText(
                text = "کارت‌هایی که امروز زمان مرورشون رسیده رو بررسی کن تا به جعبه‌های بالاتر ارتقا پیدا کنن.",
                size = 12.sp,
                color = Game.TextSecondary
            )

            GameButton(
                text = "🚀 شروع مرور ۳بعدی ($dueCount کارت)".fa(),
                onClick = onStartReview,
                style = GameButtonStyle.Primary,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp,
                textSize = 14.sp
            )
        }
    }
}

@Composable
private fun AllDoneBanner() {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isDark) Color(0x1A22C55E) else Color(0x1422C55E))
            .border(1.dp, Color(0xFF22C55E).copy(alpha = 0.35f), shape)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameText(
                text = "🎉 آفرین! کارت‌های امروز رو مرور کردی",
                size = 15.sp,
                bold = true,
                color = Color(0xFF22C55E),
                align = TextAlign.Center
            )
            GameText(
                text = "برای اضافه کردن کارت جدید، حین مکالمه در مراحل کلمات نامفهوم رو لمس کن یا دکمه + رو بزن.",
                size = 12.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )
        }
    }
}
