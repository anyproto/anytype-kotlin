package com.anytypeio.anytype.ui.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.common.DefaultPreviews

/**
 * Previews for TitleRow composable covering all layout cases.
 *
 * Gap constants:
 * - iconTextGap = 4.dp (between text and muted icon)
 * - textTimeGap = 8.dp (between left-content and right-aligned content)
 * - pendingTimeGap = 4.dp (between pending indicator and time)
 */

// Case 1: Text only (no gaps)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextOnly() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = null,
            isMuted = null,
            showPendingIndicator = false
        )
    }
}

// Case 2: Text + Time (textTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextWithTime() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = "14:32",
            isMuted = null,
            showPendingIndicator = false
        )
    }
}

// Case 3: Text + Muted icon (iconTextGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextWithMuted() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = null,
            isMuted = true,
            showPendingIndicator = false
        )
    }
}

// Case 4: Text + Pending indicator (textTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextWithPending() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = null,
            isMuted = null,
            showPendingIndicator = true
        )
    }
}

// Case 5: Text + Muted + Time (iconTextGap + textTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextMutedTime() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = "14:32",
            isMuted = true,
            showPendingIndicator = false
        )
    }
}

// Case 6: Text + Pending + Time (textTimeGap + pendingTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextPendingTime() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = "14:32",
            isMuted = null,
            showPendingIndicator = true
        )
    }
}

// Case 7: Text + Muted + Pending (iconTextGap + textTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_TextMutedPending() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = null,
            isMuted = true,
            showPendingIndicator = true
        )
    }
}

// Case 8: All elements - Text + Muted + Pending + Time (iconTextGap + textTimeGap + pendingTimeGap)
@Composable
@DefaultPreviews
fun TitleRowPreview_AllElements() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "Chat Title",
            messageTime = "14:32",
            isMuted = true,
            showPendingIndicator = true
        )
    }
}

// Bonus: Long text to test ellipsis behavior with all elements
@Composable
@DefaultPreviews
fun TitleRowPreview_LongTextAllElements() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.background_primary))
            .padding(16.dp)
    ) {
        TitleRow(
            modifier = Modifier.fillMaxWidth(),
            message = "This is a very long chat title that should be truncated with ellipsis",
            messageTime = "14:32",
            isMuted = true,
            showPendingIndicator = true
        )
    }
}
