package ir.aispeaking.sharedui.utils.shape

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection


class TooltipShape(
    private val arrowWidth: Dp,
    private val arrowHeight: Dp,
    private val cornerRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val arrowWidthPx = with(density) { arrowWidth.toPx() }
        val arrowHeightPx = with(density) { arrowHeight.toPx() }
        val cornerRadiusPx = with(density) { cornerRadius.toPx() }

        // Start from top-left after corner
        path.moveTo(cornerRadiusPx, 0f)

        // Top edge
        path.lineTo(size.width - cornerRadiusPx, 0f)

        // Top-right corner
        path.arcTo(
            Rect(
                left = size.width - 2 * cornerRadiusPx,
                top = 0f,
                right = size.width,
                bottom = 2 * cornerRadiusPx
            ),
            270f, 90f, false
        )

        // Right edge
        path.lineTo(size.width, size.height - arrowHeightPx - cornerRadiusPx)

        // Bottom-right corner
        path.arcTo(
            Rect(
                left = size.width - 2 * cornerRadiusPx,
                top = size.height - 2 * cornerRadiusPx - arrowHeightPx,
                right = size.width,
                bottom = size.height - arrowHeightPx
            ),
            0f, 90f, false
        )

        // Bottom edge to arrow
        path.lineTo(size.width / 2 + arrowWidthPx / 2, size.height - arrowHeightPx)

        // Arrow (three sides of the triangle)
        path.lineTo(size.width / 2, size.height) // Right side of triangle
        path.lineTo(size.width / 2 - arrowWidthPx / 2, size.height - arrowHeightPx) // Left side of triangle

        // Bottom edge from arrow
        path.lineTo(cornerRadiusPx, size.height - arrowHeightPx)

        // Bottom-left corner
        path.arcTo(
            Rect(
                left = 0f,
                top = size.height - 2 * cornerRadiusPx - arrowHeightPx,
                right = 2 * cornerRadiusPx,
                bottom = size.height - arrowHeightPx
            ),
            90f, 90f, false
        )

        // Left edge
        path.lineTo(0f, cornerRadiusPx)

        // Top-left corner
        path.arcTo(
            Rect(
                left = 0f,
                top = 0f,
                right = 2 * cornerRadiusPx,
                bottom = 2 * cornerRadiusPx
            ),
            180f, 90f, false
        )

        path.close()
        return Outline.Generic(path)
    }
}

