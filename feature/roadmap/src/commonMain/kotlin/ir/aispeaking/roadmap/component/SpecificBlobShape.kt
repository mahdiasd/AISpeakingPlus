package ir.aispeaking.roadmap.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// This shape is specifically designed to approximate the single blob image provided.
class SpecificBlobShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Define the shape using a Path.
        // These coordinates are relative to the size (0 to 1 scale)
        // and approximate the shape from the image.

        // Use a PathBuilder for slightly more concise syntax,
        // although direct Path commands work too.
        val path = Path().apply {
            // Scale coordinates helper
            fun Float.scaleX() = this * size.width
            fun Float.scaleY() = this * size.height

            // Based on visual approximation of the provided blob image:
            // Starting point (roughly top-middle)
            moveTo(0.5f.scaleX(), 0.08f.scaleY())

            // Curve down towards the right (top-right part)
            cubicTo(
                0.7f.scaleX(), 0.005f.scaleY(),   // Control point 1 (pulls right, slightly up)
                0.9f.scaleX(), 0.001f.scaleY(),    // Control point 2 (pulls right)
                0.92f.scaleX(), 0.04f.scaleY()    // End point (right side)
            )

            // Curve further down the right side
            cubicTo(
                0.95f.scaleX(), 1.8f.scaleY(),   // Control point 1 (pulls right)
                0.85f.scaleX(), 0.85f.scaleY(),  // Control point 2 (pulls down-right)
                0.65f.scaleX(), 0.93f.scaleY()   // End point (bottom-right)
            )

            // Curve across the bottom
            cubicTo(
                0.45f.scaleX(), 0.98f.scaleY(),  // Control point 1 (pulls down)
                0.25f.scaleX(), 0.9f.scaleY(),   // Control point 2 (pulls down-left)
                0.18f.scaleX(), 0.75f.scaleY()   // End point (bottom-left)
            )

            // Curve up the left side (this section needs to capture the slight inward bulge)
            cubicTo(
                0.08f.scaleX(), 0.6f.scaleY(),   // Control point 1 (pulls significantly inwards/left)
                0.08f.scaleX(), 0.4f.scaleY(),   // Control point 2 (pulls significantly inwards/left)
                0.2f.scaleX(), 0.25f.scaleY()    // End point (upper-left)
            )

            // Curve back to the starting point (top-left part)
            cubicTo(
                0.28f.scaleX(), 0.15f.scaleY(),  // Control point 1 (pulls up-left)
                0.4f.scaleX(), 0.08f.scaleY(),   // Control point 2 (pulls up)
                0.5f.scaleX(), 0.08f.scaleY()    // End point (back to start)
            )

            close() // Connect the last point back to the first
        }

        return Outline.Generic(path)
    }
}

@Preview
@Composable
fun SpecificBlobPreview() {
    // Approximate colors from the image
    val fillColor = Color(0xFF0F3B3D) // Dark teal
    val borderColor = Color(0xFF80DEEA) // Light blue

    Box(
        modifier = Modifier
            .size(150.dp, 150.dp) // Size similar to the preview image
            .background(
                color = fillColor, // Apply the fill color
                shape = SpecificBlobShape() // Apply the custom shape
            )
            .border(
                width = 2.dp, // Adjust border width as needed
                color = borderColor, // Apply the border color
                shape = SpecificBlobShape() // Apply the custom shape's outline for the border
            ),
        contentAlignment = Alignment.Center
    ) {
        // Add content inside the blob if needed, e.g., Text("Level 3")
    }
}