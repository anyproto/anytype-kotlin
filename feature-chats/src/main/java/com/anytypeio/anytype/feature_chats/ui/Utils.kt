package com.anytypeio.anytype.feature_chats.ui

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.feature_chats.R
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt


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

fun Modifier.horizontalSwipeToReply(
    swipeThreshold: Float,
    onReplyTriggered: () -> Unit
): Modifier = composed {

    val haptic = LocalHapticFeedback.current

    var offsetX by remember { mutableStateOf(0f) }

    this
        .offset { IntOffset(offsetX.roundToInt(), 0) }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val pointerId = down.id
                    val startPos = down.position
                    var replyHapticTriggered = false
                    val dragStartTime = System.currentTimeMillis()

                    var accepted = false
                    var swipeOffset = 0f

                    while (true) {
                        val event = awaitPointerEvent()
                        val drag = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!drag.pressed) break

                        val delta = drag.position - startPos
                        val dx = delta.x
                        val dy = delta.y

                        if (!accepted) {
                            val distance = delta.getDistance()
                            if (distance > 12f) {
                                val angle = Math.toDegrees(atan2(abs(dy), abs(dx)).toDouble()).toFloat()
                                if (angle < 25f) {
                                    accepted = true
                                    drag.consume()
                                } else {
                                    break
                                }
                            }
                        } else {
                            val dxStep = drag.positionChange().x
                            swipeOffset += dxStep
                            offsetX = swipeOffset.coerceAtMost(0f)
                            drag.consume()

                            if (!replyHapticTriggered && offsetX < -swipeThreshold) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                replyHapticTriggered = true
                            }
                        }
                    }

                    if (accepted && offsetX < -swipeThreshold) {
                        val dragDuration = System.currentTimeMillis() - dragStartTime
                        if (dragDuration > 160) {
                            onReplyTriggered()
                        }
                    }

                    offsetX = 0f
                }
            }
        }
}

class AnnotatedTextTransformation(
    private val spans: List<ChatBoxSpan>
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val annotatedString = AnnotatedString.Builder(text).apply {
            val processedRanges = mutableSetOf<Int>()
            
            spans.forEachIndexed { index, span ->
                if (index in processedRanges) return@forEachIndexed
                if (span.start !in text.indices || span.end > text.length) return@forEachIndexed
                
                // Find all spans with the same range
                val sameRangeSpans = mutableListOf(span)
                for (i in (index + 1) until spans.size) {
                    val other = spans[i]
                    if (other.start == span.start && other.end == span.end) {
                        sameRangeSpans.add(other)
                        processedRanges.add(i)
                    }
                }
                
                // Collect text decorations
                val decorations = sameRangeSpans.mapNotNull { it.style.textDecoration }
                
                if (decorations.size > 1) {
                    // Multiple text decorations - combine them
                    val combinedDecoration = TextDecoration.combine(decorations)
                    
                    // Apply non-decoration styles
                    sameRangeSpans.forEach { s ->
                        if (s.style.textDecoration == null) {
                            addStyle(s.style, span.start, span.end)
                        } else {
                            val nonDecorationStyle = s.style.copy(textDecoration = null)
                            if (nonDecorationStyle != SpanStyle()) {
                                addStyle(nonDecorationStyle, span.start, span.end)
                            }
                        }
                    }
                    
                    // Apply combined decoration once
                    addStyle(SpanStyle(textDecoration = combinedDecoration), span.start, span.end)
                } else {
                    // No conflict - apply normally
                    sameRangeSpans.forEach { s ->
                        addStyle(s.style, span.start, span.end)
                    }
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

    data class Markup(
        override val style: SpanStyle,
        override val start: Int,
        override val end: Int,
        val type: Int
    ) : ChatBoxSpan() {
        companion object {
            const val BOLD = 0
            const val ITALIC = 1
            const val STRIKETHROUGH = 2
            const val UNDERLINE = 3
            const val CODE = 4
        }
    }
}

const val DEFAULT_MENTION_SPAN_TAG = "@-mention"
const val DEFAULT_MENTION_LINK_TAG = "link"