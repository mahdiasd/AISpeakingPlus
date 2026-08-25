package ir.aispeaking.sharedui.ui.core.input

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import ir.aispeaking.utils.addSpaces
import ir.aispeaking.utils.dLog

class SpaceTransformation(val spaceEvery: Int = 3) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = AnnotatedString(text.text.addSpaces(spaceEvery)),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val a = text.text.addSpaces(spaceEvery).length
                    a.dLog("originalToTransformed:")
                    return a
                }

                override fun transformedToOriginal(offset: Int): Int {
                    text.length.dLog("transformedToOriginal:")
                    return text.length
                }
            }
        )
    }
}