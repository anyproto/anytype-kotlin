package com.anytypeio.anytype.feature_discussions.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.text.AnnotatedTextTransformation
import com.anytypeio.anytype.core_ui.text.InputSpan
import com.anytypeio.anytype.core_ui.text.MarkupEvent
import com.anytypeio.anytype.core_ui.text.normalizeSpans
import com.anytypeio.anytype.core_ui.text.toggleSpan
import com.anytypeio.anytype.core_ui.views.PreviewTitle1Regular
import kotlinx.coroutines.launch

@Composable
fun DiscussionCommentInput(
    text: TextFieldValue,
    spans: List<InputSpan>,
    onValueChange: (TextFieldValue, List<InputSpan>) -> Unit,
    onSendClicked: (String, List<InputSpan>) -> Unit,
    onPlusClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    var showMarkup by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .padding(
                start = 12.dp,
                end = 12.dp,
                bottom = 10.dp
            )
            .border(
                width = 1.dp,
                color = colorResource(R.color.shape_tertiary),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = colorResource(R.color.background_primary),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Main input row: [plus (if !focused)] + [text input] + [send (if !focused)]
        Row {
            if (!isFocused) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable { onPlusClicked() }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_plus
                        ),
                        contentDescription = "Plus button",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                CommentUserInput(
                    text = text,
                    spans = spans,
                    onValueChange = { newValue ->
                        val updatedSpans = normalizeSpans(
                            oldText = text.text,
                            newText = newValue.text,
                            spans = spans
                        )
                        onValueChange(newValue, updatedSpans)
                    },
                    onFocusChanged = { isFocused = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = if (!isFocused) 4.dp else 12.dp,
                            end = 4.dp,
                            top = 16.dp,
                            bottom = 16.dp
                        )
                )
            }
            AnimatedVisibility(
                visible = (text.text.isNotEmpty()) && !isFocused,
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn(),
                modifier = Modifier.align(Alignment.Bottom)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .clip(CircleShape)
                        .clickable {
                            onSendClicked(text.text, spans)
                            showMarkup = false
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_send_message
                        ),
                        contentDescription = "Send comment",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        if (!isFocused) return@Column

        // Bottom toolbar row (when focused): [edit panel / markup panel] + [send button]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            AnimatedContent(
                targetState = showMarkup,
                label = "DISCUSSION_PANELS",
                modifier = Modifier.weight(1.0f)
            ) { isMarkup ->
                if (isMarkup) {
                    DiscussionMarkupPanel(
                        selectionStart = text.selection.start,
                        selectionEnd = text.selection.end,
                        spans = spans,
                        onBackClicked = { showMarkup = false },
                        onMarkupEvent = { event ->
                            scope.launch {
                                val selection = text.selection
                                if (selection.start == selection.end) return@launch

                                val newSpan = when (event) {
                                    MarkupEvent.Bold -> InputSpan.Markup(
                                        style = SpanStyle(fontWeight = FontWeight.Bold),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.BOLD
                                    )
                                    MarkupEvent.Italic -> InputSpan.Markup(
                                        style = SpanStyle(fontStyle = FontStyle.Italic),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.ITALIC
                                    )
                                    MarkupEvent.Strike -> InputSpan.Markup(
                                        style = SpanStyle(textDecoration = TextDecoration.LineThrough),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.STRIKETHROUGH
                                    )
                                    MarkupEvent.Underline -> InputSpan.Markup(
                                        style = SpanStyle(textDecoration = TextDecoration.Underline),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.UNDERLINE
                                    )
                                    MarkupEvent.Code -> InputSpan.Markup(
                                        style = SpanStyle(fontFamily = FontFamily.Monospace),
                                        start = selection.start,
                                        end = selection.end,
                                        type = InputSpan.Markup.CODE
                                    )
                                }

                                val updatedSpans = toggleSpan(
                                    selectionStart = selection.start,
                                    selectionEnd = selection.end,
                                    spans = spans,
                                    newSpan = newSpan
                                )
                                onValueChange(text, updatedSpans)
                            }
                        }
                    )
                } else {
                    DiscussionEditPanel(
                        onPlusClicked = onPlusClicked,
                        onStyleClicked = { showMarkup = true },
                        onMentionClicked = {
                            val selection = text.selection
                            val cursorPosition = selection.start
                            val updatedText = text.text.substring(0, cursorPosition) +
                                    "@" +
                                    text.text.substring(cursorPosition)
                            val newSelection = TextRange(cursorPosition + 1)
                            onValueChange(
                                TextFieldValue(updatedText, selection = newSelection),
                                spans
                            )
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = text.text.isNotEmpty(),
                exit = fadeOut() + scaleOut(),
                enter = fadeIn() + scaleIn()
            ) {
                Box(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp)
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .clickable {
                            onSendClicked(text.text, spans)
                            showMarkup = false
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_send_message
                        ),
                        contentDescription = "Send comment",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentUserInput(
    text: TextFieldValue,
    spans: List<InputSpan>,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = text,
        onValueChange = onValueChange,
        textStyle = PreviewTitle1Regular.copy(
            color = colorResource(id = R.color.text_primary)
        ),
        modifier = modifier
            .onFocusChanged { state ->
                onFocusChanged(state.isFocused)
            },
        cursorBrush = SolidColor(colorResource(id = R.color.palette_system_blue)),
        maxLines = 5,
        singleLine = false,
        decorationBox = @Composable { innerTextField ->
            CommentHintDecorationBox(
                text = text.text,
                hint = stringResource(
                    id = com.anytypeio.anytype.localization.R.string.discussion_leave_comment
                ),
                innerTextField = innerTextField,
                textStyle = PreviewTitle1Regular.copy(
                    color = colorResource(R.color.text_tertiary)
                )
            )
        },
        visualTransformation = AnnotatedTextTransformation(spans),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentHintDecorationBox(
    text: String,
    hint: String,
    innerTextField: @Composable () -> Unit,
    textStyle: TextStyle
) {
    OutlinedTextFieldDefaults.DecorationBox(
        value = text,
        visualTransformation = VisualTransformation.None,
        innerTextField = innerTextField,
        singleLine = false,
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
