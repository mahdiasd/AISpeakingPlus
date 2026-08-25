package ir.aispeaking.sharedui.ui.core.guide


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.sharedui.*

import ir.aispeaking.sharedui.ui.core.text.BodyLargeText
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun GuideDialog(
    modifier: Modifier,
    onDismiss: () -> Unit,
    list: ImmutableList<GuideModel>,
) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        GuideDialogContent(
            modifier = modifier,
            list = list,
            onDismiss = onDismiss
        )
    }
}

@Composable
fun GuideDialogContent(
    modifier: Modifier = Modifier,
    list: ImmutableList<GuideModel>,
    onDismiss: () -> Unit,
) {
    val images = list.map { guideModel -> guideModel.images.map { source -> source } }.flatten()

    Column(
        modifier = modifier
            .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundLarge)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.CenterVertically)
    ) {
//        TitleBoldText(text = stringResource(Res.string.guide) )

        Spacer(Modifier.height(16.dp))
        ModernImageSlider(
            modifier = Modifier.fillMaxWidth(),
            imageCornerRadius = 12.dp,
            imageSources = images
        )
        
        Spacer(Modifier.height(24.dp))

        BodyLargeText(
            modifier = Modifier.background(AppTheme.colors.primaryContainer , shape = AppTheme.shapes.roundSmall)
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .animateClickable { onDismiss() },
            persianFont = true,
            color = AppTheme.colors.onPrimaryContainer,
            text = stringResource(Res.string.guide_dialog_exit))

    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        GuideDialogContent(
            modifier = Modifier.fillMaxWidth(),
            onDismiss = {},
            list = immutableListOf(
                GuideModel.RegisterGuideModel(),
                GuideModel.ChallengeGuideModel(),
                GuideModel.LightenerGuideModel(),
            ).toImmutableList(),
        )
    }
}