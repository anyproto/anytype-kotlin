package com.anytypeio.anytype.feature_discussions.ui

import android.content.res.Configuration
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.anytypeio.anytype.feature_discussions.R
import com.anytypeio.anytype.feature_discussions.presentation.DiscussionView
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun DiscussionPreview() {
    Messages(
        messages = listOf(
            DiscussionView.Message(
                id = "1",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Walter",
                timestamp = System.currentTimeMillis()
            ),
            DiscussionView.Message(
                id = "2",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Leo",
                timestamp = System.currentTimeMillis()
            ),
            DiscussionView.Message(
                id = "3",
                msg = stringResource(id = R.string.default_text_placeholder),
                author = "Gilbert",
                timestamp = System.currentTimeMillis()
            )
        ),
        scrollState = LazyListState()
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun DiscussionScreenPreview() {
    DiscussionScreen(
        title = "Conversations with friends",
        messages = buildList {
            repeat(30) { idx ->
                add(
                    DiscussionView.Message(
                        id = idx.toString(),
                        msg = stringResource(id = R.string.default_text_placeholder),
                        author = "User ${idx.inc()}",
                        timestamp =
                        System.currentTimeMillis()
                                - 30.toDuration(DurationUnit.DAYS).inWholeMilliseconds
                                + idx.toDuration(DurationUnit.DAYS).inWholeMilliseconds
                    )
                )
            }
        }.reversed(),
        onMessageSent = {}
    )
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Light Mode")
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO, name = "Dark Mode")
@Composable
fun BubblePreview() {
    Bubble(
        name = "Leo Marx",
        msg = stringResource(id = R.string.default_text_placeholder),
        timestamp = System.currentTimeMillis()
    )
}