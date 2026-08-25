package ir.aispeaking.roadmap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Defines the available types of blob shapes shown in the level map.
 */
enum class BlobType {
    START,   // The start icon shape (more circular/lumpy)
    LEVEL_1, // First level shape (oval, wider)
    LEVEL_2, // Second level shape (triangular-ish, point up)
    LEVEL_3, // Third level shape (asymmetrical, taller, left indent)
    LEVEL_5  // Fifth level shape (triangular-ish, point top-right)
}

/**
 * A custom Jetpack Compose Shape that renders various blob shapes
 * for a level progression map UI, based on the provided image.
 *
 * The path is defined using cubic Bézier curves relative to the component size (0.0 to 1.0).
 * This version refines the paths for LEVEL_1 and LEVEL_5.
 */
class BlobShape(private val type: BlobType) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()
        val width = size.width
        val height = size.height

        // Scale coordinates for easier reading (0 to 1 scale)
        fun Float.scaleX() = this * width
        fun Float.scaleY() = this * height

        when (type) {
            BlobType.START -> {
                // Similar to the profile picture shape - a slightly irregular circle
                path.moveTo(0.5f.scaleX(), 0.05f.scaleY()) // Top middle
                path.cubicTo( // Top right curve
                    0.75f.scaleX(), 0.05f.scaleY(),
                    0.95f.scaleX(), 0.25f.scaleY(),
                    0.95f.scaleX(), 0.5f.scaleY()
                )
                path.cubicTo( // Bottom right curve
                    0.95f.scaleX(), 0.75f.scaleY(),
                    0.75f.scaleX(), 0.95f.scaleY(),
                    0.5f.scaleX(), 0.95f.scaleY() // Bottom middle
                )
                path.cubicTo( // Bottom left curve
                    0.25f.scaleX(), 0.95f.scaleY(),
                    0.05f.scaleX(), 0.75f.scaleY(),
                    0.05f.scaleX(), 0.5f.scaleY() // Left middle
                )
                path.cubicTo( // Top left curve
                    0.05f.scaleX(), 0.25f.scaleY(),
                    0.25f.scaleX(), 0.05f.scaleY(),
                    0.5f.scaleX(), 0.05f.scaleY() // Back to top middle
                )
            }

            BlobType.LEVEL_1 -> {
                // *** REVISED LEVEL_1 SHAPE ***
                // Oval, wider than tall, smoother curves
                path.moveTo(0.2f.scaleX(), 0.5f.scaleY()) // Left middle
                path.cubicTo( // Top curve
                    0.25f.scaleX(), 0.15f.scaleY(),  // Control point 1 (pulls up-left)
                    0.65f.scaleX(), 0.1f.scaleY(),   // Control point 2 (pulls up-right, slightly flatter)
                    0.8f.scaleX(), 0.3f.scaleY()     // End point (top-right area)
                )
                path.cubicTo( // Right side curve
                    0.9f.scaleX(), 0.5f.scaleY(),    // Control point 1 (pulls right)
                    0.8f.scaleX(), 0.85f.scaleY(),   // Control point 2 (pulls down-right)
                    0.6f.scaleX(), 0.9f.scaleY()     // End point (bottom-right area)
                )
                path.cubicTo( // Bottom curve
                    0.4f.scaleX(), 0.95f.scaleY(),   // Control point 1 (pulls down, slightly flatter)
                    0.1f.scaleX(), 0.75f.scaleY(),   // Control point 2 (pulls down-left)
                    0.2f.scaleX(), 0.5f.scaleY()     // End point (back to left middle)
                )
            }

            BlobType.LEVEL_2 -> {
                // Triangular-ish with rounded corners, point generally upwards
                path.moveTo(0.5f.scaleX(), 0.1f.scaleY()) // Top point
                path.cubicTo( // Top-right side curve
                    0.75f.scaleX(), 0.15f.scaleY(),
                    0.9f.scaleX(), 0.4f.scaleY(),
                    0.85f.scaleX(), 0.7f.scaleY() // Bottom right corner
                )
                path.cubicTo( // Bottom curve
                    0.8f.scaleX(), 0.9f.scaleY(),
                    0.2f.scaleX(), 0.9f.scaleY(),
                    0.15f.scaleX(), 0.7f.scaleY() // Bottom left corner
                )
                path.cubicTo( // Top-left side curve
                    0.1f.scaleX(), 0.4f.scaleY(),
                    0.25f.scaleX(), 0.15f.scaleY(),
                    0.5f.scaleX(), 0.1f.scaleY() // Back to top point
                )
            }

            BlobType.LEVEL_3 -> {
                // Taller asymmetrical shape with a noticeable indent on the bottom-left
                path.moveTo(0.4f.scaleX(), 0.1f.scaleY()) // Top area, slightly left
                path.cubicTo( // Top curve towards right
                    0.6f.scaleX(), 0.05f.scaleY(),
                    0.8f.scaleX(), 0.15f.scaleY(),
                    0.85f.scaleX(), 0.35f.scaleY() // Top right area
                )
                path.cubicTo( // Right side curve down
                    0.9f.scaleX(), 0.55f.scaleY(),
                    0.8f.scaleX(), 0.75f.scaleY(),
                    0.6f.scaleX(), 0.85f.scaleY() // Bottom right area
                )
                path.cubicTo( // Bottom curve towards left
                    0.4f.scaleX(), 0.9f.scaleY(),
                    0.2f.scaleX(), 0.8f.scaleY(),
                    0.15f.scaleX(), 0.65f.scaleY() // Bottom left area before indent
                )
                path.cubicTo( // Indent curve - goes inwards then out
                    0.05f.scaleX(), 0.5f.scaleY(), // Pull inward significantly
                    0.1f.scaleX(), 0.4f.scaleY(),
                    0.2f.scaleX(), 0.3f.scaleY() // After indent, lower left area
                )
                path.cubicTo( // Curve back up to start
                    0.25f.scaleX(), 0.2f.scaleY(),
                    0.3f.scaleX(), 0.15f.scaleY(),
                    0.4f.scaleX(), 0.1f.scaleY() // Back to start
                )
            }

            BlobType.LEVEL_5 -> {
                // *** REVISED LEVEL_5 SHAPE ***
                // Triangular-ish with rounded corners, point generally top-right
                path.moveTo(0.75f.scaleX(), 0.2f.scaleY()) // Slightly softer top-right area
                path.cubicTo( // Right side curve down
                    0.85f.scaleX(), 0.3f.scaleY(),   // Control point 1 (pulls right)
                    0.8f.scaleX(), 0.5f.scaleY(),    // Control point 2 (pulls right, center)
                    0.7f.scaleX(), 0.65f.scaleY()    // End point (middle-right)
                )
                path.cubicTo( // Bottom curve left
                    0.6f.scaleX(), 0.8f.scaleY(),    // Control point 1 (pulls down-right)
                    0.4f.scaleX(), 0.8f.scaleY(),    // Control point 2 (pulls down-left)
                    0.3f.scaleX(), 0.65f.scaleY()    // End point (middle-left)
                )
                path.cubicTo( // Left side curve up
                    0.2f.scaleX(), 0.5f.scaleY(),    // Control point 1 (pulls left, center)
                    0.15f.scaleX(), 0.3f.scaleY(),   // Control point 2 (pulls left)
                    0.25f.scaleX(), 0.2f.scaleY()    // End point (upper-left area)
                )
                path.cubicTo( // Top curve back to point
                    0.35f.scaleX(), 0.15f.scaleY(),  // Control point 1 (pulls up-left)
                    0.65f.scaleX(), 0.15f.scaleY(),  // Control point 2 (pulls up-right)
                    0.75f.scaleX(), 0.2f.scaleY()    // End point (back to start)
                )
            }
        }

        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun LevelNode(type: BlobType, text: String, subtext: String? = null) {
    Column(
        modifier = Modifier
            .size(120.dp, 100.dp) // Adjust size as needed
            .background(
                brush = Brush.linearGradient( // Example gradient color
                    colors = listOf(Color(0xFF00798E), Color(0xFF003B4A)),
                    start = Offset(0f, 0f),
                    end = Offset(100f, 100f)
                ),
                shape = BlobShape(type) // Apply the custom shape
            )
            .padding(16.dp), // Add padding inside the shape
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        if (subtext != null) {
            Text(text = subtext, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
        }
    }
}

@Preview
@Composable
fun LevelNodesPreview() {
    Column {
        LevelNode(type = BlobType.START, text = "START", subtext = null)
        Spacer(Modifier.height(8.dp))
        LevelNode(type = BlobType.LEVEL_1, text = "Level 1", subtext = "5-50")
        Spacer(Modifier.height(8.dp))
        LevelNode(type = BlobType.LEVEL_2, text = "Level 2", subtext = "51-100")
        Spacer(Modifier.height(8.dp))
        LevelNode(type = BlobType.LEVEL_3, text = "Level 3", subtext = "101-150")
        Spacer(Modifier.height(8.dp))
        LevelNode(type = BlobType.LEVEL_5, text = "Level 5", subtext = "201-250")
    }
}