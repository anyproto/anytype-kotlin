package com.anytypeio.anytype.feature_discussions.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.foundation.noRippleClickable
import com.anytypeio.anytype.core_ui.text.InputSpan
import com.anytypeio.anytype.core_ui.text.MarkupEvent

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscussionMarkupPanel(
    modifier: Modifier = Modifier,
    selectionStart: Int,
    selectionEnd: Int,
    spans: List<InputSpan> = emptyList(),
    onMarkupEvent: (MarkupEvent) -> Unit = {},
    onBackClicked: () -> Unit
) {
    val activeTypes: Set<Int> = remember(spans, selectionStart, selectionEnd) {
        spans
            .filterIsInstance<InputSpan.Markup>()
            .filter { span ->
                span.start < selectionEnd && selectionStart < span.end
            }
            .map { it.type }
            .toSet()
    }

    val isBold = InputSpan.Markup.BOLD in activeTypes
    val isItalic = InputSpan.Markup.ITALIC in activeTypes
    val isStrike = InputSpan.Markup.STRIKETHROUGH in activeTypes
    val isUnderline = InputSpan.Markup.UNDERLINE in activeTypes
    val isCode = InputSpan.Markup.CODE in activeTypes

    CompositionLocalProvider(
        LocalOverscrollConfiguration provides null
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkupIcon(
                onClick = onBackClicked,
                resId = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_markup_back,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp)
            )

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
                    .weight(1.0f)
                    .height(52.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MarkupIcon(
                    onClick = { onMarkupEvent(MarkupEvent.Bold) },
                    resId = if (isBold) R.drawable.ic_toolbar_markup_bold_active
                    else R.drawable.ic_toolbar_markup_bold
                )
                MarkupIcon(
                    onClick = { onMarkupEvent(MarkupEvent.Italic) },
                    resId = if (isItalic) R.drawable.ic_toolbar_markup_italic_active
                    else R.drawable.ic_toolbar_markup_italic
                )
                MarkupIcon(
                    onClick = { onMarkupEvent(MarkupEvent.Strike) },
                    resId = if (isStrike) R.drawable.ic_toolbar_markup_strike_through_active
                    else R.drawable.ic_toolbar_markup_strike_through
                )
                MarkupIcon(
                    onClick = { onMarkupEvent(MarkupEvent.Underline) },
                    resId = if (isUnderline) R.drawable.ic_toolbar_markup_underline_active
                    else R.drawable.ic_toolbar_markup_underline
                )
                MarkupIcon(
                    onClick = { onMarkupEvent(MarkupEvent.Code) },
                    resId = if (isCode) R.drawable.ic_toolbar_markup_code_active
                    else R.drawable.ic_toolbar_markup_code
                )
            }
        }
    }
}

@Composable
fun DiscussionEditPanel(
    modifier: Modifier = Modifier,
    onPlusClicked: () -> Unit,
    onStyleClicked: () -> Unit,
    onMentionClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(
                id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_plus
            ),
            contentDescription = "Plus button",
            modifier = Modifier
                .padding(start = 12.dp)
                .noRippleClickable { onPlusClicked() }
        )

        Spacer(modifier = Modifier.width(20.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_style_32),
            contentDescription = "Style",
            modifier = Modifier.noRippleClickable { onStyleClicked() }
        )

        Spacer(modifier = Modifier.width(20.dp))

        Image(
            painter = painterResource(
                id = com.anytypeio.anytype.feature_discussions.R.drawable.ic_discussion_mention
            ),
            contentDescription = "Mention",
            modifier = Modifier.noRippleClickable { onMentionClicked() }
        )
    }
}

@Composable
private fun MarkupIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @DrawableRes resId: Int
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .noRippleClickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(resId),
            contentDescription = null
        )
    }
}
