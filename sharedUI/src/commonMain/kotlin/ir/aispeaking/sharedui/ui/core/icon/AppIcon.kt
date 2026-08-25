package ir.aispeaking.sharedui.ui.core.icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.iconSize
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource


@Composable
fun MeetMenuIcon(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    tint: Color = AppTheme.colors.onSurface,
    contentDescription: String? = null,
    containerColor: Color? = null,
    iconSize: Dp = if (containerColor == null) 42.dp else 42.dp,
    paddingValues: PaddingValues = PaddingValues(if (containerColor == null) 8.dp else 8.dp),
    onClick: () -> Unit = {}
) {
    Icon(
        modifier = modifier
            .iconSize(iconSize)
            .then(
                if (containerColor != null) Modifier.background(
                    containerColor,
                    CircleShape
                ) else Modifier
            )
            .padding(paddingValues)
            .animateClickable { onClick() },
        painter = painterResource(icon),
        contentDescription = contentDescription,
        tint = tint
    )
}

@Composable
fun AppIcon(
    icon: DrawableResource,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color? = AppTheme.colors.onSurface,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null
) {
    Icon(
        modifier = Modifier
            .then(
                if (onClick != null) Modifier.animateClickable(onClick)
                else Modifier
            )
            .size(size)
            .then(modifier),
        painter = painterResource(icon),
        contentDescription = contentDescription,
        tint = tint ?: LocalContentColor.current
    )
}

@Composable
fun AppIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = AppTheme.colors.onSurface,
    contentDescription: String? = null,
    onClick: () -> Unit = {}
) {
    Icon(
        modifier = Modifier
            .animateClickable { onClick() }
            .size(size)
            .then(modifier),
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tint
    )
}


@Preview
@Composable
fun PreviewMeetMenuIcon() {
    AppTheme {
        MeetMenuIcon(icon = Res.drawable.ic_profile, contentDescription = "Add")
    }
}

@Preview
@Composable
fun PreviewMeetMenuIconWithContainer() {
    AppTheme {
        MeetMenuIcon(
            icon = Res.drawable.ic_profile,
            contentDescription = "Add",
            containerColor = AppTheme.colors.onSurface
        )
    }
}


@Preview
@Composable
fun PreviewAppIconDrawable() {
    AppTheme {
        AppIcon(icon = Res.drawable.ic_profile, contentDescription = "Add")
    }
}

@Preview
@Composable
fun PreviewAppIconVector() {
    AppTheme {
    }
}