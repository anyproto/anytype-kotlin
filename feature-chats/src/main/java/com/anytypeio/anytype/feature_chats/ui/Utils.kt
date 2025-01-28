package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.views.HeadlineTitle
import com.anytypeio.anytype.feature_chats.R
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun DefaultHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = true,
        enabled = true,
        placeholder = {
            Text(
                text = hint,
                color = colorResource(id = R.color.text_tertiary),
                style = textStyle
            )
        },
        interactionSource = remember { MutableInteractionSource() },
        colors = OutlinedTextFieldDefaults.colors(
            disabledBorderColor = Color.Transparent,
            errorBorderColor = Color.Transparent,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        contentPadding = PaddingValues()
    )
}

class AnnotatedTextTransformation(
    private val spans: List<ChatBoxSpan>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = AnnotatedString.Builder(text).apply {
            spans.forEach { span ->
                Timber.d("Checking span before render: $span")
                if (span.start in text.indices && span.end <= text.length) {
                    addStyle(span.style, span.start, span.end)
                }
            }
        }.toAnnotatedString()

        return TransformedText(annotatedString, offsetMapping = OffsetMapping.Identity)
    }
}

sealed class ChatBoxSpan {
    abstract val start: Int
    abstract val end: Int
    abstract val style: SpanStyle

    data class Mention(
        override val style: SpanStyle,
        override val start: Int,
        override val end: Int,
        val param: Id
    ) : ChatBoxSpan()
}

const val DEFAULT_MENTION_SPAN_TAG = "@-mention"
const val DEFAULT_MENTION_LINK_TAG = "link"