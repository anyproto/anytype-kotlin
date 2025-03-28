package com.anytypeio.anytype.ui.onboarding.screens.signin

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.anytypeio.anytype.core_ui.MnemonicPhrasePaletteColors

object MnemonicPhraseFormatter : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = buildAnnotatedString {
            var colorIndex = 0
            var isPreviousLetterOrDigit = false
            text.forEachIndexed { index, char ->
                if (char.isLetterOrDigit()) {
                    withStyle(
                        style = SpanStyle(
                            color = MnemonicPhrasePaletteColors[colorIndex]
                        )
                    ) {
                        append(char)
                    }
                    isPreviousLetterOrDigit = true
                } else {
                    if (isPreviousLetterOrDigit) {
                        colorIndex = colorIndex.inc()
                        isPreviousLetterOrDigit = false
                    }
                    append(char)
                }
                if (colorIndex > MnemonicPhrasePaletteColors.lastIndex) {
                    colorIndex = 0
                }
            }
        }
        return TransformedText(
            transformed,
            OffsetMapping.Identity
        )
    }
}