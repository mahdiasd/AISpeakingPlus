package ir.aispeaking.sharedui.ui.lottie

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.dynamic.rememberLottieDynamicProperties
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter
import ir.aispeaking.sharedui.Res

@OptIn(ExperimentalCompottieApi::class)
@Composable
fun LottieLoader(
    modifier: Modifier = Modifier,
    jsonPath: String = "files/lt_recording_anim.json",
    iterations: Int = Compottie.IterateForever,
    isPlaying: Boolean = true,
    speed: Float = 1f,
    contentScale: ContentScale = ContentScale.Fit,
    tint: Color? = null,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes(jsonPath).decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying,
        speed = speed
    )

    val dynamicProperties = rememberLottieDynamicProperties {
        if (tint != null) {
            shapeLayer("**", "**") {
                fill("**", "**") {
                    color { tint }
                }
            }
        }
    }

    Image(
        painter = rememberLottiePainter(
            composition = composition,
            progress = { progress },
            dynamicProperties = dynamicProperties
        ),
        contentDescription = "Lottie animation",
        modifier = modifier,
        contentScale = contentScale
    )
}