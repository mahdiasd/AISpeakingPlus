package ir.aispeaking.sharedui.ui.core.toolbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme

import ir.aispeaking.sharedui.ui.core.text.HeadlineBoldText
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.iconSize
import ir.aispeaking.sharedui.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun DefaultLeftToolbarContent(
    modifier: Modifier = Modifier
        .iconSize()
        .padding(4.dp),
    icon: DrawableResource = Res.drawable.ic_back,
    tint: Color = AppTheme.colors.onSurface,
    onClick: (() -> Unit)?
) {
    Icon(
        modifier = modifier.then(
            if (onClick != null) Modifier.animateClickable { onClick() }
            else Modifier
        ),
        painter = painterResource(icon),
        contentDescription = "",
        tint = tint
    )
}

@Composable
fun DefaultRightToolbarContent(
    modifier: Modifier = Modifier
        .iconSize()
        .padding(4.dp),
     icon: DrawableResource,
    tint: Color = AppTheme.colors.onSurface,
    onClick: (() -> Unit)?
) {
    Icon(
        modifier = modifier.then(
            if (onClick != null) Modifier.animateClickable { onClick() }
            else Modifier
        ),
        painter = painterResource(icon),
        contentDescription = "",
        tint = tint
    )
}

@Composable
fun DefaultCenterToolbarContent(
    modifier: Modifier = Modifier,
    title: String,
) {
    HeadlineBoldText(
        modifier = modifier,
        text = title,
        textAlign = TextAlign.Center,
        color = AppTheme.colors.onSurface
    )
}

@Composable
fun AppToolbar(
    modifier: Modifier = Modifier.fillMaxWidth(),
    title: String?,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    containerColor: Color = AppTheme.colors.surface,
    onLeftIconClick: (() -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.HorizontalOrVertical = Arrangement.SpaceBetween,
    centerContent: (@Composable () -> Unit)? =
        when {
            title != null -> {
                { DefaultCenterToolbarContent(title = title) }
            }

            else -> null
        },
    leftContent: (@Composable () -> Unit)? =
        { DefaultLeftToolbarContent(onClick = onLeftIconClick) },

    rightContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .background(containerColor, shape = shape)
            .padding(paddingValues),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement
    ) {
        leftContent?.let {
            it()
        } ?: run {
            Spacer(modifier = Modifier.iconSize())
        }

        centerContent?.let {
            it()
        } ?: run {
            Spacer(modifier = Modifier.iconSize())
        }

        rightContent?.let {
            it()
        } ?: run {
            Spacer(modifier = Modifier.iconSize())
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // 1. Basic toolbar with only title and back button
            AppToolbar(
                title = "Basic Toolbar",
                onLeftIconClick = { /* Handle back click */ }
            )

            // 2. Toolbar without back button
            AppToolbar(
                title = "No Back Button",
                leftContent = null
            )

            // 3. Toolbar with custom right icon
            AppToolbar(
                title = "With Right Icon",
                rightContent = {
                    DefaultRightToolbarContent(
                        icon = Res.drawable.ic_back,
                        onClick = { /* Handle settings click */ }
                    )
                }
            )

            // 4. Toolbar with multiple right icons
            AppToolbar(
                title = "Multiple Actions",
                rightContent = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DefaultRightToolbarContent(
                            icon = Res.drawable.is_search,
                            onClick = { /* Handle search */ }
                        )
                        DefaultRightToolbarContent(
                            icon = Res.drawable.ic_qr,
                            onClick = { /* Handle more */ }
                        )
                    }
                }
            )

            // 5. Custom colored toolbar
            AppToolbar(
                title = "Custom Color",
                containerColor = AppTheme.colors.primary,
                leftContent = {
                    DefaultLeftToolbarContent(
                        tint = AppTheme.colors.onPrimary,
                        onClick = { /* Handle back */ }
                    )
                },
                rightContent = {
                    DefaultRightToolbarContent(
                        icon = Res.drawable.ic_qr,
                        tint = AppTheme.colors.onPrimary,
                        onClick = { /* Handle settings */ }
                    )
                }
            )

            // 7. Custom left icon
            AppToolbar(
                title = "Custom Left Icon",
                leftContent = {
                    DefaultLeftToolbarContent(
                        icon = Res.drawable.ic_qr,
                        onClick = { /* Handle menu */ }
                    )
                }
            )

            // 8. Toolbar without any actions
            AppToolbar(
                title = "No Actions",
                leftContent = null,
                rightContent = null
            )

            // 9. Right icon without click
            AppToolbar(
                title = "Disabled Right Icon",
                rightContent = {
                    DefaultRightToolbarContent(
                        icon = Res.drawable.ic_qr,
                        onClick = null
                    )
                }
            )

            // 10. Custom padding and shape
            AppToolbar(
                title = "Custom Style",
                paddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                containerColor = AppTheme.colors.primary.copy(alpha = 0.1f)
            )
        }
    }
}